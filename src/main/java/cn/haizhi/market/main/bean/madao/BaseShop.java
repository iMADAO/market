package cn.haizhi.market.main.bean.madao;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BaseShop {
    private Long shopId;
    private String shopName;
    private Integer shopState;
    private BigDecimal limitPrice;
    private BigDecimal sendPrice;
}
