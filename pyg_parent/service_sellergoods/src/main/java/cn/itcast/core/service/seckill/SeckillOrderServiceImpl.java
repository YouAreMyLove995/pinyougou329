package cn.itcast.core.service.seckill;

import cn.itcast.core.dao.seckill.SeckillOrderDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import cn.itcast.core.pojo.seckill.SeckillOrderQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Resource
    private SeckillOrderDao seckillOrderDao;

    /**
     * 查询秒杀商品订单
     * @param page
     * @param rows
     * @param seckillOrder
     * @return
     */
    @Override
    public PageResult searchOrder(Integer page, Integer rows, SeckillOrder seckillOrder) {
        //设置分页参数
        PageHelper.startPage(page,rows);
        //设置查询条件
        SeckillOrderQuery seckillOrderQuery = new SeckillOrderQuery();
        SeckillOrderQuery.Criteria criteria = seckillOrderQuery.createCriteria();
        if (seckillOrder.getSellerId() !=null && !"".equals(seckillOrder.getSellerId().trim())){
            criteria.andSellerIdEqualTo("%"+seckillOrder.getSellerId().trim()+"%");
        }
        if (seckillOrder.getStatus() != null && !"".equals(seckillOrder.getStatus().trim())){
            criteria.andStatusEqualTo(seckillOrder.getStatus().trim());
        }

        //根據id降序排列
        PageHelper.orderBy("id desc");

        //查询结果集
        Page<SeckillOrder> page1 = (Page<SeckillOrder>) seckillOrderDao.selectByExample(seckillOrderQuery);
        //创建pageResult并填充结果集
        return new PageResult(page1.getTotal(),page1.getResult());
    }
}
