package cn.haizhi.market.main.bean.madao;

import lombok.Data;

@Data
public class OrderSignInfo {
    private String orderId;
    private Double amount;
    private String orderInfo;

    public OrderSignInfo(String orderId, Double amount, String orderInfo) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
    }

    public OrderSignInfo() {
    }
}
