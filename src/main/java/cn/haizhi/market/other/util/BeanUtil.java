package cn.haizhi.market.other.util;

import org.springframework.beans.BeanUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Date: 2018/1/9
 * Author: Richard
 */

public class BeanUtil {

    public static Boolean isNull(Object object){
        return (object == null);
    }

    public static Boolean notNull(Object object){
        return (object != null);
    }

    public static Boolean isEmpty(String string){
        return (string == null || string.trim().isEmpty());
    }

    public static Boolean notEmpty(String string){
        return (string != null && !string.trim().isEmpty());
    }

    public static Boolean isEmpty(List<?> list){ return (list == null || list.size() == 0);}

    public static Boolean notEmpty(List<?> list){
        return (list != null && list.size() > 0);
    }

    public static void copyBean(Object source,Object target){
        BeanUtils.copyProperties(source,target);
    }

    //生成最大19位的Long型编号
    public static synchronized Long getId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    public static String isLike(String string) throws UnsupportedEncodingException {
        return "%" + new String(string.getBytes("iso-8859-1"), "utf-8") + "%";
    }

}
