package cn.haizhi.market.main.handler.madao;

import cn.haizhi.market.main.service.madao.NotifyService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.config.AlipayConfig;
import com.alipay.util.AlipayNotify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@RestController
@Slf4j
public class NotifyHandler {

    @Autowired
    NotifyService notifyService;

    @RequestMapping(value="/pgOrder/notify",method= RequestMethod.POST)
    @ResponseBody
    public Object notifyUrl(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException {
        System.out.println("支付提示");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }


        for(Map.Entry<String, String> entry: params.entrySet()){
            System.out.println(entry.getKey() + "================================" + entry.getValue());
        }

        //params去除sign和sign_type、排序 拼凑成字符串 在 AlipaySignature getSignCheckContentV1() 中已有实现
        //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean flag = AlipaySignature.rsaCheckV1(params, AlipayConfig.ali_public_key, AlipayConstants.CHARSET_UTF8,"RSA2");
        //验证消息是否是支付宝发出的合法消息
        boolean flag1 = AlipayNotify.verify(params);
        //验证
        try {
            if(flag && flag1){
                //付款成功后
                if(params.get("trade_status").equals("TRADE_FINISHED")){
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //如果有做过处理，不执行商户的业务程序
                    notifyService.confirmPgOrder(params);
                    //注意：
                    //该种交易状态只在两种情况下出现
                    //1、开通了普通即时到账，买家付款成功后。
                    //2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
                    out.println("success"); //请不要修改或删除
                } else if (params.get("trade_status").equals("TRADE_SUCCESS")){
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //如果有做过处理，不执行商户的业务程序
                    notifyService.confirmPgOrder(params);
                    //注意：
                    //该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。

                    out.println("success"); //请不要修改或删除
                }
                //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
            } else {//
                return "fail";
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(e.getMessage());
            e.printStackTrace();
            return "fail";
        }
        return null;
    }

    @RequestMapping(value="/order/notify",method= RequestMethod.POST)
    @ResponseBody
    public Object notifyUrl1(HttpServletRequest request, HttpServletResponse response) throws AlipayApiException {
        System.out.println("支付提示");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<String,String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }


        for(Map.Entry<String, String> entry: params.entrySet()){
            System.out.println(entry.getKey() + "================================" + entry.getValue());
        }

        //params去除sign和sign_type、排序 拼凑成字符串 在 AlipaySignature getSignCheckContentV1() 中已有实现
        //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        boolean flag = AlipaySignature.rsaCheckV1(params, AlipayConfig.ali_public_key, AlipayConstants.CHARSET_UTF8,"RSA2");
        //验证消息是否是支付宝发出的合法消息
        boolean flag1 = AlipayNotify.verify(params);
        //验证
        try {
            if(flag && flag1){
                //付款成功后
                if(params.get("trade_status").equals("TRADE_FINISHED")){
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //如果有做过处理，不执行商户的业务程序
                    notifyService.confirmOrder(params);
                    //注意：
                    //该种交易状态只在两种情况下出现
                    //1、开通了普通即时到账，买家付款成功后。
                    //2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
                    out.println("success"); //请不要修改或删除
                } else if (params.get("trade_status").equals("TRADE_SUCCESS")){
                    //判断该笔订单是否在商户网站中已经做过处理
                    //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    //如果有做过处理，不执行商户的业务程序
                    notifyService.confirmOrder(params);
                    //注意：
                    //该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。

                    out.println("success"); //请不要修改或删除
                }
                //——请根据您的业务逻辑来编写程序（以上代码仅作参考）——
            } else {//
                return "fail";
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(e.getMessage());
            e.printStackTrace();
            return "fail";
        }
        return null;
    }


}
