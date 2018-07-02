package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CartItemSelectForm {
    @NotEmpty(message = "购物车项为空")
    private List<String> cartItemIdList;
    @NotNull(message = "状态为空")
    private Byte isSelected;
}
