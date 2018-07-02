package cn.haizhi.market.other.enums.madao;

import lombok.Getter;

@Getter
public enum PgOrderEnum {
//    NEW((byte)0, "新订单"),
//    IN_GROUP((byte)1, "拼购组成"),
//    FINISH((byte)2, "已完成"),
//    TO_CANCEL((byte)3, "发起取消"),
//    CANCEL((byte)4, "已取消"),

    NEW((byte)0, "待付款"),
    PAYED((byte)1, "待发货"),
    WAIT_RECEIVE((byte)2, "待收货"),
    FINISHED((byte)3, "已完成"),
    ;

    // 待付款   待发货 待收货 已完成
    // 是否评价
    private Byte code;
    private String message;

    PgOrderEnum(Byte code, String message) {
        this.code = code;
        this.message = message;
    }
}
