package cn.haizhi.market.main.handler.madao;

import cn.haizhi.market.main.bean.madao.OrderDTO;
import cn.haizhi.market.main.bean.madao.OrderSignInfo;
import cn.haizhi.market.main.service.madao.CommonOrderService;
import cn.haizhi.market.main.service.madao.OrderService;
import cn.haizhi.market.main.view.ResultView;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.form.madao.RemarkForm;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.exception.ResultException;
import cn.haizhi.market.other.form.madao.*;
import cn.haizhi.market.other.util.ResultUtil;
import cn.haizhi.market.other.util.UserUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

import static cn.haizhi.market.other.util.FormErrorUtil.getFormErrors;

@RestController
public class OrderHandler {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonOrderService commonOrderService;

    //下订单
    @PostMapping("/order")
    public ResultView createOrder(@Valid @RequestBody OrderCreateForm orderForm, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        List<OrderDTO> orderDTOList = orderService.addOrder(orderForm, UserUtil.getUserId(session));
        if (orderDTOList.size() == 0) {
            throw new ResultException(ErrorEnum.CREATE_ORDER_FAIL.getMessage());
        }
        return ResultUtil.returnSuccess(orderDTOList);
    }

    //根据用户id、商铺id, 订单状态获取订单，订单状态、评价状态、 配送状态可选
    @GetMapping("/order")
    public ResultView getOrderByUser(@RequestParam(value = "shopId", required = false) Long shopId, @RequestParam("userId") Long userId, @RequestParam(value = "orderStatus", required = false) Byte orderStatus, @RequestParam(value = "commentStatus", required = false) Byte commentStatus, @RequestParam(value = "cancelStatus", required = false) Byte cancelStatus, @RequestParam(value = "refundStatus", required = false) Byte refundStatus) {
        return ResultUtil.returnSuccess(orderService.getOrderDTO(shopId, userId, orderStatus, commentStatus, cancelStatus, refundStatus));
    }

    //用户获取订单 分页
    @GetMapping("/order/page")
    public ResultView getOrderByUserInPage(@RequestParam(value = "shopId", required = false) Long shopId, @RequestParam(value = "orderStatus", required = false) Byte orderStatus, @RequestParam(value = "commentStatus", required = false) Byte commentStatus, @RequestParam(value = "cancelStatus", required = false) Byte cancelStatus, @RequestParam(value = "refundStatus", required = false) Byte refundStatus, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize, HttpSession session) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderDTO> orderDTOList = orderService.getOrderDTO(shopId, UserUtil.getUserId(session), orderStatus, commentStatus, cancelStatus, refundStatus);
        PageInfo pageInfo = new PageInfo(orderDTOList);
        return ResultUtil.returnSuccess(pageInfo);
    }


    //商家调用查看订单
    @GetMapping("/order/shop")
    public ResultView getOrderByShop(@RequestParam(value = "shopId") Long shopId, @RequestParam(value = "orderStatus", required = false) Byte orderStatus, @RequestParam(value = "commentStatus", required = false) Byte commentStatus, @RequestParam(value = "cancelStatus", required = false) Byte cancelStatus, @RequestParam(value = "refundStatus", required = false) Byte refundStatus) {
        return ResultUtil.returnSuccess(orderService.getOrderDTO(shopId, null, orderStatus, commentStatus, cancelStatus, refundStatus));
    }

    @GetMapping("/order/shop/page")
    public ResultView getOrderByShopInfPage(@RequestParam(value = "shopId", required = false) Long shopId, @RequestParam(value = "orderStatus", required = false) Byte orderStatus, @RequestParam(value = "commentStatus", required = false) Byte commentStatus, @RequestParam(value = "cancelStatus", required = false) Byte cancelStatus, @RequestParam(value = "refundStatus", required = false) Byte refundStatus, @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderDTO> orderDTOList = orderService.getOrderDTO(shopId, null, orderStatus, commentStatus, cancelStatus, refundStatus);
        PageInfo pageInfo = new PageInfo(orderDTOList);
        return ResultUtil.returnSuccess(pageInfo);
    }


    //支付普通订单
