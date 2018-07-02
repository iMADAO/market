package cn.haizhi.market.main.service.madao;

import cn.haizhi.market.main.bean.madao.*;
import cn.haizhi.market.main.bean.qiyuan.UserAddress;
import cn.haizhi.market.main.bean.richard.GroupProduct;
import cn.haizhi.market.main.mapper.madao.CommonMapper;
import cn.haizhi.market.main.mapper.madao.OrderItemMapper;
import cn.haizhi.market.main.mapper.madao.PgOrderMasterMapper;
import cn.haizhi.market.main.mapper.richard.GroupProductMapper;
import cn.haizhi.market.other.enums.madao.*;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.form.madao.OrderDeliveryStatusForm;
import cn.haizhi.market.other.form.madao.OrderPayBackForm;
import cn.haizhi.market.other.form.madao.RefundForm;
import cn.haizhi.market.other.util.*;
import cn.haizhi.market.other.util.madao.payUtils.OrderInfoUtil2_0;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.config.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.haizhi.market.other.util.ErrorResultUtil.getResultViewList;

@Service
@Slf4j
public class PgOrderService {
    @Autowired
    private CommonMapper commonMapper;
    @Autowired
    private PgOrderMasterMapper pgOrderMasterMapper;
    @Autowired
    private GroupProductMapper groupProductMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Value("seller_id")
    private String seller_id;

    private String url = "120.78.87.169:8080";
    @Autowired
    private PgCartItemJedisService pgCartItemJedisService;

    @Autowired
    private GroupService groupService;

    //添加拼购订单
    public PgOrderDTO addPgOrder(PgOrderCreateForm orderForm, Long userId) {
        //用于返回生成的订单
        PgOrderDTO pgOrderDTO = new PgOrderDTO();
        pgOrderDTO.setOrderItemList(new ArrayList<>());
        UserAddress address = commonMapper.getUserAddressById(orderForm.getAddressId());
        //检查地址是否正确
        if (address==null || !address.getUserId().equals(userId)) {
            log.error("用户地址信息错误");
            throw new MadaoException(ErrorEnum.ADDRESS_ERROR, IdResultMap.getIdMap(orderForm.getAddressId()));
        }

        //用于支付宝
        String subject = "";
        String body = "";

        //获取该订单的所有购物车项
        //在pgCartItemJedisService中已经检查购物车id是否有效
        List<PgCartItemDTO> pgCartItemDTOList = pgCartItemJedisService.getPgCartItemDTOByCartItemIdList(userId, orderForm.getCartItemIdList());


//      购物车在数据库的时候的版本
//        List<PgCartItemDTO> pgCartItemDTOList = commonMapper.getPgCartItemDTOByCartItemIdList(orderForm.getCartItemIdList());
//        //如果查出的购物项数量和传入表单的数量不一致，获取查不出的项的id抛出异常
//        if(!(pgCartItemDTOList.size()==orderForm.getCartItemIdList().size())){
//            List<String> id = new LinkedList<>();
//            id.addAll(orderForm.getCartItemIdList());
//            for(PgCartItemDTO pgCartItemDTO: pgCartItemDTOList){
//                id.remove(pgCartItemDTO.getItemId());
//            }
//            throw new MadaoException(ErrorEnum.CARTITEM_ERROR, IdResultMap.getIdMap(id));
//        }

        //处理拼购订单
        String orderId = KeyUtil.genUniquKey();
        BigDecimal orderAmount = BigDecimal.ZERO;

        //用于保存出错的购物车的id
        List<String> id = null;
        for (PgCartItemDTO pgCartItemDTO : pgCartItemDTOList) {
            //检查是否属于该用户
            if(!pgCartItemDTO.getUserId().equals(userId)){
                if(id==null)
                    id = new ArrayList<>();
                id.add(pgCartItemDTO.getItemId());
            }
            orderAmount = orderAmount.add(pgCartItemDTO.getProductNprice().multiply(BigDecimal.valueOf(pgCartItemDTO.getProductQuantity())));
            OrderItem orderItem = new OrderItem();
            BeanUtils.copyProperties(pgCartItemDTO, orderItem);
            orderItem.setItemId(KeyUtil.genUniquKey());
            orderItem.setProductPrice(pgCartItemDTO.getProductNprice());
            orderItem.setOrderId(orderId);
            orderItem.setProductIcon(pgCartItemDTO.getPicturePath());
            int result = orderItemMapper.insertSelective(orderItem);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
            pgOrderDTO.getOrderItemList().add(orderItem);

            subject += pgCartItemDTO.getProductName() + "-";
            body += pgCartItemDTO.getProductDesc() + "-";
        }
        if(id!=null)
            throw new MadaoException(ErrorEnum.CARTITEM_OWNER_ERROR, IdResultMap.getIdMap(id));

        PgOrderMaster pgOrderMaster = new PgOrderMaster();
        pgOrderMaster.setUserId(userId);
        pgOrderMaster.setUserName(address.getConsignee());
        pgOrderMaster.setUserPhone(address.getPhone());
        pgOrderMaster.setUserAddress(address.getUserAddress());
        pgOrderMaster.setOrderAmount(orderAmount);
        pgOrderMaster.setOrderId(orderId);

        int result = pgOrderMasterMapper.insertSelective(pgOrderMaster);
        if(result<=0)
            throw new MadaoException(ErrorEnum.OPERATION_FAIL);

        //将订单加入拼购组
        groupService.addToGroup(userId, pgOrderMaster.getOrderId(), orderForm.getGroupId());
        pgOrderMaster.setGroupId(orderForm.getGroupId());


        BeanUtils.copyProperties(pgOrderMaster, pgOrderDTO);

        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        Double price = pgOrderMaster.getOrderAmount().doubleValue();
//        String orderInfo = OrderInfoUtils.getOrderInfo(subject, body, price.toString(), orderId);
//        String sign = SignUtils.getSign(orderInfo);
//        pgOrderDTO.setSign(sign);


        Map<String, String> keyValues = new HashMap<String, String>();

        boolean rsa2 = true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdf.format(new Date());
        keyValues.put("app_id", AlipayConfig.appid);

        keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\""+price+"\",\"subject\":\""+subject+"\",\"body\":\""+body+"\",\"out_trade_no\":\"" + pgOrderMaster.getOrderId() +  "\"}");

