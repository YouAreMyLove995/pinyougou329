package cn.itcast.core.controller.sekill;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import cn.itcast.core.service.seckill.SeckillGoodsService;
import cn.itcast.core.service.seckill.SeckillOrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sekillOrder")
public class sekillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 查询秒杀商品订单
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/searchOrder.do")
    public PageResult searchOrder(Integer page, Integer rows, @RequestBody SeckillOrder seckillOrder){
        PageResult pageResult = seckillOrderService.searchOrder(page, rows,seckillOrder);
        return pageResult;
    }
}
