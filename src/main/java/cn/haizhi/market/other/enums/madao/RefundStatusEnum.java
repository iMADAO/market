package cn.haizhi.market.other.enums.madao;

import lombok.Getter;

@Getter
public enum  RefundStatusEnum {
    NOT_REFUND((byte)0, "未退款"),
    REFUNDED((byte)1, "已退款")
    ;
    private String message;
    private Byte code;

    RefundStatusEnum(Byte code, String message) {
        this.message = message;
        this.code = code;
    }
}
