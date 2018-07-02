package cn.haizhi.market.main.bean.madao;

import lombok.Data;

@Data
public class BaseCart {
    private String cartId;
    private Long shopId;
    private String itemId;
    private Long userId;
    private Long productId;
    private Integer productQuantity;
    private Byte isSelected = 0;
}
