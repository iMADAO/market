package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderCreateForm {

    @NotNull(message="地址不能为空")
    private Long addressId;

    @NotEmpty(message = "未选中购物项")
    private List<String> cartItemIdList;
}
