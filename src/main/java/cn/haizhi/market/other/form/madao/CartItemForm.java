package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CartItemForm {
    @NotNull(message = "商品为空")
    private Long productId;

    @NotNull(message = "商品数量为空")
    @Min(value=1, message = "商品数量不能少于1")
    private Integer productQuantity;

    public CartItemForm(Long productId, Integer productQuantit) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }

    public CartItemForm() {
    }
}
