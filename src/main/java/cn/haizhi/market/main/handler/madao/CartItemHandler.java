package cn.haizhi.market.main.handler.madao;

import cn.haizhi.market.main.bean.madao.CartItemDTO;
import cn.haizhi.market.main.bean.madao.CartShopDTO;
import cn.haizhi.market.main.bean.qiyuan.User;
import cn.haizhi.market.main.service.madao.CartItemJedisService;
import cn.haizhi.market.main.service.madao.CartItemService;
import cn.haizhi.market.main.view.ResultView;
import cn.haizhi.market.other.enums.madao.CartItemCategoryEnum;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.exception.ResultException;
import cn.haizhi.market.other.form.madao.*;
import cn.haizhi.market.other.util.IdResultMap;
import cn.haizhi.market.other.util.ResultUtil;
import cn.haizhi.market.other.util.UserUtil;
import cn.haizhi.market.other.util.qiyuan.Const;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.xml.transform.Result;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.haizhi.market.other.util.FormErrorUtil.getFormErrors;

@RestController
public class CartItemHandler {
    //购物车存在数据库时候的类
    //    @Autowired
//    private CartItemService cartItemService;

    @Autowired
    private CartItemJedisService cartItemService;

    //根据用户id和购物车类别获取购物车列表
    @GetMapping("/cartItem")
    public ResultView getCartItemListByUserId(HttpSession session) {
       return ResultUtil.returnSuccess(cartItemService.getCartListByUserId(UserUtil.getUserId(session)));
    }

    //获取购物车列表 分页
    @GetMapping("/cartItem/page")
    public ResultView getCartItemListByUserId(@RequestParam(value="pageNum", defaultValue="1") Integer pageNum, @RequestParam(value="pageSize", defaultValue="10") Integer pageSize, HttpSession session){
        cn.haizhi.market.main.bean.madao.PageInfo<CartShopDTO> pageInfo = cartItemService.getCartListByUserIdInPage(UserUtil.getUserId(session), pageNum, pageSize);
        return ResultUtil.returnSuccess(pageInfo);
    }


    //添加购物车
    @PostMapping("/cartItem")
    public ResultView addCartItem(@Valid @RequestBody CartItemForm form, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        Map<String, String> map = cartItemService.addCartItem(form, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess(map);
    }

    @PostMapping("/cartItem/incr")
    public ResultView addCartItemAddOne(@Valid @RequestBody CartItemBaseForm form, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        Map<String, String> map = cartItemService.addCartItemByOne(form, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess(map);
    }

    //根据购物车id删除
    @DeleteMapping("/cartItem")
    public ResultView deleteCartItem(@Valid @RequestBody CartItemDeleteForm form, BindingResult bindingResult, HttpSession session) {
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        cartItemService.deleteCartItemById(UserUtil.getUserId(session), form.getCartItemId());
        return ResultUtil.returnSuccess();
    }

    //根据用户id删除，清空用户购物车
    @DeleteMapping("/cartItem/empty")
    public ResultView emptyCartItem(HttpSession session){
        cartItemService.deleteCartItemByUserId(UserUtil.getUserId(session));
        return ResultUtil.returnSuccess();
    }

    //修改购物车数量
    @PutMapping("/cartItem")
    public ResultView updateCartItem(@Valid @RequestBody CartItemReviseForm form, BindingResult bindingResult, HttpSession session){
        if(bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        cartItemService.updateCartItemQuantity(UserUtil.getUserId(session), form.getCartItemId(), form.getQuantity());
        return ResultUtil.returnSuccess(IdResultMap.getIdMap(form.getCartItemId()));
    }

    @PutMapping("/cartItem/select")
    public ResultView selectCartItem(@Valid @RequestBody CartItemSelectForm form, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        cartItemService.selectCartItem(form, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess();
    }

    @PutMapping("/cartItem/select/shop")
    public ResultView selectCartItemByShop(@Valid @RequestBody CartShopSelectForm form, BindingResult bindingResult, HttpSession session){
        if (bindingResult.hasErrors()){
            throw new MadaoException(ErrorEnum.PARAM_ERROR, getFormErrors(bindingResult));
        }
        cartItemService.selectCartItemByShopId(form, UserUtil.getUserId(session));
        return ResultUtil.returnSuccess();
    }
}
