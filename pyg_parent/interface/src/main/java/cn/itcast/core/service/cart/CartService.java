package cn.itcast.core.service.cart;

import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;

import java.util.List;

public interface CartService {
    /**
     * 根据id查询库存信息
     * @param id
     * @return
     */
    public Item findOne(Long id);


    /**
     * 返回详细的购物车信息用于回显数据
     * @param cartList
     * @return
     */
    List<Cart> findCartList(List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList
     * @param name
     */
    void mergeCartList(List<Cart> cartList, String name);

    /**
     * 从redis中获取购物车
     * @param name
     * @return
     */
    List<Cart> findCartListFromRedis(String name);

}
