package cn.haizhi.market.main.handler.madao;

import cn.haizhi.market.main.bean.madao.PageInfo;
import cn.haizhi.market.main.bean.madao.PgCartItem;
import cn.haizhi.market.main.bean.madao.PgCartItemDTO;
import cn.haizhi.market.main.service.madao.PgCartItemJedisService;
import cn.haizhi.market.main.service.madao.PgCartItemService;
import cn.haizhi.market.main.view.ResultView;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.form.madao.*;
import cn.haizhi.market.other.util.IdResultMap;
import cn.haizhi.market.other.util.ResultUtil;
import cn.haizhi.market.other.util.UserUtil;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import java.util.List;

import static cn.haizhi.market.other.util.FormErrorUtil.getFormErrors;

@RestController
public class PgCartItemHandler {

    @Autowired
//  private PgCartItemService pgCartItemService;
    private PgCartItemJedisService pgCartItemService;
    //添加进购物车
    @PostMapping("/pgCartItem")
    public ResultView addPgCartItem(@Valid @RequestBody PgCartItemForm pgCartItemForm, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        String id = pgCartItemService.addCartItem(pgCartItemForm, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess(IdResultMap.getIdMap(id));
    }

    //获取购物车列表
    @GetMapping("/pgCartItem")
    public ResultView getCartItemListByUserId(HttpSession session) {
        return ResultUtil.returnSuccess(pgCartItemService.getPgCartItemList(UserUtil.getUserId(session)));
    }

    //获取购物车列表-分页
    @GetMapping("/pgCartItem/page")
    public ResultView getPgCartItemListByUserIdInPage(@RequestParam(value = "pageNum", defaultValue = "10") Integer pageNum, @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize, HttpSession session){
//use redis for pgCart
//        PageInfo pageInfo = pgCartItemService.getPgCartItemListInPage(userId, pageNum, pageSize);
        PageHelper.startPage(pageNum, pageSize);
        List<PgCartItemDTO> list  = pgCartItemService.getPgCartItemList(UserUtil.getUserId(session));
        PageInfo pageInfo = new PageInfo();
        pageInfo.setList(list);
        return ResultUtil.returnSuccess(pageInfo);
    }

    //根据购物车id删除
    @DeleteMapping("/pgCartItem")
    public ResultView deleteCartItem(@Valid @RequestBody CartItemDeleteForm cartItemDeleteForm, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgCartItemService.deleteCartItemById(cartItemDeleteForm, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess();
    }

    //根据用户id清空购物车
    @DeleteMapping("/pgCartItem/empty")
    public ResultView emptyCartItemByUserId(HttpSession session){
        pgCartItemService.deleteCartItemByUserId(UserUtil.getUserId(session));
        return ResultUtil.returnSuccess();
    }

    //修改购物车数量
    @PutMapping("/pgCartItem")
    public ResultView updateCartItem(@Valid @RequestBody CartItemReviseForm cartItemReviseForm, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgCartItemService.setPgCartItemQuantity(cartItemReviseForm, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess(IdResultMap.getIdMap(cartItemReviseForm.getCartItemId()));
    }

    //设置购物车项选中状态
    @PutMapping("/pgCartItem/select")
    public ResultView selectCartItem(@Valid @RequestBody CartItemSelectForm form, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        pgCartItemService.selectCartItem(form, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess();
    }

}
