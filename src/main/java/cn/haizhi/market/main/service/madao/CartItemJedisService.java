package cn.haizhi.market.main.service.madao;

import cn.haizhi.market.main.bean.madao.*;
import cn.haizhi.market.main.bean.richard.Product;
import cn.haizhi.market.main.mapper.madao.CommonMapper;
import cn.haizhi.market.main.mapper.madao.JedisClient;
import cn.haizhi.market.main.mapper.richard.ProductMapper;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.form.madao.CartItemBaseForm;
import cn.haizhi.market.other.form.madao.CartItemForm;
import cn.haizhi.market.other.form.madao.CartItemSelectForm;
import cn.haizhi.market.other.form.madao.CartShopSelectForm;
import cn.haizhi.market.other.util.IdResultMap;
import cn.haizhi.market.other.util.JsonUtils;
import cn.haizhi.market.other.util.KeyUtil;
import cn.haizhi.market.other.util.PageInfoUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartItemJedisService {
    @Autowired
    private JedisClient jedisClient;

    @Autowired
    private CommonMapper commonMapper;

    @Autowired
    private ProductMapper productMapper;

    @Value("CART_ITEM_PREFIX")
    private String CART_ITEM_PREFIX;

    //添加进购物车
    public Map<String, String> addCartItem(CartItemForm cartItemForm, Long userId){
        //通过商品id查询商品和店铺信息，验证是否存在
        ProductShop productShop = commonMapper.getShopInfoByProductId(cartItemForm.getProductId());
        if(productShop==null || productShop.getShopId()==null) {
            throw new MadaoException(ErrorEnum.PRODUCT_INFO_ERROR, IdResultMap.getIdMap(cartItemForm.getProductId()));
        }
        if(productShop.getProductStock()<cartItemForm.getProductQuantity()){
            throw new MadaoException(ErrorEnum.PRODUCT_STOCK_ERROR, IdResultMap.getIdMap(cartItemForm.getProductId()));
        }

        //判断该项商品是否已在购物车，如果是的话，更新数量，如果不是的话，新建项
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        String cartId = null;
        for(BaseCart baseCart: baseCartList){
            if(cartId==null && baseCart.getShopId().equals(productShop.getShopId())){
                cartId = baseCart.getCartId();
            }
            if(baseCart.getProductId().equals(cartItemForm.getProductId())){
                baseCart.setProductQuantity(cartItemForm.getProductQuantity());
                jedisClient.set(CART_ITEM_PREFIX + ":" + userId +"", JsonUtils.objectToJson(baseCartList));
                Map<String, String> map = new HashMap<>();
                map.put("cartId", baseCart.getCartId());
                map.put("itemId", baseCart.getItemId());
                return map;
            }
        }

        //加入新的商品到List, 并写入redis中
        if(cartId==null)
            cartId = KeyUtil.genUniquKey();
        String itemId = KeyUtil.genUniquKey();
        BaseCart baseCart = new BaseCart();
        baseCart.setCartId(cartId);
        baseCart.setItemId(itemId);
        BeanUtils.copyProperties(cartItemForm, baseCart);
        baseCart.setShopId(productShop.getShopId());
        baseCartList.add(baseCart);
        jedisClient.set(CART_ITEM_PREFIX + ":" + userId +"", JsonUtils.objectToJson(baseCartList));
        Map<String, String> map = new HashMap<>();
        map.put("cartId", baseCart.getCartId());
        map.put("itemId", baseCart.getItemId());
        return map;
    }

    public Map<String,String> addCartItemByOne(CartItemBaseForm form, Long userId) {
        //通过商品id查询商品和店铺信息，验证是否存在
        ProductShop productShop = commonMapper.getShopInfoByProductId(form.getProductId());
        if(productShop==null || productShop.getShopId()==null) {
            throw new MadaoException(ErrorEnum.PRODUCT_INFO_ERROR, IdResultMap.getIdMap(form.getProductId()));
        }

        //判断该项商品是否已在购物车，如果是的话，更新数量，如果不是的话，新建项
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        String cartId = null;
        for(BaseCart baseCart: baseCartList){
            if(cartId==null && baseCart.getShopId().equals(productShop.getShopId())){
                cartId = baseCart.getCartId();
            }
            if(baseCart.getProductId().equals(form.getProductId())){
                if(productShop.getProductStock()< baseCart.getProductQuantity()+1)
                    throw new MadaoException(ErrorEnum.PRODUCT_STOCK_ERROR, IdResultMap.getIdMap(form.getProductId()));
                baseCart.setProductQuantity(baseCart.getProductQuantity()+1);
                jedisClient.set(CART_ITEM_PREFIX + ":" + userId +"", JsonUtils.objectToJson(baseCartList));
                Map<String, String> map = new HashMap<>();
                map.put("cartId", baseCart.getCartId());
                map.put("itemId", baseCart.getItemId());
                return map;
            }
        }

        //加入新的商品到List, 并写入redis中
        if(cartId==null)
            cartId = KeyUtil.genUniquKey();
        String itemId = KeyUtil.genUniquKey();
        BaseCart baseCart = new BaseCart();
        baseCart.setCartId(cartId);
        baseCart.setItemId(itemId);
        BeanUtils.copyProperties(form, baseCart);
        baseCart.setShopId(productShop.getShopId());
        baseCart.setProductQuantity(1);
        baseCartList.add(baseCart);
        jedisClient.set(CART_ITEM_PREFIX + ":" + userId +"", JsonUtils.objectToJson(baseCartList));
        Map<String, String> map = new HashMap<>();
        map.put("cartId", baseCart.getCartId());
        map.put("itemId", baseCart.getItemId());
        return map;
    }



    //根据用户id获取普通购物车项列表并分页
    public PageInfo<CartShopDTO> getCartListByUserIdInPage(Long userId, Integer pageNum, Integer pageSize){
        //获取用户所有的购物车项，并拼装成商店id和购物车项列表的map
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        if (baseCartList.size()==0)
            return new PageInfo<>();
        Map<Long, List<BaseCart>> map = new HashMap<>();
        for(BaseCart baseCart: baseCartList){
            if(!map.containsKey(baseCart.getShopId())){
                map.put(baseCart.getShopId(), new ArrayList<>());
            }
            map.get(baseCart.getShopId()).add(baseCart);
        }

        //得到以商店为分组的购物车项列表
        List<List<BaseCart>> list = new ArrayList<>();
        for (Map.Entry<Long, List<BaseCart>> entry: map.entrySet()){
            list.add(entry.getValue());
        }

        //获取PageInfo对象，截取List获得此次分页所需的购物车项，以商店为分组
        PageInfo<CartShopDTO> pageInfo = PageInfoUtils.getPageInfo(pageNum, pageSize, list.size());
        List<List<BaseCart>> subList = list.subList(pageInfo.getStartRow() - 1 , pageInfo.getEndRow());

        //对每组获得商店信息和商品信息，并拼装成一个CartShopDTO的List
        List<CartShopDTO> cartShopDTOList = new ArrayList<>();
        for(List<BaseCart> shopList: subList){
            CartShopDTO cartShopDTO = new CartShopDTO();
            List<CartItemDTO> cartItemDTOList = new ArrayList<>();
            BaseShop baseShop = commonMapper.getBaseShopById(shopList.get(0).getShopId());
            BeanUtils.copyProperties(baseShop, cartShopDTO);
            cartShopDTO.setCartId(shopList.get(0).getCartId());
            byte isShopSelected = 1;
            for(BaseCart baseCart: shopList){
                CartItemDTO cartItemDTO = new CartItemDTO();
                Product product = productMapper.selectByPrimaryKey(baseCart.getProductId());
                BeanUtils.copyProperties(baseCart, cartItemDTO);
                BeanUtils.copyProperties(product, cartItemDTO);
                cartItemDTO.setShopName(baseShop.getShopName());
                cartItemDTOList.add(cartItemDTO);
                if(baseCart.getIsSelected().equals((byte)0))
                    isShopSelected = 0;
            }
            cartShopDTO.setIsSelected(isShopSelected);
            cartShopDTO.setUserId(cartItemDTOList.get(0).getUserId());
            cartShopDTO.setCartItemDTOList(cartItemDTOList);
            cartShopDTOList.add(cartShopDTO);
        }
        pageInfo.setList(cartShopDTOList);
        return pageInfo;
    }


    //根据id获取普通购物车项列表
    public List<CartShopDTO> getCartListByUserId(Long userId){
        List<CartShopDTO> cartShopDTOList = new ArrayList<>();
        //将json转换成List,并拼装成map
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        if (baseCartList.size()==0)
            return cartShopDTOList;
        Map<Long, List<BaseCart>> map = new HashMap<>();
        for(BaseCart baseCart: baseCartList){
            if(!map.containsKey(baseCart.getShopId())){
                map.put(baseCart.getShopId(), new ArrayList<>());
            }
            map.get(baseCart.getShopId()).add(baseCart);
        }

        //根据map的每项拼装成CartShopDTO
        for(Map.Entry<Long, List<BaseCart>> entry: map.entrySet()){
            CartShopDTO cartShopDTO = new CartShopDTO();
            List<CartItemDTO> cartItemDTOList = new ArrayList<>();
            cartShopDTO.setCartItemDTOList(cartItemDTOList);

            BaseShop baseShop = commonMapper.getBaseShopById(entry.getKey());
            BeanUtils.copyProperties(baseShop, cartShopDTO);
            Byte isSelected = 1;
            for(BaseCart baseCart: entry.getValue()){
                CartItemDTO cartItemDTO = new CartItemDTO();
                Product product = productMapper.selectByPrimaryKey(baseCart.getProductId());
                BeanUtils.copyProperties(baseCart, cartItemDTO);
                BeanUtils.copyProperties(product, cartItemDTO);
                cartItemDTO.setShopName(baseShop.getShopName());
                cartItemDTOList.add(cartItemDTO);
                if(baseCart.getIsSelected().equals((byte)0)){
                    isSelected = 0;
                }
            }
            cartShopDTO.setIsSelected(isSelected);
            cartShopDTO.setUserId(cartShopDTO.getCartItemDTOList().get(0).getUserId());
            cartShopDTO.setCartId(entry.getValue().get(0).getCartId());
            cartShopDTOList.add(cartShopDTO);
        }
        return cartShopDTOList;
    }

    //根据传入的购物车id获取CartItemDTO的List
    public List<CartItemDTO> getCartItemDTOByCartItemIdList(Long userId, List<String> cartItemIdList){
        List<CartItemDTO> cartItemDTOList = new ArrayList<>();
        List<String> cartItemIdList1 = new ArrayList<>(cartItemIdList);
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        for(BaseCart baseCart: baseCartList){
            if(cartItemIdList.contains(baseCart.getItemId())){
                CartItemDTO cartItemDTO = new CartItemDTO();
                BeanUtils.copyProperties(baseCart, cartItemDTO);
                Product product = productMapper.selectByPrimaryKey(baseCart.getProductId());
                BeanUtils.copyProperties(product, cartItemDTO);
                cartItemDTOList.add(cartItemDTO);
                cartItemIdList1.remove(baseCart.getItemId());
            }
        }
        if(cartItemIdList1.size()!=0)
            throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(cartItemIdList));
        return cartItemDTOList;
    }


    //根据id删除购物车项
    public void deleteCartItemById(Long userId, String cartItemId){
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        boolean flag = false;
        for(BaseCart baseCart: baseCartList){
            if (baseCart.getItemId().equals(cartItemId)){
                baseCartList.remove(baseCart);
                flag = true;
                break;
            }
        }
        if (flag==false)
            throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(cartItemId));
        else
            jedisClient.set(CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(baseCartList));
    }


    public void deleteCartItemList(Long userId, List<String> cartItemIdList){
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        List<BaseCart> removeCart = new ArrayList<>();
        List<String> cartItemIdList1 = new ArrayList<>(cartItemIdList);

        for(BaseCart baseCart: baseCartList){
            if(cartItemIdList1.contains(baseCart.getItemId())){
                removeCart.add(baseCart);
                cartItemIdList1.remove(baseCart.getItemId());
            }
        }

        if(cartItemIdList1.size()!=0){
            throw new MadaoException(ErrorEnum.CARTITEM_DELETE_FAIL, IdResultMap.getIdMap(cartItemIdList));
        }

        baseCartList.removeAll(removeCart);
        jedisClient.set(CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(baseCartList));
    }

    //根据用户id删除购物车项,即 清空该用户的购物车
    public void deleteCartItemByUserId(Long userId){
        jedisClient.expire(CART_ITEM_PREFIX + ":" + userId, 0);
    }

    //更新购物车项数量，数量的规范控制在表单验证那里
    public void updateCartItemQuantity(Long userId, String itemId, int quantity){
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        boolean flag = false;
        for(BaseCart baseCart: baseCartList){
            if (baseCart.getItemId().equals(itemId)){
                flag = true;
                Product product = productMapper.selectByPrimaryKey(baseCart.getProductId());
                if (product.getProductStock()<quantity){
                    throw new MadaoException(ErrorEnum.PRODUCT_STOCK_ERROR, IdResultMap.getIdMap(baseCart.getItemId()));
                }
                baseCart.setProductQuantity(quantity);
                break;
            }
        }
        if (flag==false){
            throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(itemId));
        }else{
            jedisClient.set(CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(baseCartList));
        }

    }

    //设置商品选中状态
    public void selectCartItem(CartItemSelectForm form, Long userId){
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        for(BaseCart baseCart: baseCartList){
            if(form.getCartItemIdList().contains(baseCart.getItemId())) {
                form.getCartItemIdList().remove(baseCart.getItemId());
                baseCart.setIsSelected(form.getIsSelected());
            }
        }
        if(form.getCartItemIdList().size()!=0)
            throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(form.getCartItemIdList()));

        jedisClient.set(CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(baseCartList));
    }

    //设置商店选中状态
    public void selectCartItemByShopId(CartShopSelectForm form, Long userId){
        List<BaseCart> baseCartList = getBaseCartListByUserId(userId);
        boolean flag = false;
        for (BaseCart baseCart: baseCartList){
            if (baseCart.getCartId().equals(form.getCartId())){
                baseCart.setIsSelected(form.getIsSelected());
                flag = true;
            }
        }
        if(!flag)
            throw new MadaoException(ErrorEnum.CART_NOT_EXIT, IdResultMap.getIdMap(form.getCartId()));
        jedisClient.set(CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(baseCartList));
    }




    public List<BaseCart> getBaseCartListByUserId(Long userId){
        String result = jedisClient.get(CART_ITEM_PREFIX + ":" + userId);
        if(result==null || result==""){
            return new ArrayList<>();
        }
        return JsonUtils.jsonToList(result, BaseCart.class);
    }


}
