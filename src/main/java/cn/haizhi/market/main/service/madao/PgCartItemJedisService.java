package cn.haizhi.market.main.service.madao;

import cn.haizhi.market.main.bean.madao.*;
import cn.haizhi.market.main.bean.richard.GroupProduct;
import cn.haizhi.market.main.mapper.madao.CommonMapper;
import cn.haizhi.market.main.mapper.madao.JedisClient;
import cn.haizhi.market.main.mapper.madao.PgCartItemMapper;
import cn.haizhi.market.main.mapper.richard.GroupProductMapper;
import cn.haizhi.market.other.enums.madao.ErrorEnum;
import cn.haizhi.market.other.exception.MadaoException;
import cn.haizhi.market.other.form.madao.*;
import cn.haizhi.market.other.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PgCartItemJedisService {
   @Autowired
    private PgCartItemMapper pgCartItemMapper;

   @Autowired
   private CommonMapper commonMapper;

   @Autowired
   private GroupProductMapper groupProductMapper;

   @Autowired
   private JedisClient jedisClient;

   @Value("PG_CART_ITEM_PREFIX")
   private String PG_CART_ITEM_PREFIX;

   public String addCartItem(PgCartItemForm pgCartItemForm, Long userId){
       //根据传入的productId判断该商品是否存在
       GroupProduct groupProduct = groupProductMapper.selectByPrimaryKey(pgCartItemForm.getProductId());
       if(groupProduct==null)
           throw new MadaoException(ErrorEnum.PRODUCT_NOT_EXIST, IdResultMap.getIdMap(pgCartItemForm.getProductId()));
       //如果商品库存不足就抛出异常
       if(groupProduct.getProductStock()<pgCartItemForm.getProductQuantity()){
           throw new MadaoException(ErrorEnum.PRODUCT_STOCK_ERROR, IdResultMap.getIdMap(pgCartItemForm.getProductId()));
       }

       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);

       //判断该用户的购物车是否已经有该商品
       boolean flag = false;
       String itemId = null;
       for(BasePgCart basePgCart: basePgCartList){
           //如果已存在该商品，设置数量
           if (basePgCart.getProductId().equals(pgCartItemForm.getProductId())){
               basePgCart.setProductQuantity(pgCartItemForm.getProductQuantity());
               flag = true;
               itemId = basePgCart.getItemId();
               break;
           }
       }

       //如果不存在，新建购物车项
       if (flag==false){
           BasePgCart basePgCart = new BasePgCart();
           basePgCart.setProductId(pgCartItemForm.getProductId());
           basePgCart.setProductQuantity(pgCartItemForm.getProductQuantity());
           basePgCart.setItemId(KeyUtil.genUniquKey());
           basePgCartList.add(basePgCart);
           itemId = basePgCart.getItemId();
       }
       jedisClient.set(PG_CART_ITEM_PREFIX+":" + userId, JsonUtils.objectToJson(basePgCartList));
       return itemId;
   }

   public List<PgCartItemDTO> getPgCartItemList(Long userId){
       List<PgCartItemDTO> pgCartItemDTOList = new ArrayList<>();
       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);
       for(BasePgCart basePgCart: basePgCartList){
           PgCartItemDTO pgCartItemDTO = new PgCartItemDTO();
           GroupProduct groupProduct = groupProductMapper.selectByPrimaryKey(basePgCart.getProductId());
           String picPath = commonMapper.getGroupProductPicById(basePgCart.getProductId());
           BeanUtils.copyProperties(groupProduct, pgCartItemDTO);
           BeanUtils.copyProperties(basePgCart, pgCartItemDTO);
           pgCartItemDTO.setPicturePath(picPath);
           pgCartItemDTO.setUserId(userId);
           pgCartItemDTOList.add(pgCartItemDTO);
       }
       return pgCartItemDTOList;
   }

   public PageInfo<PgCartItemDTO> getPgCartItemListInPage(Long userId, int pageNum, int pageSize){
       List<PgCartItemDTO> pgCartItemDTOList = new ArrayList<>();
       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);
       if (basePgCartList.size()==0)
           return new PageInfo<>();
       PageInfo<PgCartItemDTO> pageInfo = PageInfoUtils.getPageInfo(pageNum, pageSize, basePgCartList.size());
       List<BasePgCart> subList = basePgCartList.subList(pageInfo.getStartRow()-1, pageInfo.getEndRow());

       for(BasePgCart basePgCart: subList){
           PgCartItemDTO pgCartItemDTO = new PgCartItemDTO();
           GroupProduct groupProduct = groupProductMapper.selectByPrimaryKey(basePgCart.getProductId());
           String picPath = commonMapper.getGroupProductPicById(basePgCart.getProductId());
           BeanUtils.copyProperties(groupProduct, pgCartItemDTO);
           BeanUtils.copyProperties(basePgCart, pgCartItemDTO);
           pgCartItemDTO.setPicturePath(picPath);
           pgCartItemDTO.setUserId(userId);
           pgCartItemDTOList.add(pgCartItemDTO);
       }
       pageInfo.setList(pgCartItemDTOList);
       return pageInfo;
   }

   public List<PgCartItemDTO> getPgCartItemDTOByCartItemIdList(Long userId, List<String> cartItemIdList){
       List<PgCartItemDTO> pgCartItemDTOList = new ArrayList<>();
       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);
       List<String> cartItemIdList1 = new ArrayList<>(cartItemIdList);
       for(BasePgCart basePgCart: basePgCartList){
           if(cartItemIdList.contains(basePgCart.getItemId())){
               PgCartItemDTO pgCartItemDTO =  new PgCartItemDTO();
               BeanUtils.copyProperties(basePgCart, pgCartItemDTO);
               GroupProduct groupProduct = groupProductMapper.selectByPrimaryKey(basePgCart.getProductId());
               BeanUtils.copyProperties(groupProduct, pgCartItemDTO);
               pgCartItemDTO.setUserId(userId);
               String picPath = commonMapper.getGroupProductPicById(basePgCart.getProductId());
               pgCartItemDTO.setPicturePath(picPath);
               pgCartItemDTOList.add(pgCartItemDTO);
               cartItemIdList1.remove(basePgCart.getItemId());
           }
       }
       if(cartItemIdList1.size()!=0){
           throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(cartItemIdList));
       }
       return pgCartItemDTOList;
   }

   public void setPgCartItemQuantity(CartItemReviseForm cartItemReviseForm, Long userId){
       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);
       boolean flag = false;
       for(BasePgCart basePgCart: basePgCartList){
           if(basePgCart.getItemId().equals(cartItemReviseForm.getCartItemId())){
               flag = true;
               GroupProduct groupProduct = groupProductMapper.selectByPrimaryKey(basePgCart.getProductId());
               if(groupProduct.getProductStock() < cartItemReviseForm.getQuantity())
                   throw new MadaoException(ErrorEnum.PRODUCT_STOCK_ERROR, IdResultMap.getIdMap(basePgCart.getProductId()));
               basePgCart.setProductQuantity(cartItemReviseForm.getQuantity());
               break;
           }
       }
       if(!flag){
           throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(cartItemReviseForm.getCartItemId()));
       }else{
           jedisClient.set(PG_CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(basePgCartList));
       }
   }

   public void deleteCartItemById(CartItemDeleteForm cartItemDeleteForm, Long userId){
       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);
       boolean flag = false;
       for(BasePgCart basePgCart: basePgCartList){
           if (basePgCart.getItemId().equals(cartItemDeleteForm.getCartItemId())){
               flag = true;
               basePgCartList.remove(basePgCart);
               break;
           }
       }
       if (!flag)
           throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(cartItemDeleteForm.getCartItemId()));
       else
           jedisClient.set(PG_CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(basePgCartList));

   }

   // 删除用户多个购物车项
   public void deleteCartItemListById(Long userId, List<String> cartItemIdList){
       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);
       List<BasePgCart> removePgCartList = new ArrayList<>();
       List<String> cartItemIdList1 = new ArrayList<>(cartItemIdList);
       for(BasePgCart basePgCart: basePgCartList){
           if(cartItemIdList1.contains(basePgCart.getItemId())){
               removePgCartList.add(basePgCart);
               cartItemIdList1.remove(basePgCart.getItemId());
           }
       }

       if(cartItemIdList1.size()!=0){
           throw new MadaoException(ErrorEnum.CARTITEM_DELETE_FAIL, IdResultMap.getIdMap(cartItemIdList));
       }

       basePgCartList.removeAll(removePgCartList);
       jedisClient.set(PG_CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(basePgCartList));
   }

   //设置购物车项选中状态
   public void selectCartItem(CartItemSelectForm form, Long userId){
       List<BasePgCart> basePgCartList = getBasePgCartListByUserId(userId);
       List<String> cartItemIdList = form.getCartItemIdList();
       for (BasePgCart basePgCart: basePgCartList){
           if(cartItemIdList.contains(basePgCart.getItemId())){
               basePgCart.setIsSelected(form.getIsSelected());
               cartItemIdList.remove(basePgCart.getItemId());
           }
       }
       if(cartItemIdList.size()!=0){
           throw new MadaoException(ErrorEnum.CARTITEM_NOT_EXIST, IdResultMap.getIdMap(cartItemIdList));
       }
       jedisClient.set(PG_CART_ITEM_PREFIX + ":" + userId, JsonUtils.objectToJson(basePgCartList));
   }

   //清空用户购物车
   public void deleteCartItemByUserId(Long userId){
       jedisClient.expire(PG_CART_ITEM_PREFIX + ":" + userId, 0);
   }

   //根据用户id从redis中获取购物车
   public List<BasePgCart> getBasePgCartListByUserId(Long userId){
       String json = jedisClient.get(PG_CART_ITEM_PREFIX + ":" + userId);
       if(json=="" || json==null){
           return new ArrayList<>();
       }
       return JsonUtils.jsonToList(json, BasePgCart.class);
   }
}
