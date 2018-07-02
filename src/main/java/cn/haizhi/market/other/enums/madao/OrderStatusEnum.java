package cn.haizhi.market.other.enums.madao;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
//    NEW((byte)0, "新订单"),
//    PAY((byte)1, "已支付"),
//    WAIT_SEND((byte)2, "待收货"),
//    SENDING((byte)3, "送货中"),
//    RECEIVE((byte)4, "已收货"),
//    FINISHED((byte)5, "已完成"),
//    TO_CANCEL((byte)6, "发起取消"),
//    CANCELED((byte)7, "已取消"),


    NEW((byte)0, "待付款"),
    PAYED((byte)1, "待发货"),
    WAIT_RECEIVE((byte)2, "待收货"),
    FINISHED((byte)3, "已完成"),

    // 待付款   待发货 待收货 已完成
    // 是否评价

    ;
    private Byte code;
    private String message;

    OrderStatusEnum(Byte code, String message) {
        this.code = code;
        this.message = message;
    }
}
