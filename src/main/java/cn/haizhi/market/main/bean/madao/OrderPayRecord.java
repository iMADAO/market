package cn.haizhi.market.main.bean.madao;

import lombok.Data;

@Data
public class OrderPayRecord {
    private Long userId;

    private String leadOrderId;

    private String orderId;

    public OrderPayRecord(Long userId, String leadOrderId, String orderId) {
        this.userId = userId;
        this.leadOrderId = leadOrderId;
        this.orderId = orderId;
    }

    public OrderPayRecord() {
    }
}