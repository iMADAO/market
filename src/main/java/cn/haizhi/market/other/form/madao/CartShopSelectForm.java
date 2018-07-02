package cn.haizhi.market.other.form.madao;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CartShopSelectForm {
    @NotNull(message = "购物车id为空")
    private String cartId;
    @NotNull(message = "设置状态为空")
    private Byte isSelected;
}
