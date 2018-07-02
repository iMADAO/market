package cn.haizhi.market.main.bean.madao;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayNotify {
    private Date notify_time;
    private String notify_type;
    private String notify_id;
    private String sign_type;
    private String sign;
    private String out_trade_no;
    private String subject;
    private String payment_type;
    private String trade_no;
    private String trade_status;
    private String seller_id;
    private String seller_email;
    private String buyer_id;
    private String buyer_email;
    private BigDecimal total_fee;
    private Long quantity;
    private BigDecimal price;

    private String body;
    private Date gmt_create;
    private Date gmt_payment;
    private String is_total_fee_adjust;
    private String use_coupon;
    private String discount;
    private String refund_status;
    private Date gmt_refund;
}
