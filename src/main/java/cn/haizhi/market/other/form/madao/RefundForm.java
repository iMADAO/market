package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class RefundForm {
    @NotBlank(message=" 订单id为空")
    private String orderId;

}
