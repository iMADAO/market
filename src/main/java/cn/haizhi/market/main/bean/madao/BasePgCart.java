package cn.haizhi.market.main.bean.madao;

import lombok.Data;

@Data
public class BasePgCart {
    private String itemId;
    private Long productId;
    private Integer productQuantity;
    private Byte isSelected = 0;
}
