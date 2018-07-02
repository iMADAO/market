package cn.haizhi.market.main.handler.richard;

import cn.haizhi.market.main.bean.richard.*;
import cn.haizhi.market.main.service.richard.FrontService;
import cn.haizhi.market.main.view.ResultView;
import cn.haizhi.market.other.util.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Date: 2018/1/9
 * Author: Richard
 */

@RestController
@RequestMapping("/front")
public class FrontHandler {

    @Autowired
    private FrontService frontService;

    @GetMapping(value = {"shops/products"},produces = "application/json; charset=UTF-8")
    public ResultView getShopsOrProducts(@RequestParam("selectName")String selectName) throws Exception {
        return ResultUtil.returnSuccess(frontService.getShopsOrProducts(selectName));
    }

    @GetMapping(value = {"/product/categories","/groupProduct/categories"},produces = "application/json; charset=UTF-8")
    public ResultView getProductCategories(ProductCategory productCategoryForm) throws Exception {
        return ResultUtil.returnSuccess(frontService.getProductCategories(productCategoryForm));
    }

    @GetMapping(value = "/products",produces = "application/json; charset=UTF-8")
    public ResultView getProducts(Product productForm,@RequestParam(value = "pcategoryName",required = false)String pcategoryName) throws Exception {
        return ResultUtil.returnSuccess(frontService.getProducts(productForm,pcategoryName));
    }

    @GetMapping(value = "/product",produces = "application/json; charset=UTF-8")
    public ResultView getProduct(@RequestParam("productId") Long productId) throws Exception {
        return ResultUtil.returnSuccess(frontService.getProduct(productId));
    }

    @GetMapping(value = "/shops",produces = "application/json; charset=UTF-8")
    public ResultView getShops(Shop shopForm) throws Exception {
        return ResultUtil.returnSuccess(frontService.getShops(shopForm));
    }

    @GetMapping(value = "/shop",produces = "application/json; charset=UTF-8")
    public ResultView getShop(@RequestParam("shopId")Long shopId) throws Exception {
        return ResultUtil.returnSuccess(frontService.getShop(shopId));
    }

    @GetMapping(value = "/shop/comments",produces = "application/json; charset=UTF-8")
    public ResultView getShopComments(ShopComment shopCommentForm) throws Exception {
        return ResultUtil.returnSuccess(frontService.getShopComments(shopCommentForm));
    }

    @GetMapping(value = "/groupProducts",produces = "application/json; charset=UTF-8")
    public ResultView getGroupProducts(GroupProduct groupProductForm) throws Exception {
        return ResultUtil.returnSuccess(frontService.getGroupProducts(groupProductForm));
    }

    @GetMapping(value = "/groupProduct",produces = "application/json; charset=UTF-8")
    public ResultView getGroupProduct(@RequestParam("productId") Long productId) throws Exception {
        return ResultUtil.returnSuccess(frontService.getGroupProduct(productId));
    }

}
