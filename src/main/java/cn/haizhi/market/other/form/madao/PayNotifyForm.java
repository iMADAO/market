package cn.haizhi.market.other.form.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class PayNotifyForm {
    @NotNull
    private Long userId;

    @NotBlank
    private String orderId;

    @NotBlank
    private String payAccount;

    @NotNull
    private Byte payType;
}
