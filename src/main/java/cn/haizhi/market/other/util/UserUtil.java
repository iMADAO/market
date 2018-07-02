package cn.haizhi.market.other.util;

import cn.haizhi.market.main.bean.qiyuan.User;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.util.qiyuan.Const;

import javax.servlet.http.HttpSession;

public class UserUtil {
    public static Long getUserId(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null)
            throw new MadaoException(ErrorEnum.USER_NOT_LOGIN);
        return user.getUserId();
    }
}
