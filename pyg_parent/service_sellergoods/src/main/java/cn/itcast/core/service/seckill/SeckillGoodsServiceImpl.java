package cn.itcast.core.service.seckill;

import cn.itcast.core.dao.seckill.SeckillGoodsDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillGoodsQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Resource
    private SeckillGoodsDao seckillGoodsDao;

    /**
     * 条件查询未通过审核的秒杀商品
     * @param page
     * @param rows
     * @param seckillGoods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, SeckillGoods seckillGoods) {
        //设置分页参数
        PageHelper.startPage(page,rows);
        //设置查询条件
        SeckillGoodsQuery seckillGoodsQuery = new SeckillGoodsQuery();
        seckillGoodsQuery.createCriteria().andStatusEqualTo("0");

        //根據id降序排列
        PageHelper.orderBy("id desc");

        //查询结果集
        Page<SeckillGoods> page1 = (Page<SeckillGoods>) seckillGoodsDao.selectByExample(seckillGoodsQuery);
        //创建pageResult并填充结果集
        return new PageResult(page1.getTotal(),page1.getResult());

    }

    /**
     * 审核秒杀商品,也就是修改秒杀商品状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids!=null && ids.length>0){
            SeckillGoods seckillGoods = new SeckillGoods();
            seckillGoods.setStatus(status);
            for (final Long id : ids) {
                seckillGoods.setId(id);
                seckillGoodsDao.updateByPrimaryKeySelective(seckillGoods);
            }
        }
    }
}
