package cn.itcast.core.controller.sekill;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import cn.itcast.core.service.seckill.SeckillGoodsService;
import cn.itcast.core.service.seckill.SeckillOrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sekill")
public class sekillController {

    @Reference
    private SeckillGoodsService seckillGoodsService;

    /**
     * 条件查询未通过审核的秒杀商品
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody SeckillGoods seckillGoods){
        PageResult pageResult = seckillGoodsService.search(page, rows,seckillGoods);
        return pageResult;
    }

    /**
     * 审核秒杀商品,也就是修改秒杀商品状态
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids, String status){
        try {
            seckillGoodsService.updateStatus(ids,status);
            return new Result(true,"审核成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败喽");
        }
    }

}
