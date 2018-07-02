package cn.haizhi.market.main.service.madao;

import cn.haizhi.market.main.bean.madao.*;
import cn.haizhi.market.main.bean.qiyuan.UserAddress;
import cn.haizhi.market.main.bean.richard.Product;
import cn.haizhi.market.main.mapper.madao.*;
import cn.haizhi.market.main.mapper.richard.ProductMapper;
import cn.haizhi.market.other.enums.madao.*;
import cn.haizhi.market.other.enums.richard.ShopStateEnum;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.form.madao.OrderCreateForm;
import cn.haizhi.market.other.form.madao.OrderDeliveryStatusForm;
import cn.haizhi.market.other.form.madao.OrderPayBackForm;
import cn.haizhi.market.other.form.madao.OrderSignForm;
import cn.haizhi.market.other.util.IdResultMap;
import cn.haizhi.market.other.util.KeyUtil;
import cn.haizhi.market.other.util.madao.payUtils.OrderInfoUtil2_0;
import com.alipay.config.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.haizhi.market.other.util.ErrorResultUtil.getResultViewList;
import static cn.haizhi.market.other.util.ErrorResultUtil.getResultViewListWithName;

@Service
@Transactional
@Slf4j
public class OrderService {
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMasterMapper orderMasterMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private PgOrderMasterMapper pgOrderMasterMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CartItemJedisService cartItemJedisService;

    @Autowired
    private OrderPayRecordMapper orderPayRecordMapper;

    private String url = "120.78.87.169:8080";

