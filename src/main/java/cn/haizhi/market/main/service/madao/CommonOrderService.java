package cn.haizhi.market.main.service.madao;

import cn.haizhi.market.main.bean.madao.CommonOrder;
import cn.haizhi.market.main.mapper.madao.CommonMapper;
import cn.haizhi.market.other.enums.madao.OrderTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CommonOrderService {
    @Autowired
    CommonMapper commonMapper;

    public List<CommonOrder> getCommonOrder(Long userId, Byte orderStatus, Byte commentStatus, Byte cancelStatus, Byte refundStatus){
        List<CommonOrder> orderlist = commonMapper.getCommonOrderList(null, userId, orderStatus, commentStatus, cancelStatus, refundStatus);
        List<CommonOrder> pgOrderList = commonMapper.getPgCommonOrderList(userId, orderStatus, commentStatus, cancelStatus, refundStatus);
        for(CommonOrder commonOrder: pgOrderList){
            commonOrder.setOrderType(OrderTypeEnum.PG_ORDER.getCode());
        }
        orderlist.addAll(pgOrderList);
        Collections.sort(orderlist);
        return orderlist;
    }
}
