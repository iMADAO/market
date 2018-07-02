package cn.haizhi.market.main.bean.madao;

import cn.haizhi.market.other.enums.madao.GroupStatusEnum;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Data
@DynamicUpdate
public class PgGroup {
    private String groupId;

    private String leadMemberId;

    private Integer groupNum;

    private Integer groupCount;

    private Byte groupStatus = GroupStatusEnum.WAIT.getCode();

    private Date deadDate;

    private Byte activeStatus = GroupStatusEnum.WAIT.getCode();

    private Date createTime;

    private Date updateTime;

    public String getGroupId() {
        return groupId;
    }
}