//    @PutMapping("/order")
//    public ResultView payOrder(@Valid @RequestBody OrderUpdateForm orderUpdateForm, BindingResult bindingResult) {
//        if (bindingResult.hasErrors()) {
//            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
//        }
//        orderService.payOrder(orderUpdateForm.getId(), orderUpdateForm.getOrderIdList());
//        return ResultUtil.returnSuccess();
//    }


    //商家设置配送时间
    @PutMapping("/order/delivery")
    public ResultView setOrderDeliveryTime(@Valid @RequestBody OrderDateForm orderDateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.updateDeliveryTime(orderDateForm.getShopId(), orderDateForm.getOrderIdList(), orderDateForm.getDate());
        return ResultUtil.returnSuccess();
    }


    //商家确认送达
    @PutMapping("/order/arrive")
    public ResultView setOrderArriveTime(@Valid @RequestBody OrderDateForm orderDateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.confirmArriveByShop(orderDateForm.getShopId(), orderDateForm.getOrderIdList(), orderDateForm.getDate());
        return ResultUtil.returnSuccess();
    }


    //用户取消订单
    @PutMapping("/order/cancel")
    public ResultView cancelOrderByUser(@Valid @RequestBody OrderIdForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.cancelOrderByUser(form.getId(), form.getOrderIdList());
        return ResultUtil.returnSuccess();
    }

    //商家取消订单
    @PutMapping("/order/cancel/shop")
    public ResultView cancelOrderByShop(@Valid @RequestBody OrderIdForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        List<OrderDTO> result = orderService.cancelOrderByShop(form.getId(), form.getOrderIdList());
        if (result.size() == 0)
            throw new MadaoException(ErrorEnum.ORDER_NOT_CANCEL);
        return ResultUtil.returnSuccess();
    }


    //用户确认收货 普通订单
    @PutMapping("/order/confirm")
    public ResultView confirmOrder(@Valid @RequestBody OrderIdForm orderIdForm, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.confirm(UserUtil.getUserId(session), orderIdForm.getOrderIdList());
        return ResultUtil.returnSuccess();
    }


    //商家确认取消普通订单
    @PutMapping("/order/cancel/confirm/shop")
    public ResultView confirmCancel(@Valid @RequestBody OrderIdForm orderdForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.confirmCancel(orderdForm.getId(), orderdForm.getOrderIdList());
        return ResultUtil.returnSuccess();
    }

    //商家设置订单配送中
    @PutMapping("/order/sending")
    public ResultView setOrderSending(@Valid @RequestBody OrderDeliveryStatusForm form, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.setOrderDeliveryStatusSending(form);
        return ResultUtil.returnSuccess();
    }

    //用户普通订单留言
    @PutMapping("/order/remark")
    public ResultView setOrderRemark(@Valid @RequestBody RemarkForm form, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.setOrderRemark(UserUtil.getUserId(session), form.getOrderId(), form.getRemark());
        return ResultUtil.returnSuccess();
    }

    //用户获取两种类型的订单
    @GetMapping("/order/allType")
    public ResultView getCommonOrderByUser(@RequestParam(value = "orderStatus", required = false) Byte orderStatus, @RequestParam(value = "commentStatus", required = false) Byte commentStatus, @RequestParam(value = "cancelStatus", required = false) Byte cancelStatus, @RequestParam(value = "refundStatus", required = false) Byte refundStatus, HttpSession session) {
        return ResultUtil.returnSuccess(commonOrderService.getCommonOrder(UserUtil.getUserId(session), orderStatus,  commentStatus, cancelStatus, refundStatus));
    }

    //获取订单支付需要的sign
    @PutMapping("/order/sign")
    public ResultView pgOrderSign(@Valid @RequestBody OrderSignForm form, BindingResult bindingResult){
        if(bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        OrderSignInfo orderSignInfo = orderService.getOrderSign(form);
        return ResultUtil.returnSuccess(orderSignInfo);
    }

    //订单支付结果的通知
    @PutMapping("/order/pay/callback")
    public ResultView pgOrderSignCallBack(@Valid @RequestBody OrderPayBackForm form, BindingResult bindingResult){
        if(bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        orderService.payCallBack(form);
        return ResultUtil.returnSuccess();
    }
}
