package cn.haizhi.market.other.enums.madao;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GroupAddForm {
    @NotNull(message = "订单id为空")
    private String orderId;
    @NotNull(message = "组id为空")
    private String groupId;
}