        keyValues.put("charset", "utf-8");

        keyValues.put("method", "alipay.trade.app.pay");

        keyValues.put("sign_type", rsa2 ? "RSA2" : "RSA");

        keyValues.put("timestamp", datetime);

        keyValues.put("version", "1.0");

        keyValues.put("notify_url", url + "/market/pgOrder/notify");

        String orderParam = OrderInfoUtil2_0.buildOrderParam(keyValues);

        System.out.println("params-----------------------" + keyValues);
        String sign = OrderInfoUtil2_0.getSign(keyValues, AlipayConfig.private_key, true);
        final String orderInfo = orderParam + "&" + sign;


        pgOrderDTO.setOrderInfo(orderInfo);
        decreaseStock(pgCartItemDTOList);
        //调用删除购物车的方法
        pgCartItemJedisService.deleteCartItemListById(userId, orderForm.getCartItemIdList());
        return pgOrderDTO;
    }

    //根据购物车项列表扣库存
    public void decreaseStock(List<PgCartItemDTO> pgCartItemDTOList){
        //存放扣库存时错误的商品的  错误信息---购物项 键值对
        Map<ErrorEnum, List<String>> map = new HashMap<>();

        for(PgCartItemDTO pgCartItemDTO: pgCartItemDTOList){
            GroupProduct groupProduct = groupProductMapper.selectByPrimaryKey(pgCartItemDTO.getProductId());
            if(groupProduct==null) {
                if (!map.containsKey(ErrorEnum.PRODUCT_NOT_EXIST)) {
                    map.put(ErrorEnum.PRODUCT_NOT_EXIST, new ArrayList<>());
                }
                map.get(ErrorEnum.PRODUCT_NOT_EXIST).add(pgCartItemDTO.getItemId());
            }
//            TODO 商品下架的属性
//            if (groupProduct.getProductS) {
//                if(!map.containsKey(ErrorEnum.PRODUCT_DOWN)){
//                    map.put(ErrorEnum.PRODUCT_DOWN, new ArrayList<>());
//                }
//                map.get(ErrorEnum.PRODUCT_DOWN).add(pgCartItemDTO.getItemId());
//            }

            Integer result = groupProduct.getProductStock() - pgCartItemDTO.getProductQuantity();
            if(result<0) {
                log.error("【商品库存不足】-------Need={}---------product={}", pgCartItemDTO.getProductQuantity(), groupProduct);
                if(!map.containsKey(ErrorEnum.PRODUCT_STOCK_ERROR)){
                    map.put(ErrorEnum.PRODUCT_STOCK_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.PRODUCT_STOCK_ERROR).add(pgCartItemDTO.getItemId());
            }

            groupProduct.setProductStock(result);
            int result1 = groupProductMapper.updateByPrimaryKeySelective(groupProduct);
            if(result1<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if(map.size()>0){
            throw new MadaoException(ErrorEnum.ORDER_CREATE_ERROR, getResultViewList(map));
        }
    }


    //获取拼购订单列表
    public List<PgOrderDTO> getPgOrderDTO(Long userId, Byte orderStatus, Byte commentStatus, Byte cancelStatus, Byte refundStatus){
        return commonMapper.getPgOrderByUserId(userId, orderStatus, commentStatus, cancelStatus, refundStatus);
    }

    //用户支付拼购订单
//    public void payPgOrder(Long userId, List<String> orderIdList) {
//        List<PgOrderMaster> pgOrderMasterList = getPgOrderMasterByIdList(orderIdList);
//        BigDecimal amount = BigDecimal.ZERO;
//        Map<ErrorEnum, List<String>> map = new HashMap<>();
//        for(PgOrderMaster pgOrderMaster: pgOrderMasterList){
//            if(!pgOrderMaster.getUserId().equals(userId)){
//                if(!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR)){
//                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
//                }
//                map.get(ErrorEnum.ORDER_OWNER_ERROR).add(pgOrderMaster.getOrderId());
//            }
//            if(!pgOrderMaster.getOrderStatus().equals( PgOrderEnum.NEW.getCode())){
//                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR)){
//                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
//                }
//                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderMaster.getOrderId());
//            }
//            if(!pgOrderMaster.getPayStatus().equals(PayStatusEnum.WAIT.getCode())){
//                if(!map.containsKey(ErrorEnum.ORDER_PAY_STATUS_ERROR)){
//                    map.put(ErrorEnum.ORDER_PAY_STATUS_ERROR, new ArrayList<>());
//                }
//                map.get(ErrorEnum.ORDER_PAY_STATUS_ERROR).add(pgOrderMaster.getOrderId());
//            }
//            amount = amount.add(pgOrderMaster.getOrderAmount());
//        }
//
//        if (map.size()>0){
//            throw new MadaoException(ErrorEnum.ORDER_PAY_FAIL, getResultViewList(map));
//        }
//
//        //Todo 支付
//
//        for(PgOrderMaster pgOrderMaster: pgOrderMasterList){
//            pgOrderMaster.setPayStatus(PayStatusEnum.SUCCESS.getCode());
//            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
//            if(result<=0)
//                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
//        }
//    }



    //取消未支付拼购订单, 如果订单状态不是在付款之前，抛出异常
    public void cancelPgOrder(Long userId, List<String> orderIdList){
        List<PgOrderDTO> pgOrderDTOList = getPgOrderDTObyIdList(orderIdList);
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        List<OrderItem> orderItemList = new ArrayList<>();
        for(PgOrderDTO pgOrderDTO: pgOrderDTOList){
            if(!pgOrderDTO.getOrderStatus().equals(PgOrderEnum.NEW.getCode())) {
                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderDTO.getOrderId());
            }

            if(!pgOrderDTO.getUserId().equals(userId)){
                if(!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR))
                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_OWNER_ERROR).add(pgOrderDTO.getOrderId());
            }
            pgOrderMasterMapper.deleteByPrimaryKey(pgOrderDTO.getOrderId());
            orderItemList.addAll(pgOrderDTO.getOrderItemList());
        }
        if(map.size()!=0)
            throw new MadaoException(ErrorEnum.ORDER_CANCEL_FAIL, getResultViewList(map));
        //删除订单项
        removeOrderItem(orderItemList);
        //加库存
        increaseStock(orderItemList);
    }

    private void removeOrderItem(List<OrderItem> orderItemList) {
        for(OrderItem orderItem: orderItemList){
            orderItemMapper.deleteByPrimaryKey(orderItem.getItemId());
        }
    }

    //退单后加库存操作
    public void increaseStock(List<OrderItem> orderItemList){
        for(OrderItem orderItem: orderItemList){
            commonMapper.increaseGroupProductStock(orderItem.getProductId(), orderItem.getProductQuantity());
        }
    }

    //用户发起取消拼购订单
    public List<PgOrderMaster> cancelPgOrderByUser(Long userId, List<String> pgOrderIdList){
        List<PgOrderMaster> pgOrderMasterList = getPgOrderMasterByIdList(pgOrderIdList);

        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for(PgOrderMaster pgOrderMaster: pgOrderMasterList){
            if(!userId.equals(pgOrderMaster.getUserId())){
                log.error("【异常】={}", ErrorEnum.ORDER_OWNER_ERROR.getMessage());
                if(!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR))
                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_OWNER_ERROR).add(pgOrderMaster.getOrderId());
            }
            if(!pgOrderMaster.getCancelStatus().equals(CancelStatusEnum.NO_CANCEL.getCode())){
                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR.getMessage());
                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderMaster.getOrderId());
            }

            //判断时间信息是否可用,不可用则加入异常
            Date date = null;
            if(pgOrderMaster.getDeliveryTime()!=null)
                date = pgOrderMaster.getDeliveryTime();
            else if(pgOrderMaster.getArriveTime()!=null)
                date = pgOrderMaster.getArriveTime();
            else if(pgOrderMaster.getReceiveTime()!=null)
                date = pgOrderMaster.getReceiveTime();
            if(date==null){
                if(!map.containsKey(ErrorEnum.ORDER_DATE_ERROR)){
                    map.put(ErrorEnum.ORDER_DATE_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_DATE_ERROR).add(pgOrderMaster.getOrderId());
            }else if((new Date().getTime() - date.getTime())>86400000L){
                if(!map.containsKey(ErrorEnum.ORDER_HAD_OVERDUE))
                    map.put(ErrorEnum.ORDER_HAD_OVERDUE, new ArrayList<>());
                map.get(ErrorEnum.ORDER_HAD_OVERDUE).add(pgOrderMaster.getOrderId());
            }

            pgOrderMaster.setCancelStatus(CancelStatusEnum.TO_CANCEL.getCode());
            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if(map.size()>0)
            throw new MadaoException(ErrorEnum.ORDER_CANCEL_FAIL, getResultViewList(map));

        return pgOrderMasterList;
    }


    //商家确认取消拼购订单
