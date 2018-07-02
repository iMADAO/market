package cn.haizhi.market.main.handler.madao;

import cn.haizhi.market.main.bean.madao.PgOrderDTO;
import cn.haizhi.market.main.mapper.madao.CommonMapper;
import cn.haizhi.market.main.service.madao.PgOrderService;
import cn.haizhi.market.main.view.ResultView;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.enums.madao.PgOrderCreateForm;
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
public class PgOrderHandler {
    @Autowired
    private PgOrderService pgOrderService;

    @Autowired
    private CommonMapper commonMapper;

    //创建拼购订单
    @PostMapping("/pgOrder")
    public ResultView createPgOrder(@Valid @RequestBody PgOrderCreateForm orderForm, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        PgOrderDTO pgOrderDTO = pgOrderService.addPgOrder(orderForm, UserUtil.getUserId(session));
        if(pgOrderDTO.getOrderItemList().size()==0){
            throw new ResultException(ErrorEnum.CREATE_ORDER_FAIL.getMessage());
        }
        return ResultUtil.returnSuccess(pgOrderDTO);
    }

    //根据用户id和订单状态获取拼购订单
    @GetMapping("/pgOrder")
    public ResultView getPgOrderByUser(@RequestParam(value="orderStatus", required = false) Byte orderStatus, @RequestParam(value="commentStatus", required=false) Byte commentStatus, @RequestParam(value="cancelStatus", required=false) Byte cancelStatus, @RequestParam(value="refundStatus", required=false) Byte refundStatus, HttpSession session){
        return ResultUtil.returnSuccess(pgOrderService.getPgOrderDTO(UserUtil.getUserId(session), orderStatus, commentStatus, cancelStatus, refundStatus));
    }

