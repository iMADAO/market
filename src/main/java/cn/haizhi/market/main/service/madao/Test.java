package cn.haizhi.market.main.service.madao;

import cn.haizhi.market.other.util.BeanUtil;
import cn.haizhi.market.other.util.DateFormatUtil;

import java.math.BigDecimal;

public class Test {
    public static void main(String[] args){
    StringBuffer sb = new StringBuffer();
    System.out.println(BeanUtil.getId());

    System.out.println(DateFormatUtil.getLastTimeOfDay());
    System.out.println();
    BigDecimal num = new BigDecimal(0.01);
    System.out.println(num);
    Double n = num.doubleValue();
    System.out.println(n);


    }
}