    //添加普通订单
    public List<OrderDTO> addOrder(OrderCreateForm orderForm, Long userId) {
        //判断传入的地址id是否可用
        UserAddress address = commonMapper.getUserAddressById(orderForm.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            log.error("用户地址信息错误-------------{}", address);
            throw new MadaoException(ErrorEnum.ADDRESS_ERROR, IdResultMap.getIdMap(orderForm.getAddressId()));
        }

        //获取该订单的所有购物车项
        List<CartItemDTO> cartItemDTOList = cartItemJedisService.getCartItemDTOByCartItemIdList(userId, orderForm.getCartItemIdList());
        //在cartItemJedisService中已经检查购物车id是否有效

//      购物车在数据库的时候的版本
//        List<CartItemDTO> cartItemDTOList1 = commonMapper.getCartItemDTOByCartItemIdList(orderForm.getCartItemIdList());
//        //如果从订单表中查出的数据数量和传入的id数量不符合，说明传入的id不正确，抛出异常并返回查不出的项的id
//        if (orderForm.getCartItemIdList().size() != cartItemDTOList.size()) {
//            List<String> id = new LinkedList<>();
//            id.addAll(orderForm.getCartItemIdList());
//            for (CartItemDTO cartItemDTO : cartItemDTOList) {
//                id.remove(cartItemDTO.getItemId());
//            }
//            throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(id));
//        }

        //得到 商铺id--购物项列表的键值对
        Map<Long, List<CartItemDTO>> shopIdCartItemDTOMap = new HashMap<>();
        for (CartItemDTO cartItemDTO : cartItemDTOList) {
            //根据商铺id将购物车分类加入map
            Long shopId = cartItemDTO.getShopId();
            if (!shopIdCartItemDTOMap.containsKey(shopId)) {
                shopIdCartItemDTOMap.put(shopId, new ArrayList<>());
            }
            shopIdCartItemDTOMap.get(shopId).add(cartItemDTO);
        }

        String subject = "";
        String body = "";

        List<OrderDTO> orderDTOList = new ArrayList<>();
        //处理普通商品订单
        if (shopIdCartItemDTOMap.size() > 0) {
            //循环获取每个商家id对应的购物车项
            ArrayList<CartItemDTO> listAll = new ArrayList<>();
            //记录下不符合商店最低配送价格的购物项id列表和商店状态表
            Map<ErrorEnum, List<String>> map = new HashMap<>();
            for (Map.Entry entry : shopIdCartItemDTOMap.entrySet()) {
                OrderDTO orderDTO = new OrderDTO();
                orderDTO.setOrderItemList(new ArrayList<>());
                List<CartItemDTO> list = (List<CartItemDTO>) entry.getValue();
                listAll.addAll(list);
                String orderId = KeyUtil.genUniquKey();
                BigDecimal productAmount = BigDecimal.ZERO;
                Long shopId = list.get(0).getShopId();
                //获得该商店配送的基本信息
                BaseShop baseShop = commonMapper.getBaseShopById(shopId);
                //如果商店是关闭状态，加入异常
                boolean flag = false;
                if (baseShop.getShopState().byteValue() == ShopStateEnum.IS_CLOSED.getCode().byteValue()) {
                    flag = true;
                    if (!map.containsKey(ErrorEnum.SHOP_CLOSE)) {
                        map.put(ErrorEnum.SHOP_CLOSE, new ArrayList<>());
                    }
                    map.get(ErrorEnum.SHOP_CLOSE).add(new Long(baseShop.getShopId()).toString());
                }

                if (flag)
                    continue;

                //循环每个购物车项，转为订单项，插入数据库并组装成一个订单主项
                for (CartItemDTO cartItemDTO : list) {
                    BigDecimal price = BigDecimal.ZERO;
                    if(cartItemDTO.getDiscountState()==DiscountStateEnum.ON_DISCOUNT.getCode().intValue()){
                        price = cartItemDTO.getDiscountPrice();
                    }else {
                        price = cartItemDTO.getProductPrice();
                    }
                    productAmount = productAmount.add(price.multiply(BigDecimal.valueOf(cartItemDTO.getProductQuantity())));
                    OrderItem orderItem = new OrderItem();
                    BeanUtils.copyProperties(cartItemDTO, orderItem);
                    orderItem.setProductUnit(cartItemDTO.getLimitNumber());
                    orderItem.setItemId(KeyUtil.genUniquKey());
                    orderItem.setProductPrice(price);
                    orderItem.setOrderId(orderId);
                    int result = orderItemMapper.insertSelective(orderItem);
                    if(result<=0)
                        throw new MadaoException(ErrorEnum.OPERATION_FAIL);
                    orderDTO.getOrderItemList().add(orderItem);

                    subject+=orderItem.getProductName() + " ";
                    body += orderItem.getProductDesc() + " ";

                }

                //如果达不到最低的配送价格，就加入异常
                System.out.println(productAmount);
                if (baseShop.getLimitPrice().compareTo(productAmount) > 0) {
                    flag = true;
                    if (!map.containsKey(ErrorEnum.ORDER_LIMIT_ERROR)) {
                        map.put(ErrorEnum.ORDER_LIMIT_ERROR, new ArrayList<>());
                    }
                    map.get(ErrorEnum.ORDER_LIMIT_ERROR).add(String.valueOf((Long) entry.getKey()));
                }
                if(flag)
                    continue;

                OrderMaster orderMaster = new OrderMaster();
                orderMaster.setShopId(shopId);
                orderMaster.setShopName(baseShop.getShopName());
                orderMaster.setUserId(userId);
                orderMaster.setUserName(address.getConsignee());
                orderMaster.setUserPhone(address.getPhone());
                orderMaster.setUserAddress(address.getUserAddress());
                orderMaster.setProductAmount(productAmount);
                orderMaster.setSendPrice(baseShop.getSendPrice());
                orderMaster.setOrderAmount(productAmount.add(baseShop.getSendPrice()));
                orderMaster.setOrderId(orderId);

                //TODO
//                Double price = orderMaster.getOrderAmount().doubleValue();
//                String orderInfo = OrderInfoUtils.getOrderInfo(subject, body, price.toString(), orderId);
//                String sign = SignUtils.getSign(orderInfo);
//                orderDTO.setSign(sign);
                int result = orderMasterMapper.insertSelective(orderMaster);
                if(result<=0)
                    throw new MadaoException(ErrorEnum.OPERATION_FAIL);
                BeanUtils.copyProperties(orderMaster, orderDTO);
                orderDTOList.add(orderDTO);
            }

            if (map.size() > 0) {
                throw new MadaoException(ErrorEnum.ORDER_CREATE_ERROR, getResultViewListWithName("shopId", map));
            }
            //扣库存
            decreaseStock(listAll);
            //调用删除购物车的方法
            cartItemJedisService.deleteCartItemList(userId, orderForm.getCartItemIdList());

        }
        return orderDTOList;
    }


