package cn.haizhi.market.main.bean.madao;

import cn.haizhi.market.other.util.DateFormatUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Test {
    public static void main(String[] args){
        Date date = new Date();
        Date date1 = new Date(date.getTime() + 1000);
        Date date2 = new Date(date.getTime() - 1000);
        System.out.println(date.before(date1));
        System.out.println(date.before(date2));
        System.out.println(date.getTime());
        Calendar calendar = new GregorianCalendar();
        calendar.set(2018, 0, 18, 0, 0, 0);
        Calendar calendar1 = new GregorianCalendar();
        calendar1.set(2018, 0, 19, 0, 0, 1);
        date = new Date(calendar.getTimeInMillis());
        date1 = new Date(calendar1.getTimeInMillis());
        System.out.println(date + "--------------" + date1);
        System.out.println(date1.getTime()- date.getTime());
        if(date1.getTime()-date.getTime()>86400000){
            System.out.println("ok");
        }
        try {
            date = calendar.getTime();
            System.out.println(calendar.getTimeInMillis());
            Date dateTest = DateFormatUtil.StringToDate("20180118");
            System.out.println(date.getTime() + "-------------" + dateTest.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar2 = new GregorianCalendar();
        calendar2.setTimeInMillis(1515513600000L);
        System.out.println(calendar2.get(Calendar.DAY_OF_MONTH));
        System.out.println(calendar2.get(Calendar.HOUR) + "--" + calendar2.get(Calendar.MINUTE) + "---" + calendar2.get(Calendar.SECOND));
        Byte b = 1;
        Integer i = 1;
        System.out.println(b.equals(i));

        date = new Date();
        calendar.set(2018, 0, 20, 5, 0, 0);
        System.out.println(calendar.getTimeInMillis());
        System.out.println(DateFormatUtil.DateToString(new Date(1516395600180L)));
        date = new Date();
        System.out.println(date.getTime());
        System.out.println("{" +
                "\"out_trade_no\":\"20150320010101001\"," +
                "\"trade_no\":\"2014112611001004680073956707\"," +
                "\"refund_amount\":200.12," +
                "\"refund_currency\":\"USD\"," +
                "\"refund_reason\":\"正常退款\"," +
                "\"out_request_no\":\"HZ01RF001\"," +
                "\"operator_id\":\"OP001\"," +
                "\"store_id\":\"NJ_S_001\"," +
                "\"terminal_id\":\"NJ_T_001\"," +
                "      \"goods_detail\":[{" +
                "        \"goods_id\":\"apple-01\"," +
                "\"alipay_goods_id\":\"20010001\"," +
                "\"goods_name\":\"ipad\"," +
                "\"quantity\":1," +
                "\"price\":2000," +
                "\"goods_category\":\"34543238\"," +
                "\"body\":\"特价手机\"," +
                "\"show_url\":\"http://www.alipay.com/xxx.jpg\"" +
                "        }]" +
                "  }");

        BigDecimal bigDecimal = new BigDecimal(1.2341);
        System.out.println(bigDecimal.doubleValue());
    }

}
//1516276587041
