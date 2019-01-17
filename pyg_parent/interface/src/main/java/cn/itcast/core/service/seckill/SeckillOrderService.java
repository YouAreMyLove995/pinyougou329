package cn.itcast.core.service.seckill;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillOrder;


public interface SeckillOrderService {
    /**
     * 查询秒杀商品订单
     * @param page
     * @param rows
     * @param seckillOrder
     * @return
     */
    PageResult searchOrder(Integer page, Integer rows, SeckillOrder seckillOrder);
}
