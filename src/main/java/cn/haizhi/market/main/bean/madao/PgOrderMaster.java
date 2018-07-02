package cn.haizhi.market.main.bean.madao;

import cn.haizhi.market.other.enums.madao.CancelStatusEnum;
import cn.haizhi.market.other.enums.madao.CommentStatusEnum;
import cn.haizhi.market.other.enums.madao.OrderStatusEnum;
import cn.haizhi.market.other.enums.madao.RefundStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PgOrderMaster {
    private String orderId;

    private String groupId;

    private Long userId;

    private String userName;

    private String userPhone;

    private String userAddress;

    private BigDecimal orderAmount;

    private Byte orderStatus = OrderStatusEnum.NEW.getCode();

    private Byte commentStatus = CommentStatusEnum.WAIT.getCode();

    private Byte payWay;

    private String payAcount;

    private Date deliveryTime;

    private Date arriveTime;

    private Date receiveTime;

    private Long commentId;

    private Date createTime;

    private Date updateTime;

    private String orderRemark;

    private Byte cancelStatus = CancelStatusEnum.NO_CANCEL.getCode();

    private Byte refundStatus = RefundStatusEnum.NOT_REFUND.getCode();

    private Date deliveryStartTime;

}