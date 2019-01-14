package cn.itcast.core.service.order;


import cn.itcast.core.pojo.order.Order;

public interface OrderService {

    //用户提交订单
    void add(Order order, String name);
}
