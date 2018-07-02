package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class OrderSignForm {
    @NotNull(message = "用户id为空")
    private Long userId;

    @NotEmpty(message = "订单id为空")
    private List<String> orderIdList;
}
