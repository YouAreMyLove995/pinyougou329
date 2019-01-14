package cn.itcast.core.controller.cart;

import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.cart.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    /**
     * 添加购物项到购物车
     * @param itemId
     * @param num
     * @param response
     * @param request
     * @return
     */
    @RequestMapping("/addGoodsToCartList.do")
    @CrossOrigin(origins = {"http://localhost:9107"})
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletResponse response, HttpServletRequest request){
        try {
            String name = SecurityContextHolder.getContext().getAuthentication().getName();

            //设置一个标识，判断本地是否有购物车，如果有，在合并到服务器端购物车的时候需要清空本地购物车
            boolean flag = false;
            //将商品添加到购物车

            //创建一个空的购物车
            List<Cart> cartList = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length>0){
                for (Cookie cookie : cookies) {
                    //判断是否已经有购物车
                    if ("BUYER_CART".equals(cookie.getName())){
                        //如果有就用这个
                        cartList = JSON.parseArray(URLDecoder.decode(cookie.getValue(),"utf-8"),Cart.class);

                        flag = true;

                        break;
                    }
                }
            }
            if (cartList == null){
                //如果没有购物车则说明是第一次购物，创建一个
                cartList = new ArrayList<>();
            }

            //商品装车 封装商品
            Cart cart = new Cart();
            Item item = cartService.findOne(itemId);
                //封装商家id信息
            cart.setSellerId(item.getSellerId());
                //封装orderItemList购物项集合,在这里只需要放入库存id 和 num ，为了给cookie瘦身
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = new OrderItem();

            orderItem.setItemId(itemId);
            orderItem.setNum(num);
            orderItemList.add(orderItem);
            //设置商品选项集
            cart.setOrderItemList(orderItemList);
            //判断是否是同一个商家
            int cartIndexOf = cartList.indexOf(cart);
            if (cartIndexOf != -1){
                //如果是，就继续把商品往这个商家里加

                Cart oldCart = cartList.get(cartIndexOf);
                int orderItemIndexOf = oldCart.getOrderItemList().indexOf(orderItem);
                    //判断商家是否有相同的商品
                if (orderItemIndexOf != -1){
                    //如果有，只加num数即可
                    OrderItem orderItem1 = oldCart.getOrderItemList().get(orderItemIndexOf);
                    orderItem1.setNum(orderItem1.getNum()+num);
                }else{
                    //如果没有，就把商品放入这个商家的购物项集合中即可
                    oldCart.getOrderItemList().add(orderItem);
                }

            }else {
                //如果不是，直接装车
                cartList.add(cart);
            }

            if (!"anonymousUser".equals(name)){
                //如果用户登录，合并本地购物车和服务器端购物车
                cartService.mergeCartList(cartList,name);
                if (flag){
                    Cookie cookie = new Cookie("BUYER_CART", null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }else {
                //将购物车保存到本地cookie
                Cookie cookie = new Cookie("BUYER_CART", URLEncoder.encode(JSON.toJSONString(cartList), "utf-8"));
                cookie.setMaxAge(60*60*24);
                cookie.setPath("/");//设置cookie共享
                response.addCookie(cookie);
            }


//            response.setHeader("Access-Control-Allow-Origin","http://localhost:9107");
//            response.setHeader("Access-Control-Allow-Credentials","true");
            return new Result(true,"添加购物车成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败了");
        }
    }

    /**
     * 回显购物车数据
     * @return
     */
    @RequestMapping("/findCartList.do")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException {
        //创建一个空的购物车
        List<Cart> cartList = null;
        //判断本地是否有购物车
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length>0){
            for (Cookie cookie : cookies) {
                //判断是否已经有购物车
                if ("BUYER_CART".equals(cookie.getName())){
                    //如果有就拿到这个
                    cartList = JSON.parseArray(URLDecoder.decode(cookie.getValue(),"utf-8"),Cart.class);
                    break;
                }
            }
        }

        //判断是否登录，如果登录就拿到redis中的购物车
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        if(!"anonymousUser".equals(name)){
            //如果已登录，将本地购物车合并到redis中的购物车
            if (cartList != null){
                cartService.mergeCartList(cartList,name);
                Cookie cookie = new Cookie("BUYER_CART", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            //从redis中获取购物车
            cartList = cartService.findCartListFromRedis(name);
        }

        //判断购物车是否为空
        if(cartList != null){
            //填充购物车中缺少的数据
            cartList = cartService.findCartList(cartList);
        }
        //返回购物车
        return cartList;
    }
}
