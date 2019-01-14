package cn.itcast.core.service.seckill;

import cn.itcast.core.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    /**
     * 返回正在参与秒杀的商品
     * @return
     */
    public List<SeckillGoods> findList();
}