//    public List<PgOrderDTO> confirmCancelPg(Long shopId, List<String> orderIdList){
//        //Todo 对商家的检验
//        List<PgOrderDTO> pgOrderDTOList = getPgOrderDTObyIdList(orderIdList);
//        PgOrderMaster pgOrderMaster = new PgOrderMaster();
//        List<OrderItem> orderItemList = new ArrayList<>();
//        Map<ErrorEnum, List<String>> map = new HashMap<>();
//        for(PgOrderDTO pgOrderDTO: pgOrderDTOList){
//            if(!pgOrderDTO.getOrderStatus().equals(PgOrderEnum.TO_CANCEL.getCode())){
//                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR.getMessage() + pgOrderDTO.getOrderId());
//                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
//                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
//                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderDTO.getOrderId());
//            }
//            pgOrderDTO.setOrderStatus(PgOrderEnum.CANCELED.getCode());
//            BeanUtils.copyProperties(pgOrderDTO, pgOrderMaster);
//            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
//            if(result<=0)
//                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
//            orderItemList.addAll(pgOrderDTO.getOrderItemList());
//        }
//        if(map.size()>0)
//            throw new MadaoException(ErrorEnum.CONFIRM_CANCEL_FAIL, getResultViewList(map));
//
//        //加库存
//        increaseStock(orderItemList);
//        return pgOrderDTOList;
//    }



    //商家取消拼购订单
