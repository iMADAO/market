package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class CartItemReviseForm {
    @NotBlank(message = "购物车项为空")
    private String cartItemId;
    @NotNull(message = "数量不能为空")
    @Min(value=1, message = "数量不能小于1")
    private Integer quantity;
}
