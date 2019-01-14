package cn.itcast.core.service.order;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.cart.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.util.uniqueKey.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IdWorker idWorker;

    @Resource
    private ItemDao itemDao;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private OrderDao orderDao;

    @Resource
    private PayLogDao payLogDao;

    /**
     * 用户提交订单
     * @param order
     * @param name
     */
    @Transactional
    @Override
    public void add(Order order, String name) {

        //获取该用户的购物车信息
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(name);

        if (cartList != null && cartList.size()>0){
            //定义一个变量保存支付日志中的总金额
            double logTotalPrice = 0f;
            //定义一个集合保存订单编号列表
            ArrayList<Long> orderIdList = new ArrayList<>();

            //生成订单时需注意：一个商家对应一个订单，这样比较合理
            for (Cart cart : cartList) {
                //保存订单信息
                long orderId = idWorker.nextId();
                orderIdList.add(orderId);
                order.setOrderId(orderId);//设置订单id
                order.setStatus("1");//设置订单状态
                order.setCreateTime(new Date());//设置订单创建时间
                order.setUserId(name);//设置用户id，这里其实设置的是用户名
                order.setSourceType("2");//设置订单来源 PC端
                order.setSellerId(cart.getSellerId());//设置商家id
                //定义一个变量，在添加订单详情时用于保存订单总价
                double totalPrice = 0f;
                //获取购物项
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if (orderItemList != null && orderItemList.size()>0){
                    for (OrderItem orderItem : orderItemList) {
                        //保存订单详情信息
                        long orderItemId = idWorker.nextId();
                        orderItem.setId(orderItemId);//设置订单详情id
                        orderItem.setOrderId(orderId);//设置对应的订单id
                        //获取对应库存信息
                        Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                        orderItem.setGoodsId(item.getGoodsId());//设置商品id
                        orderItem.setTitle(item.getTitle());//设置订单详情的标题
                        orderItem.setPrice(new BigDecimal(item.getPrice().doubleValue()));//设置商品单价
                        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum()));//设置总价
                        orderItem.setSellerId(cart.getSellerId());//设置商家id
                        //遍历每一个商品就把总价++
                        totalPrice += item.getPrice().doubleValue() * orderItem.getNum();

                        //将订单详情加入数据库
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                logTotalPrice += totalPrice;
                order.setPayment(new BigDecimal(totalPrice));//设置订单总金额
                //将订单加入数据库
                orderDao.insertSelective(order);
            }
            //提交订单时生成日志
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));//设置支付订单号
            payLog.setCreateTime(new Date());//设置创建日期
            payLog.setTotalFee((long) logTotalPrice*100);//设置支付金额（分）
            payLog.setUserId(name); //设置用户ID
            payLog.setTradeState("0");//设置交易状态
            payLog.setOrderList(orderIdList.toString().replace("[","").replace("]",""));//设置订单编号列表
            payLog.setPayType("1");//设置支付类型
            payLogDao.insertSelective(payLog);
            //将这个日志放入缓存中，调用微信支付接口时还需要这里的数据
            redisTemplate.boundHashOps("payLog").put(name,payLog);
        }

        //订单提交完成后，删除redis中缓存的购物车信息
        redisTemplate.boundHashOps("BUYER_CART").delete(name);
    }
}
