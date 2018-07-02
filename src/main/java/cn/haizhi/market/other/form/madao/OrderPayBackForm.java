package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrderPayBackForm {
    @NotNull(message = "用户id为空")
    private Long userId;
    @NotBlank(message = "订单id为空")
    private String orderId;

    private Byte payType;

    private String payAccount;

    @NotNull(message = "支付结果为空")
    private Byte flag;
}
