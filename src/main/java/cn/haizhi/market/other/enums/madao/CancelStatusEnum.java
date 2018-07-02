package cn.haizhi.market.other.enums.madao;

import lombok.Getter;

@Getter
public enum CancelStatusEnum {
    NO_CANCEL((byte)0, "未取消"),
    TO_CANCEL((byte)1, "发起取消"),
    CANCELED((byte)2, "已取消"),
    ;
    private Byte code;
    private String message;

    CancelStatusEnum(Byte code, String message) {
        this.code = code;
        this.message = message;
    }
}
