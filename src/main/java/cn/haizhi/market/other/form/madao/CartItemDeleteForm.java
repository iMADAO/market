package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class CartItemDeleteForm {
    @NotBlank(message = "购物项Id为空")
    private String cartItemId;

}
