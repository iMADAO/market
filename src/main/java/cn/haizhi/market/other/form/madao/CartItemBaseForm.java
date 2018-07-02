package cn.haizhi.market.other.form.madao;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CartItemBaseForm {
    @NotNull(message = "商品为空")
    private Long productId;
}
