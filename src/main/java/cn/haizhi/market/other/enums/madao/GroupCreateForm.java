package cn.haizhi.market.other.enums.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class GroupCreateForm {
    @NotBlank(message = "订单为空")
    private String orderId;
}
