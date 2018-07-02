//package cn.haizhi.market.main.service.madao;
//
//
//import cn.haizhi.market.main.bean.madao.CartItemDTO;
//import cn.haizhi.market.main.bean.madao.PgCartItem;
//import cn.haizhi.market.main.bean.madao.PgCartItemDTO;
//import cn.haizhi.market.other.form.madao.OrderCreateForm;
//import cn.haizhi.market.other.form.madao.OrderIdForm;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.*;
//
//@RunWith(value = SpringJUnit4ClassRunner.class)
//@ContextConfiguration("classpath:spring/*.xml")
//public class CartItemJedisServiceTest {
//
//    @Autowired
//    private OrderService orderService;
//    @Autowired
//    private CartItemJedisService cartItemJedisService;
//    @Autowired
//    private PgCartItemJedisService pgCartItemJedisService;
//    @org.junit.Test
//    public void addCartItem() {
//        OrderCreateForm form = new OrderCreateForm();
//        form.setAddressId(1L);
//        form.setUserId(1L);
//        form.setUserName("aa");
//        List<String> list = new ArrayList<>();
//        list.add("1521781847582549311");
//
//        form.setCartItemIdList(list);
//        System.out.println(orderService.addOrder(form));
//    }
//
//    @Test
//    public void test(){
//        List<String> list = new ArrayList<>();
//        list.add("1521781847582549311");
//        List<CartItemDTO> resultList = cartItemJedisService.getCartItemDTOByCartItemIdList(2L, list);
//        for (CartItemDTO cartItemDTO: resultList){
//            System.out.println(cartItemDTO);
//        }
//
//    }
//
//    @Test
//    public void test2(){
//        List<String> list = new ArrayList<>();
//        list.add("1521868959106184894");
//        List<PgCartItemDTO> resultList = pgCartItemJedisService.getPgCartItemDTOByCartItemIdList(3L, list);
//        for (PgCartItemDTO pgCartItemDTO: resultList){
//            System.out.println(pgCartItemDTO);
//        }
//
//    }
//}
