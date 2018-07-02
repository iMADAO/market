package cn.haizhi.market.other.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateFormatUtil {
    public static String DateToString(Date date){
//        Integer year = calendar.get(Calendar.YEAR);
//        Integer month = calendar.get(Calendar.MONTH);
//        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
//        return year.toString() + month.toString() + day.toString();
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(date);
    }

    public static Date StringToDate(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.parse(dateStr);
    }

    public static Date getLastTimeOfDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }

    public static boolean compareByDate(Date date){
        Calendar currCalendar = new GregorianCalendar();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if(currCalendar.get(Calendar.YEAR) > calendar.get(Calendar.YEAR))
            return false;
        else if (currCalendar.get(Calendar.YEAR) < calendar.get(Calendar.YEAR))
            return true;

        else if (currCalendar.get(Calendar.DAY_OF_YEAR) > calendar.get(Calendar.DAY_OF_YEAR))
            return false;
        else
            return true;
    }
}