    //根据购物车项列表扣库存
    public void decreaseStock(List<CartItemDTO> cartItemDTOList) {
        //存放扣库存时错误的商品的  错误信息---购物项 键值对
        Map<ErrorEnum, List<String>> map = new HashMap<>();

        for (CartItemDTO cartItemDTO : cartItemDTOList) {
            Product product = productMapper.selectByPrimaryKey(cartItemDTO.getProductId());
            if (product == null) {
                if (!map.containsKey(ErrorEnum.PRODUCT_NOT_EXIST)) {
                    map.put(ErrorEnum.PRODUCT_NOT_EXIST, new ArrayList<>());
                }
                map.get(ErrorEnum.PRODUCT_NOT_EXIST).add(cartItemDTO.getItemId());
            }
            if (product.getProductState().intValue()!=ProductStateEnum.ON_SALE.getCode().intValue()) {
                if (!map.containsKey(ErrorEnum.PRODUCT_DOWN)) {
                    map.put(ErrorEnum.PRODUCT_DOWN, new ArrayList<>());
                }
                map.get(ErrorEnum.PRODUCT_DOWN).add(cartItemDTO.getItemId());
            }
            Integer result = product.getProductStock() - cartItemDTO.getProductQuantity();
            if (result < 0) {
                log.error("【商品库存不足】-------Need={}---------product={}", cartItemDTO.getProductQuantity(), product);
                if (!map.containsKey(ErrorEnum.PRODUCT_STOCK_ERROR)) {
                    map.put(ErrorEnum.PRODUCT_STOCK_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.PRODUCT_STOCK_ERROR).add(cartItemDTO.getItemId());
            }

            product.setProductStock(result);
            log.info("【扣库存成功】------------Need={}-----------product={}", cartItemDTO.getProductQuantity(), product);
            int result1 = productMapper.updateByPrimaryKey(product);
            if(result1<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if (map.size() > 0) {
            throw new MadaoException(ErrorEnum.ORDER_CREATE_ERROR, getResultViewList(map));
        }
    }

    //根据条件获取订单
    public List<OrderDTO> getOrderDTO(Long shopId, Long userId, Byte orderStatus, Byte commentStatus, Byte cancelStatus, Byte refundStatus) {
        return commonMapper.getOrderDTOByUserId(shopId, userId, orderStatus, commentStatus, cancelStatus, refundStatus);
    }


    //用户支付普通订单
//    public void payOrder(Long userId, List<String> orderIdList) {
//        List<OrderMaster> orderMasterList = getOrderMaserByIdList(orderIdList);
//        BigDecimal amount = BigDecimal.ZERO;
//        //Todo 批处理
//        // 得出所有异常，一次返回
//        Map<ErrorEnum, List<String>> map = new HashMap<>();
//        for (OrderMaster orderMaster : orderMasterList) {
//            if (!orderMaster.getUserId().equals(userId)) {
//                if (!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR)) {
//                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
//                }
//                map.get(ErrorEnum.ORDER_OWNER_ERROR).add(orderMaster.getOrderId());
//                log.error("【异常】={}", ErrorEnum.ORDER_OWNER_ERROR.getMessage());
//            }
//            if (!orderMaster.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())) {
//                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR.getMessage());
//                if (!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR)) {
//                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
//                }
//                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(orderMaster.getOrderId());
//            }
//            if (!orderMaster.getPayStatus().equals(PayStatusEnum.WAIT.getCode())) {
//                log.error("【异常】={}", ErrorEnum.ORDER_PAY_STATUS_ERROR.getMessage());
//                if (!map.containsKey(ErrorEnum.ORDER_PAY_STATUS_ERROR)) {
//                    map.put(ErrorEnum.ORDER_PAY_STATUS_ERROR, new ArrayList<>());
//                }
//                map.get(ErrorEnum.ORDER_PAY_STATUS_ERROR).add(orderMaster.getOrderId());
//            }
//            amount = amount.add(orderMaster.getOrderAmount());
//        }
//        //如果有错误，抛出异常
//        if (map.size() != 0) {
//            throw new MadaoException(ErrorEnum.ORDER_PAY_FAIL, getResultViewList(map));
//        }
//
//        //Todo 支付
//
//
//        //更新订单
//        for (OrderMaster orderMaster : orderMasterList) {
//            orderMaster.setPayStatus(PayStatusEnum.SUCCESS.getCode());
//            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
//            if(result<=0)
//                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
//
//        }
//    }


    //用户发起取消普通订单
    public List<OrderMaster> cancelOrderByUser(Long userId, List<String> orderIdList) {
        List<OrderMaster> orderMasterList = getOrderMaserByIdList(orderIdList);

        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for (OrderMaster orderMaster : orderMasterList) {
            if (!orderMaster.getUserId().equals(userId)) {
                log.error("【异常】={}", ErrorEnum.ORDER_OWNER_ERROR.getMessage());
                if (!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR))
                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_OWNER_ERROR).add(orderMaster.getOrderId());
            }
            if (!orderMaster.getCancelStatus().equals(CancelStatusEnum.NO_CANCEL.getCode())) {
                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR.getMessage());
                if (!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(orderMaster.getOrderId());
            }
            //如果送达已经超过一天，不能退货

            //依次按照商家确认到达时间、商家配送时间、用户确认到达时间作为订单是否过了退单期限的依据

            Date date = null;
            if(orderMaster.getArriveTime()!=null)
                date = orderMaster.getArriveTime();
            else if(orderMaster.getDeliveryTime()!=null)
                date = orderMaster.getDeliveryTime();
            else
                date = orderMaster.getReceiveTime();
            //如果时间都为空，加入异常,如果不为空，判断是否过期
            if(date==null){
                if(!map.containsKey(ErrorEnum.ORDER_DATE_ERROR)){
                    map.put(ErrorEnum.ORDER_DATE_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_DATE_ERROR).add(orderMaster.getOrderId());
            }else if (new Date().getTime() - date.getTime() > 86400000L) {
                if (!map.containsKey(ErrorEnum.ORDER_HAD_OVERDUE))
                    map.put(ErrorEnum.ORDER_HAD_OVERDUE, new ArrayList<>());
                map.get(ErrorEnum.ORDER_HAD_OVERDUE).add(orderMaster.getOrderId());
            }



//            orderMaster.setOrderStatus(OrderStatusEnum.TO_CANCEL.getCode());
            orderMaster.setCancelStatus(CancelStatusEnum.TO_CANCEL.getCode());
            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }

        if (map.size() > 0) {
            throw new MadaoException(ErrorEnum.ORDER_CANCEL_FAIL, getResultViewList(map));
        }
        return orderMasterList;
    }

    //商家确认取消普通订单  TODO 退款
    public List<OrderDTO> confirmCancel(Long shopId, List<String> orderIdList) {
        List<OrderDTO> orderDTOList = getOrderDTObyIdList(orderIdList);
        OrderMaster orderMaster = new OrderMaster();
        List<OrderItem> orderItemList = new ArrayList<>();
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for (OrderDTO orderDTO : orderDTOList) {
            if (!orderDTO.getShopId().equals(shopId)) {
                if (!map.containsKey(ErrorEnum.ORDER_SHOP_OWNER_ERROR)) {
                    map.put(ErrorEnum.ORDER_SHOP_OWNER_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_SHOP_OWNER_ERROR).add(orderDTO.getOrderId());
            }
            if (!orderDTO.getCancelStatus().equals(CancelStatusEnum.TO_CANCEL.getCode())) {
                if (!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(orderDTO.getOrderId());
            }
//            orderDTO.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
            orderDTO.setCancelStatus(CancelStatusEnum.CANCELED.getCode());
            BeanUtils.copyProperties(orderDTO, orderMaster);
            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
            orderItemList.addAll(orderDTO.getOrderItemList());
        }
        if (map.size() > 0)
            throw new MadaoException(ErrorEnum.CONFIRM_CANCEL_FAIL, getResultViewList(map));
        //加库存
        increaseStock(orderItemList);
        return orderDTOList;
    }


    //退单后加库存操作
    public void increaseStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            commonMapper.increaseStock(orderItem.getProductId(), orderItem.getProductQuantity());
        }
    }

    //商家取消普通订单  只能取消未付款订单
    public List<OrderDTO> cancelOrderByShop(Long shopId, List<String> orderIdList) {
        List<OrderDTO> orderDTOList = getOrderDTObyIdList(orderIdList);
        List<OrderItem> orderItemList = new ArrayList<>();

        //异常检测
        OrderMaster orderMaster = new OrderMaster();
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for (OrderDTO orderDTO : orderDTOList) {
            if (!orderDTO.getShopId().equals(shopId)) {
                log.error("【异常】={}", ErrorEnum.ORDER_SHOP_OWNER_ERROR.getMessage());
                if (!map.containsKey(ErrorEnum.ORDER_SHOP_OWNER_ERROR)) {
                    map.put(ErrorEnum.ORDER_SHOP_OWNER_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_SHOP_OWNER_ERROR).add(orderDTO.getOrderId());
            }
            if (!orderDTO.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())) {
                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR.getMessage() + orderDTO.getOrderId());
                if (!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(orderDTO.getOrderId());
            }
//            if(orderDTO.getArriveTime().getTime()-new Date().getTime()>86400000L){
//                if(!map.containsKey(ErrorEnum.ORDER_HAD_OVERDUE))
//                    map.put(ErrorEnum.ORDER_HAD_OVERDUE, new ArrayList<>());
//                map.get(ErrorEnum.ORDER_HAD_OVERDUE).add(orderDTO.getOrderId());
//            }
            //如果已经送货完成，不能取消订单  todo  不确定
            if (orderDTO.getOrderStatus().equals(OrderStatusEnum.FINISHED.getCode())) {
                if (!map.containsKey(ErrorEnum.ORDER_HAD_DELIVERY)) {
                    map.put(ErrorEnum.ORDER_HAD_DELIVERY, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_HAD_DELIVERY).add(orderDTO.getOrderId());
            }
//            orderDTO.setOrderStatus(OrderStatusEnum.CANCEL.getCode());
            orderDTO.setCancelStatus(CancelStatusEnum.CANCELED.getCode());
            orderItemList.addAll(orderDTO.getOrderItemList());
            BeanUtils.copyProperties(orderDTO, orderMaster);
            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);

        }
        //发现异常则组装后抛出
        if (map.size() > 0)
            throw new MadaoException(ErrorEnum.ORDER_CANCEL_FAIL, getResultViewList(map));

//        OrderMaster orderMaster = new OrderMaster();
//        for(OrderDTO orderDTO: orderDTOList){
//            if(orderDTO.getPayStatus().equals(PayStatusEnum.SUCCESS.getCode())) {
//                //Todo  退款
//
//                orderDTO.setPayStatus(PayStatusEnum.WAIT.getCode());
//            }
//
//
//            BeanUtils.copyProperties(orderDTO, orderMaster);
//            orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
//        }
        //加库存
        increaseStock(orderItemList);
        return orderDTOList;
    }


    //商家退款
    public void refund(Long shopId, List<String> orderIdList) {
        //Todo
    }

    //商家设置配送时间
    public List<String> updateDeliveryTime(Long shopId, List<String> orderIdList, Long date1) {
        //判断时间是否合法
        Date deliveryDate = new Date(date1);
        if (deliveryDate.before(new Date())) {
            Map<String, String> map = new HashMap<>();
            map.put("date", "时间不能是当前时间之前");
            throw new MadaoException(ErrorEnum.PARAM_ERROR, map);
        }

        List<OrderMaster> orderMasterList = getOrderMaserByIdList(orderIdList);
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for (OrderMaster orderMaster : orderMasterList) {
            if (!orderMaster.getShopId().equals(shopId)) {
                if (!map.containsKey(ErrorEnum.ORDER_SHOP_OWNER_ERROR))
                    map.put(ErrorEnum.ORDER_SHOP_OWNER_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_SHOP_OWNER_ERROR).add(orderMaster.getOrderId());
                log.error("【异常】={}", ErrorEnum.ORDER_SHOP_OWNER_ERROR.getMessage());
            }
//            if (!orderMaster.getDeliveryStatus().equals(DeliveryStatusEnum.WAIT.getCode())) {
//                if (!map.containsKey(ErrorEnum.ORDER_HAD_DELIVERY))
//                    map.put(ErrorEnum.ORDER_HAD_DELIVERY, new ArrayList<>());
//                map.get(ErrorEnum.ORDER_HAD_DELIVERY).add(orderMaster.getOrderId());
//            }
            if (orderMaster.getOrderStatus().equals(OrderStatusEnum.FINISHED.getCode())) {
                if (!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(orderMaster.getOrderId());
            }
            orderMaster.setDeliveryTime(deliveryDate);
            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if (map.size() > 0)
            throw new MadaoException(ErrorEnum.SET_DELIVERY_TIME_FAIL, getResultViewList(map));
        return orderIdList;
    }


    //设置订单评价         供调用
    public void setComment(Long userId, String orderId, Long commentId) {
        OrderMaster orderMaster = orderMasterMapper.selectByPrimaryKey(orderId);
        log.info(orderMaster.toString());
        if (!orderMaster.getUserId().equals(userId)) {
            log.error("【异常】={}" + ErrorEnum.ORDER_OWNER_ERROR.getMessage());
            throw new MadaoException(ErrorEnum.ORDER_OWNER_ERROR, orderMaster.getOrderId());
        }
        if (!orderMaster.getOrderStatus().equals(OrderStatusEnum.FINISHED.getCode())) {
            log.error("【异常】={}" + ErrorEnum.ORDER_NOT_FINISH.getMessage());
            throw new MadaoException(ErrorEnum.ORDER_NOT_FINISH, orderMaster.getOrderId());
        }
        if (!orderMaster.getCommentStatus().equals(CommentStatusEnum.WAIT.getCode())) {
            log.error("【异常】={}" + ErrorEnum.ORDER_HAD_COMMENT.getMessage());
            throw new MadaoException(ErrorEnum.ORDER_HAD_COMMENT, orderMaster.getOrderId());
        }
        orderMaster.setCommentId(commentId);
        orderMaster.setCommentStatus(CommentStatusEnum.FINISH.getCode());
        int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
        if(result<=0)
            throw new MadaoException(ErrorEnum.OPERATION_FAIL);
    }

    //用户确认收货普通订单
    public void confirm(Long userId, List<String> orderIdList) {
        List<OrderMaster> orderMasterList = getOrderMaserByIdList(orderIdList);
        //异常检测

        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for (OrderMaster orderMaster : orderMasterList) {
            if (!orderMaster.getUserId().equals(userId)) {
                log.error("【异常】={}", ErrorEnum.ORDER_OWNER_ERROR.getMessage());
                if (!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR)) {
                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_OWNER_ERROR).add(orderMaster.getOrderId());
            }
            if (orderMaster.getOrderStatus().equals(OrderStatusEnum.FINISHED.getCode())) {
                log.error("【异常】={}", ErrorEnum.ORDER_STATUS_ERROR.getMessage() + orderMaster.getOrderId());
                if (!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR))
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(orderMaster.getOrderId());
            }
//            orderMaster.setDeliveryStatus(DeliveryStatusEnum.FINSH.getCode());
//            orderMaster.setOrderStatus(OrderStatusEnum.FINISH.getCode());
            orderMaster.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
            orderMaster.setReceiveTime(new Date());
            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if (map.size() > 0)
            throw new MadaoException(ErrorEnum.CONFIRM_RECEIVE_FAIL, getResultViewList(map));
    }


    //商家确认订单送达并设置配送时间
    public void confirmArriveByShop(Long shopId, List<String> orderIdList, Long arrive) {
        Date date = new Date(arrive);
        List<OrderMaster> orderMasterList = getOrderMaserByIdList(orderIdList);
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        for (OrderMaster orderMaster : orderMasterList) {
            if (!orderMaster.getShopId().equals(shopId)) {
                if (!map.containsKey(ErrorEnum.ORDER_SHOP_OWNER_ERROR)) {
                    map.put(ErrorEnum.ORDER_SHOP_OWNER_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_SHOP_OWNER_ERROR).add(orderMaster.getOrderId());
            }
            orderMaster.setArriveTime(date);
//            orderMaster.setDeliveryStatus(DeliveryStatusEnum.FINSH.getCode());
            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);

        }
        if (map.size() > 0) {
            throw new MadaoException(ErrorEnum.CONFIRM_RECEIVE_FAIL, getResultViewList(map));
        }
    }






    //用户设置普通订单留言
    public void setOrderRemark(Long userId, String orderId, String remark) {
        OrderMaster orderMaster = orderMasterMapper.selectByPrimaryKey(orderId);
        if (orderMaster == null)
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, orderId);
        if (!orderMaster.getUserId().equals(userId))
            throw new MadaoException(ErrorEnum.ORDER_OWNER_ERROR, IdResultMap.getIdMap(orderId));
        if (!(orderMaster.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())|| orderMaster.getOrderStatus().equals(OrderStatusEnum.PAYED.getCode())))
            throw new MadaoException(ErrorEnum.ORDER_STATUS_ERROR, IdResultMap.getIdMap(orderId));
        orderMaster.setOrderRemark(remark);
        int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
        if(result<=0)
            throw new MadaoException(ErrorEnum.OPERATION_FAIL);
    }

    public void setOrderDeliveryStatusSending(OrderDeliveryStatusForm form) {
        List<String> id = null;
        List<OrderMaster> orderMasterList = getOrderMaserByIdList(form.getOrderId());
        for(OrderMaster orderMaster: orderMasterList){
            if(!orderMaster.getShopId().equals(form.getShopId())){
                if (id==null)
                    id = new ArrayList<>();
                id.add(orderMaster.getOrderId());
                continue;
            }
//            orderMaster.setDeliveryStatus(DeliveryStatusEnum.DELIVERYING.getCode());
            orderMaster.setOrderStatus(OrderStatusEnum.WAIT_RECEIVE.getCode());
            orderMaster.setDeliveryStartTime(new Date());
            int result = orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            if(result<=0)
                throw new MadaoException(ErrorEnum.OPERATION_FAIL);
        }
        if(id!=null)
            throw new MadaoException(ErrorEnum.ORDER_SHOP_OWNER_ERROR, IdResultMap.getIdMap(id));
    }



    public List<OrderMaster> getOrderMaserByIdList(List<String> orderIdList) {
        OrderMasterExample example = new OrderMasterExample();
        OrderMasterExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdIn(orderIdList);
        List<OrderMaster> orderMasterList = orderMasterMapper.selectByExample(example);
        if (orderMasterList.size() != orderIdList.size()) {
            List<String> id = new LinkedList<>();
            id.addAll(orderIdList);
            for (OrderMaster orderMaster : orderMasterList)
                id.remove(orderMaster.getOrderId());
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(id));
        }
        return orderMasterList;
    }



