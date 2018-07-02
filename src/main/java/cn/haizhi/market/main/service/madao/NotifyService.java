package cn.haizhi.market.main.service.madao;

import cn.haizhi.market.main.bean.madao.OrderMaster;
import cn.haizhi.market.main.bean.madao.OrderPayRecord;
import cn.haizhi.market.main.bean.madao.OrderPayRecordExample;
import cn.haizhi.market.main.bean.madao.PgOrderMaster;
import cn.haizhi.market.main.mapper.madao.OrderMasterMapper;
import cn.haizhi.market.main.mapper.madao.OrderPayRecordMapper;
import cn.haizhi.market.main.mapper.madao.PgOrderMasterMapper;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.enums.madao.OrderStatusEnum;
import cn.haizhi.market.other.enums.madao.PgOrderEnum;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.util.IdResultMap;
import com.alipay.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NotifyService {
    @Autowired
    OrderPayRecordMapper orderPayRecordMapper;

    @Autowired
    OrderService orderService;

    @Autowired
    PgOrderMasterMapper pgOrderMasterMapper;

    @Autowired
    OrderMasterMapper orderMasterMapper;

    //检查订单信息是否正确
    public void confirmOrder(Map<String, String> params) {
        OrderPayRecordExample example = new OrderPayRecordExample();
        OrderPayRecordExample.Criteria criteria = example.createCriteria();
        criteria.andLeadOrderIdEqualTo(params.get("out_trade_no"));
        List<OrderPayRecord> orderPayRecordList = orderPayRecordMapper.selectByExample(example);
        if (orderPayRecordList.size() == 0)
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST, IdResultMap.getIdMap(params.get("out_trade_no")));

        List<String> orderIdList = new ArrayList<>();
        for (OrderPayRecord orderPayRecord : orderPayRecordList) {
            orderIdList.add(orderPayRecord.getOrderId());
        }

        List<OrderMaster> orderMasterList = orderService.getOrderMaserByIdList(orderIdList);

        BigDecimal amount = BigDecimal.ZERO;
        //订单在商户网站中已经做过处理
        for (OrderMaster orderMaster : orderMasterList) {
            if (!orderMaster.getOrderStatus().equals(PgOrderEnum.NEW.getCode())) {
                throw new MadaoException(ErrorEnum.ORDER_STATUS_ERROR, orderMaster.getOrderId());
            }
            amount = amount.add(orderMaster.getOrderAmount());
            orderMaster.setOrderStatus(OrderStatusEnum.PAYED.getCode());
            orderMasterMapper.updateByPrimaryKeySelective(orderMaster);
        }

        double total_amount = Double.parseDouble(params.get("total_amount"));
        if (amount.compareTo(new BigDecimal(total_amount)) != 0) {
            throw new MadaoException(ErrorEnum.ORDER_PAY_ERROR);
        }

        String partner = AlipayConfig.partner;
        String seller_id = params.get("seller_id");
        if (!partner.equals(seller_id)) {
            throw new MadaoException(ErrorEnum.ORDER_PAY_ERROR);
        }
    }


    //检查拼购订单信息是否正确
    public void confirmPgOrder(Map<String, String> params){
        PgOrderMaster pgOrderMaster = pgOrderMasterMapper.selectByPrimaryKey(params.get("out_trade_no"));
        if(pgOrderMaster==null){
            throw new MadaoException(ErrorEnum.ORDER_NOT_EXIST);
        }

        //订单在商户网站中已经做过处理
        if(!pgOrderMaster.getOrderStatus().equals(PgOrderEnum.NEW.getCode())){
            throw new MadaoException(ErrorEnum.ORDER_STATUS_ERROR, pgOrderMaster.getOrderId());
        }

        double total_amount = Double.parseDouble(params.get("total_amount"));
        if(pgOrderMaster.getOrderAmount().compareTo(new BigDecimal(total_amount))!=0){
            throw new MadaoException(ErrorEnum.ORDER_PAY_ERROR);
        }

        String partner = AlipayConfig.partner;
        String seller_id = params.get("seller_id");
        if (!partner.equals(seller_id)){
            throw new MadaoException(ErrorEnum.ORDER_PAY_ERROR);
        }

// TODO 验证买家支付宝id
//        String account = pgOrderMaster.getPayAcount();
//        if (!account.equals(params.get("buyer_id")))
//            throw new MadaoException(ErrorEnum.ORDER_PAY_ERROR);
        pgOrderMaster.setOrderStatus(PgOrderEnum.PAYED.getCode());
        pgOrderMasterMapper.updateByPrimaryKeySelective(pgOrderMaster);
    }
}
