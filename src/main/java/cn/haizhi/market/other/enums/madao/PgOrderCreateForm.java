package cn.haizhi.market.other.enums.madao;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PgOrderCreateForm {
    @NotNull(message="地址不能为空")
    private Long addressId;

    @NotBlank(message = "拼购组为空")
    private String groupId;

    @NotEmpty(message = "未选中购物项")
    private List<String> cartItemIdList;
}