    public List<OrderDTO> getOrderDTObyIdList(List<String> orderIdList) {
        List<OrderDTO> orderDTOList = commonMapper.getOrderByOrderIdList(orderIdList);
        if (orderDTOList.size() != orderIdList.size()) {
            List<String> id = new LinkedList<>();
            id.addAll(orderIdList);
            for (OrderDTO orderDTO : orderDTOList)
                id.remove(orderDTO.getOrderId());
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(id));
        }
        return orderDTOList;
    }

    //查询待收货的订单，如果是待收货，未取消状态 且经过七天，设为已完成
    public void updateOrderStatus(){
        OrderMasterExample example = new OrderMasterExample();
        OrderMasterExample.Criteria criteria = example.createCriteria();
        criteria.andOrderStatusEqualTo(OrderStatusEnum.WAIT_RECEIVE.getCode());
        criteria.andCancelStatusEqualTo(CancelStatusEnum.NO_CANCEL.getCode());
        List<OrderMaster> orderMasterList = orderMasterMapper.selectByExample(example);
        for(OrderMaster orderMaster: orderMasterList){
            if(orderMaster.getDeliveryStartTime()!=null) {
                if (new Date().getTime() - orderMaster.getDeliveryStartTime().getTime() > 604800000L) {
                    orderMaster.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
                    orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
                }
            }
        }
    }

    //根据传入的订单id列表生成支付所需sign和向数据库写入订单 支付的信息
    public OrderSignInfo getOrderSign(OrderSignForm form){
        Map<ErrorEnum, List<String>> map = new HashMap<>();
        List<OrderDTO> orderDTOList = getOrderDTObyIdList(form.getOrderIdList());
        String subject = "";
        String body = "";
        String orderId = KeyUtil.genUniquKey();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDTO orderDTO: orderDTOList){
            if(!orderDTO.getUserId().equals(form.getUserId())){
                if(!map.containsKey(ErrorEnum.ORDER_OWNER_ERROR)){
                    map.put(ErrorEnum.ORDER_OWNER_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_CREATE_ERROR).add(orderDTO.getOrderId());
            }

            if(!orderDTO.getOrderStatus().equals(OrderStatusEnum.NEW.getCode())){
                if(!map.containsKey(ErrorEnum.ORDER_STATUS_ERROR)){
                    map.put(ErrorEnum.ORDER_STATUS_ERROR, new ArrayList<>());
                }
                map.get(ErrorEnum.ORDER_STATUS_ERROR).add(orderDTO.getOrderId());
            }

            for (OrderItem orderItem: orderDTO.getOrderItemList()){
                subject+=orderItem.getProductName()+"-";
                body += orderItem.getProductDesc()+"-";
            }

            OrderPayRecord orderPayRecord = new OrderPayRecord(form.getUserId(), orderId, orderDTO.getOrderId());
            orderPayRecordMapper.insertSelective(orderPayRecord);

            totalAmount = totalAmount.add(orderDTO.getOrderAmount());
        }


        Double price = totalAmount.doubleValue();
//        String orderInfo = OrderInfoUtils.getOrderInfo(subject, body, price+"", orderId);
//        String sign = SignUtils.getSign(orderInfo);


        Map<String, String> keyValues = new HashMap<String, String>();

        boolean rsa2 = true;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String datetime = sdf.format(new Date());
        keyValues.put("app_id", AlipayConfig.appid);

        keyValues.put("biz_content", "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\""+price+"\",\"subject\":\""+subject+"\",\"body\":\""+body+"\",\"out_trade_no\":\"" + orderId +  "\"}");

        keyValues.put("charset", "utf-8");

        keyValues.put("method", "alipay.trade.app.pay");

        keyValues.put("sign_type", rsa2 ? "RSA2" : "RSA");

        keyValues.put("timestamp", datetime);

        keyValues.put("version", "1.0");

        keyValues.put("notify_url", url + "/market/order/notify");

        String orderParam = OrderInfoUtil2_0.buildOrderParam(keyValues);

        System.out.println("params-----------------------" + keyValues);
        String sign = OrderInfoUtil2_0.getSign(keyValues, AlipayConfig.private_key, true);
        final String orderInfo = orderParam + "&" + sign;

        OrderSignInfo orderSignInfo = new OrderSignInfo(orderId, totalAmount.doubleValue(), orderInfo);
        return  orderSignInfo;
    }

    //支付失败后删除多余的数据  支付成功则更新各个订单的支付信息   订单的状态则等到支付宝异步通知成功后更新
    public void payCallBack(OrderPayBackForm form){
        OrderPayRecordExample example = new OrderPayRecordExample();
        OrderPayRecordExample.Criteria criteria = example.createCriteria();
        criteria.andLeadOrderIdEqualTo(form.getOrderId());
        List<OrderPayRecord> orderPayRecordList = orderPayRecordMapper.selectByExample(example);
        System.out.println("size------------------------------" + orderPayRecordList.size());
        if(orderPayRecordList.size()==0)
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(form.getOrderId()));
        if (orderPayRecordList.size()>0 && !orderPayRecordList.get(0).getUserId().equals(form.getUserId())){
            throw new MadaoException(ErrorEnum.ORDER_OWNER_ERROR, IdResultMap.getIdMap(form.getOrderId()));
        }

        if (form.getFlag().compareTo((byte)0)==0) {
            orderPayRecordMapper.deleteByExample(example);
        }else{
            if (form.getPayType()==null || form.getPayAccount()==null) {
                Map<String, String> map = new HashMap<>();
                if(form.getPayType()==null)
                    map.put("payType", "支付方式为空");
                if(form.getPayAccount()==null)
                    map.put("payAccount", "支付帐号为空");
                throw new MadaoException(ErrorEnum.PARAM_ERROR, map);
            }
            List<String> orderIdList = new ArrayList<>();
            for(OrderPayRecord orderPayRecord: orderPayRecordList)
                orderIdList.add(orderPayRecord.getOrderId());
            List<OrderMaster>  orderMasterList = getOrderMaserByIdList(orderIdList);
            for(OrderMaster orderMaster: orderMasterList){
                orderMaster.setPayWay(form.getPayType());
                orderMaster.setPayAcount(form.getPayAccount());
                orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
            }
        }
    }
}