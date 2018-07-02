package cn.haizhi.market.other.enums.madao;

public enum CartSelectedEnum {
    UNSELECTED((byte) 0, "未选中"),
    SELECTED((byte) 1,"已选中"),
    ;
    private Byte code;
    private String message;

    CartSelectedEnum(Byte code, String message) {
        this.code = code;
        this.message = message;
    }
}