    @GetMapping("/pgOrder/page")
    public ResultView getPgOrderByUserInPage(@RequestParam(value="orderStatus", required = false) Byte orderStatus, @RequestParam(value="commentStatus", required=false) Byte commentStatus, @RequestParam(value="cancelStatus", required=false) Byte cancelStatus, @RequestParam(value="refundStatus", required=false) Byte refundStatus,  @RequestParam(value="pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value="pageSize", defaultValue = "10") Integer pageSize, HttpSession session){
        PageHelper.startPage(pageNum, pageSize);
        List<PgOrderDTO> pgOrderDTOList = pgOrderService.getPgOrderDTO(UserUtil.getUserId(session), orderStatus, commentStatus, cancelStatus, refundStatus);
        PageInfo pageInfo = new PageInfo(pgOrderDTOList);
        return ResultUtil.returnSuccess(pageInfo);
    }

    //商家带调用查看拼购订单
    @GetMapping("/pgOrder/shop")
    public ResultView getPgOrderByShop(@RequestParam(value="shopId", required=false) Long shopId, @RequestParam(value="orderStatus", required=false) Byte orderStatus, @RequestParam(value="commentStatus", required=false) Byte commentStatus, @RequestParam(value="cancelStatus", required=false) Byte cancelStatus, @RequestParam(value="refundStatus", required=false) Byte refundStatus){
        return ResultUtil.returnSuccess(pgOrderService.getPgOrderDTO(null, orderStatus, commentStatus, cancelStatus, refundStatus));
    }

    @GetMapping("/pgOrder/shop/page")
    public ResultView getPgOrderByShop(@RequestParam(value="shopId", required=false) Long shopId, @RequestParam(value="orderStatus", required=false) Byte orderStatus, @RequestParam(value="commentStatus", required=false) Byte commentStatus, @RequestParam(value="cancelStatus", required=false) Byte cancelStatus, @RequestParam(value="refundStatus", required=false) Byte refundStatus, @RequestParam(value="pageNum", defaultValue = "1") Integer pageNum, @RequestParam(value="pageSize", defaultValue = "10") Integer pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<PgOrderDTO> pgOrderDTOList = pgOrderService.getPgOrderDTO(null, orderStatus, commentStatus,cancelStatus, refundStatus);
        PageInfo pageInfo = new PageInfo(pgOrderDTOList);
        return ResultUtil.returnSuccess(pageInfo);
    }

    //商家设置配送时间
    @PutMapping("/pgOrder/delivery")
    public ResultView setPgOrderDeliveryTime(@Valid @RequestBody OrderDateForm orderDateForm, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.updateDeliveryTime(orderDateForm.getShopId(), orderDateForm.getOrderIdList(), orderDateForm.getDate());
        return ResultUtil.returnSuccess();
    }

    //商家确认订单到达
    @PutMapping("/pgOrder/arrive")
    public ResultView setPgOrderArriveTime(@Valid @RequestBody OrderDateForm orderDateForm, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.confirmArriveByShopPg(orderDateForm.getShopId(), orderDateForm.getOrderIdList(),  orderDateForm.getDate());
        return ResultUtil.returnSuccess();
    }

    //删除未支付拼购订单
    @DeleteMapping("/pgOrder/remove")
    public ResultView cancelPgOrderByUser(@Valid @RequestBody OrderIdForm form, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.cancelPgOrder(form.getId(), form.getOrderIdList());
        return ResultUtil.returnSuccess();
    }

//    //商家取消拼购订单
//    @PutMapping("/pgOrder/cancel/shop")
//    public ResultView cancelPgOrderByShop(@Valid @RequestBody OrderIdForm pgOrderIdForm, BindingResult bindingResult){
//        if(bindingResult.hasErrors()){
//            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
//        }
//        List<String> orderIdList = pgOrderIdForm.getOrderIdList();
//        List<PgOrderDTO> result = pgOrderService.cancelPgOrderByShop(orderIdList);
//        if(result.size()==0)
//            throw new MadaoException(ErrorEnum.ORDER_NOT_CANCEL);
//        return ResultUtil.returnSuccess();
//    }

    //用户确认收货  拼购订单
    @PutMapping("/pgOrder/confirm")
    public ResultView confirmPgOrder(@Valid @RequestBody OrderIdForm orderIdForm, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.confirmPg(UserUtil.getUserId(session), orderIdForm.getOrderIdList());
        return ResultUtil.returnSuccess();
    }

//    //商家确认取消拼购订单
//    @PutMapping("/pgOrder/cancel/confirm/shop")
//    public ResultView confirmCancelPg(@Valid @RequestBody OrderIdForm orderdForm, BindingResult bindingResult){
//        if(bindingResult.hasErrors()){
//            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
//        }
//        pgOrderService.confirmCancelPg(orderdForm.getId(), orderdForm.getOrderIdList());
//        return ResultUtil.returnSuccess();
//    }

    //用户拼购订单留言
    @PutMapping("/pgOrder/remark")
    public ResultView remarkPgOrder(@Valid @RequestBody RemarkForm form, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.setPgOrderRemark(UserUtil.getUserId(session), form.getOrderId(), form.getRemark());
        return ResultUtil.returnSuccess();
    }

    //设置订单配送中
    @PutMapping("/pgOrder/sending")
    public ResultView setOrderSending(@Valid @RequestBody OrderDeliveryStatusForm form, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.setOrderDeliveryStatusSending(form);
        return ResultUtil.returnSuccess();
    }

    @PutMapping("/pgOrder/refund")
    public ResultView pgOrderRefund(@Valid @RequestBody RefundForm form, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.pgOrderRefund(form);
        return ResultUtil.returnSuccess();
    }

    @PutMapping("/pgOrder/pay/callback")
    public ResultView pgOrderSignCallBack(@Valid @RequestBody OrderPayBackForm form, BindingResult bindingResult){
        if(bindingResult.hasErrors()) {
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgOrderService.payCallBack(form);
        return ResultUtil.returnSuccess();
    }
}
