package cn.itcast.core.service.cart;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private ItemDao itemDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 根据id查询库存信息
     * @param id
     * @return
     */
    @Override
    public Item findOne(Long id) {
        return itemDao.selectByPrimaryKey(id);
    }


    /**
     * 返回详细的购物车信息用于回显数据
     * @param cartList
     * @return
     */
    @Override
    public List<Cart> findCartList(List<Cart> cartList) {
        for (Cart cart : cartList) {
            //添加店铺名称
            Seller seller = sellerDao.selectByPrimaryKey(cart.getSellerId());
            cart.setSellerName(seller.getNickName());

            //添加商品库存中的详细信息
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                orderItem.setPicPath(item.getImage());//设置商品图片
                orderItem.setTitle(item.getTitle());//设置商品标题
                orderItem.setPrice(item.getPrice());//设置商品单价
                //设置商品小计
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));
            }
        }
        return cartList;
    }

    /**
     * 合并购物车
     * @param newCartList
     * @param name
     */
    @Override
    public void mergeCartList(List<Cart> newCartList, String name) {
        //从redis中取出老车
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);
        //将新车与老车进行合并
        oldCartList = mergeNewCartListToOldCartList(newCartList,oldCartList);
        //将合并好的车再放入redis中
        redisTemplate.boundHashOps("BUYER_CART").put(name,oldCartList);
    }

    /**
     * 从redis中获取购物车
     * @param name
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);
        return cartList;
    }


    //================================================================================================================

    /**
     * 将新车与老车进行合并的函数
     * @param newCartList
     * @param oldCartList
     * @return
     */
    private List<Cart> mergeNewCartListToOldCartList(List<Cart> newCartList, List<Cart> oldCartList) {
        if (oldCartList != null){
            if (newCartList != null){
                //如果新车老车都不为空，进行合并，将新车中的数据加入老车中
                for (Cart newCart : newCartList) {
                    //判断老车中是否存在该商家
                    int sellerIndexOf = oldCartList.indexOf(newCart);
                    if (sellerIndexOf != -1){
                        //说明存在该商家，继续在该商家下添加商品
                        //紧接着判断是否存在该商品
                        Cart oldCart = oldCartList.get(sellerIndexOf);
                        List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        for (OrderItem newOrderItem : newOrderItemList) {
                            //判断老车中对应商家是否存在该商品
                            int itemIndexOf = oldOrderItemList.indexOf(newOrderItem);
                            if (itemIndexOf != -1){
                                //存在就合并商品的数量
                                OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);
                                //获取老车中的商品数量
                                Integer oldOrderItemNum = oldOrderItem.getNum();
                                Integer newOrderItemNum = newOrderItem.getNum();
                                oldOrderItem.setNum(oldOrderItemNum+newOrderItemNum);
                            }else{
                                //不存在，直接在该商家下添加商品
                                oldOrderItemList.add(newOrderItem);
                            }
                        }

                    }else {
                        //等于-1说明老车中不存在该商家，直接添加即可
                        oldCartList.add(newCart);
                    }
                }

            }else {
                //如果新车为空，直接返回老车
                return oldCartList;
            }
        }else {
            //如果老车为空,直接返回新车
            return newCartList;
        }
        //将新车合并到老车，最后返回老车
        return oldCartList;
    }
}
