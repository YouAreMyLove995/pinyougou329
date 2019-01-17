package cn.itcast.core.service.seckill;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    /**
     * 条件查询未通过审核的秒杀商品
     * @param page
     * @param rows
     * @param seckillGoods
     * @return
     */
    PageResult search(Integer page, Integer rows, SeckillGoods seckillGoods);

    /**
     * 审核秒杀商品,也就是修改秒杀商品状态
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, String status);
}