//    public List<PgOrderDTO> cancelPgOrderByShop(List<String>  pgOrderIdList){
//        List<PgOrderDTO> pgOrderDTOList = getPgOrderDTObyIdList(pgOrderIdList);
//        List<OrderItem> orderItemList = new ArrayList<>();
//
//        PgOrderMaster pgOrderMaster = new PgOrderMaster();
//        Map<ErrorEnum, List<String>> map = new HashMap<>();
//        for(PgOrderDTO pgOrderDTO: pgOrderDTOList){
//            if(pgOrderDTO.getOrderStatus().equals(PgOrderEnum.CANCELED.getCode()) || pgOrderDTO.getOrderStatus().equals(PgOrderEnum.TO_CANCEL.getCode())){
//                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR, pgOrderDTO.getOrderId());
//                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
//                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
//                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderDTO.getOrderId());
//            }
////            if(pgOrderDTO.getDeliveryStatus().equals(DeliveryStatusEnum.FINSH.getCode())){
//            if(pgOrderDTO.getOrderStatus().equals(PgOrderEnum.RECEIVE.getCode())){
//                if(!map.containsKey(ErrorEnum.ORDER_HAD_DELIVERY)){
//                    map.put(ErrorEnum.ORDER_HAD_DELIVERY, new ArrayList<>());
//                }
//                map.get(ErrorEnum.ORDER_HAD_DELIVERY).add(pgOrderDTO.getOrderId());
//            }
//            orderItemList.addAll(pgOrderDTO.getOrderItemList());
//            pgOrderDTO.setOrderStatus(PgOrderEnum.CANCELED.getCode());
//            BeanUtils.copyProperties(pgOrderDTO, pgOrderMaster);
//            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
//            if(result<=0)
//                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
//        }
//        if(map.size()>0){
//            throw new MadaoException(ErrorEnum.ORDER_CANCEL_FAIL, getResultViewList(map));
//        }
//        //加库存
//        increaseStock(orderItemList);
//        return pgOrderDTOList;
//    }


    //商家设置配送时间 TODO 检查商家权限
    public List<String> updateDeliveryTime(Long shopId, List<String> orderIdList, Long date1) {
        //判断时间是否合法
        Date deliveryDate = new Date(date1);
        if (deliveryDate.before(new Date())) {
            Map<String, String> map = new HashMap<>();
            map.put("date", "时间不能是当前时间之前");
            throw new MadaoException(ErrorEnum.PARAM_ERROR, map);
        }

        List<PgOrderMaster> pgOrderMasterList = getPgOrderMasterByIdList(orderIdList);
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for (PgOrderMaster pgOrderMaster : pgOrderMasterList) {
            //订单状态为已完成，不能设置配送时间
            if (pgOrderMaster.getOrderStatus().equals(PgOrderEnum.FINISHED.getCode())) {
                if (!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderMaster.getOrderId());
            }
            pgOrderMaster.setDeliveryTime(deliveryDate);
            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if (map.size() > 0)
            throw new MadaoException(ErrorEnum.SET_DELIVERY_TIME_FAIL, getResultViewList(map));
        return orderIdList;
    }

    //用户确认收货 拼购订单  订单完成
    @Transactional
    public void confirmPg(Long userId, List<String> orderIdList){
        List<PgOrderMaster> pgOrderMasterList = getPgOrderMasterByIdList(orderIdList);
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for(PgOrderMaster pgOrderMaster: pgOrderMasterList){
            //判断订单是否属于该用户
            if(!pgOrderMaster.getUserId().equals(userId)){
                log.error("【异常】={}", ErrorEnum.ORDER_OWNER_ERROR.getMessage() );
                if(!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR)){
                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_OWNER_ERROR).add(pgOrderMaster.getOrderId());
            }

            //订单状态为新订单，即为付款，抛出异常
            if(pgOrderMaster.getOrderStatus().equals(PgOrderEnum.NEW.getCode())){
                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR.getMessage() + pgOrderMaster.getOrderId());
                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderMaster.getOrderId());
            }
            pgOrderMaster.setOrderStatus(PgOrderEnum.FINISHED.getCode());
            pgOrderMaster.setReceiveTime(new Date());
            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if(map.size()>0){
            throw new MadaoException(ErrorEnum.CONFIRM_RECEIVE_FAIL, getResultViewList(map));
        }
    }


    //用户设置拼购订单备注
    public void setPgOrderRemark(Long userId, String orderId, String remark) {
        PgOrderMaster pgOrderMaster = pgOrderMasterMapper.selectByPrimaryKey(orderId);
        if (pgOrderMaster == null)
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, orderId);
        if (!pgOrderMaster.getUserId().equals(userId))
            throw new MadaoException(ErrorEnum.ORDER_OWNER_ERROR, IdResultMap.getIdMap(orderId));
        //订单状态为已完成则不能加备注
        if (pgOrderMaster.getOrderStatus().equals(PgOrderEnum.FINISHED.getCode()))
            throw new MadaoException(ErrorEnum.ORDER_STATUS_ERROR, IdResultMap.getIdMap(orderId));
        pgOrderMaster.setOrderRemark(remark);
        int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
        if(result<=0)
            throw new MadaoException(ErrorEnum.OPERATION_FAIL);
    }

    //商家确认送达 TODO
    public void confirmArriveByShopPg(Long shopId, List<String> orderIdList, Long arrive) {
        Date date = new Date(arrive);
        List<PgOrderMaster> pgOrderMasterList = getPgOrderMasterByIdList(orderIdList);
        for (PgOrderMaster pgOrderMaster : pgOrderMasterList) {
            pgOrderMaster.setArriveTime(date);
            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
    }

    //根据订单id获取订单列表
    public List<PgOrderDTO> getPgOrderDTObyIdList(List<String> orderIdList) {
        List<PgOrderDTO> pgOrderDTOList = commonMapper.getPgOrderByOrderIdList(orderIdList);
        if (pgOrderDTOList.size() != orderIdList.size()) {
            List<String> id = new LinkedList<>();
            id.addAll(orderIdList);
            for (PgOrderDTO pgorderDTO : pgOrderDTOList)
                id.remove(pgorderDTO.getOrderId());
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(id));
        }
        return pgOrderDTOList;
    }

    //根据订单id获取订单列表
    public List<PgOrderMaster> getPgOrderMasterByIdList(List<String> orderIdList) {
        PgOrderMasterExample example = new PgOrderMasterExample();
        PgOrderMasterExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdIn(orderIdList);
        List<PgOrderMaster> pgOrderMasterList = pgOrderMasterMapper.selectByExample(example);
        if (pgOrderMasterList.size() != orderIdList.size()) {
            List<String> id = new LinkedList<>();
            id.addAll(orderIdList);
            for (PgOrderMaster pgOrderMaster : pgOrderMasterList) {
                id.remove(pgOrderMaster.getOrderId());
            }
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(id));
        }
        return pgOrderMasterList;
    }

    //商家设置订单为配送中 Todo 验证拼购商家
    public void setOrderDeliveryStatusSending(OrderDeliveryStatusForm form) {

        Map<ErrorEnum, List<String>> map = new HashMap<>();
        List<PgOrderMaster> pgOrderMasterList = getPgOrderMasterByIdList(form.getOrderId());
        for(PgOrderMaster pgOrderMaster: pgOrderMasterList){
            if(!pgOrderMaster.getOrderStatus().equals(PgOrderEnum.PAYED.getCode())){
                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(pgOrderMaster.getOrderId());
            }
            pgOrderMaster.setOrderStatus(PgOrderEnum.WAIT_RECEIVE.getCode());
            pgOrderMaster.setDeliveryStartTime(new Date());
            int result = pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if(map.size()>0)
            throw new MadaoException(ErrorEnum.ORDER_SET_DELIVERY_FAIL, getResultViewList(map));
    }

    //查询待收货的订单，如果是待收货，未取消状态 且经过七天，设为已完成
    public void updatePgOrderStatus(){
        PgOrderMasterExample example = new PgOrderMasterExample();
        PgOrderMasterExample.Criteria criteria = example.createCriteria();
        criteria.andOrderStatusEqualTo(PgOrderEnum.WAIT_RECEIVE.getCode());
        criteria.andCancelStatusEqualTo(CancelStatusEnum.NO_CANCEL.getCode());
        List<PgOrderMaster> pgOrderMasterList = pgOrderMasterMapper.selectByExample(example);
        for(PgOrderMaster pgOrderMaster: pgOrderMasterList){
            if(pgOrderMaster.getDeliveryStartTime()!=null) {
                if (new Date().getTime() - pgOrderMaster.getDeliveryStartTime().getTime() > 604800000L) {
                    pgOrderMaster.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
                    pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
                }
            }
        }
    }


    //退款
    public void pgOrderRefund(RefundForm form){
        PgOrderMaster pgOrderMaster = pgOrderMasterMapper.selectByPrimaryKey(form.getOrderId());
        if(pgOrderMaster==null){
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(form.getOrderId()));
        }
        if(!pgOrderMaster.getRefundStatus().equals(RefundStatusEnum.NOT_REFUND.getCode())){
            throw  new MadaoException(ErrorEnum.ORDER_STATUS_ERROR, IdResultMap.getIdMap(form.getOrderId()));
        }
        //TODO 退款操作
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", AlipayConfig.partner,  AlipayConfig.private_key,"json","UTF-8",AlipayConfig.ali_public_key,"RSA2");
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + pgOrderMaster.getOrderId() + "\"," +
                "\"refund_amount\":"+ pgOrderMaster.getOrderAmount().doubleValue() + "," +
                "\"refund_currency\":\"USD\"," +
                "\"refund_reason\":\"正常退款\"," +
                "  }");
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new MadaoException(ErrorEnum.REFUND_FAIL, IdResultMap.getIdMap(form.getOrderId()));
        }
        if(response.isSuccess()){
            pgOrderMaster.setRefundStatus(RefundStatusEnum.REFUNDED.getCode());
            pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            throw new MadaoException(ErrorEnum.REFUND_FAIL, IdResultMap.getIdMap(form.getOrderId()));
        }
    }

    //拼购订单支付后的结果通知
    public void payCallBack(OrderPayBackForm form) {
        PgOrderMaster pgOrderMaster = pgOrderMasterMapper.selectByPrimaryKey(form.getOrderId());
        if(pgOrderMaster==null)
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(form.getOrderId()));
        if(!pgOrderMaster.getUserId().equals(form.getUserId()))
            throw new MadaoException(ErrorEnum.ORDER_OWNER_ERROR, IdResultMap.getIdMap(form.getOrderId()));
        if(!pgOrderMaster.getOrderStatus().equals(OrderStatusEnum.NEW.getCode()) || !pgOrderMaster.getCancelStatus().equals(CancelStatusEnum.NO_CANCEL.getCode())){
            throw new MadaoException(ErrorEnum.ORDER_STATUS_ERROR, IdResultMap.getIdMap(form.getOrderId()));
        }

        //如果订单未支付，则该订单为无效，将其删除
        if (form.getFlag().compareTo((byte) 0)==0){
            //退库存的操作
            OrderItemExample orderItemExample = new OrderItemExample();
            OrderItemExample.Criteria criteria = orderItemExample.createCriteria();
            criteria.andOrderIdEqualTo(form.getOrderId());
            List<OrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
            if (orderItemList.size()>0) {
                increaseStock(orderItemList);
            }

            //删除订单及订单项
            pgOrderMasterMapper.deleteByPrimaryKey(pgOrderMaster.getOrderId());
            orderItemMapper.deleteByExample(orderItemExample);
        }else{
            if (form.getPayType()==null || form.getPayAccount()==null){
                Map<String, String> map = new HashMap<>();
                if (form.getPayType()==null)
                    map.put("payType", "支付类型不能为空");
                if (form.getPayAccount()==null)
                    map.put("payAccount", "支付帐号不能为空");
                throw new MadaoException(ErrorEnum.PARAM_ERROR, map);
            }
            // 如果订单支付成功，更新信息。订单状态的改变在支付宝异步通知中进行
            pgOrderMaster.setPayWay(form.getPayType());
            pgOrderMaster.setPayAcount(form.getPayAccount());
            pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
        }
    }
}