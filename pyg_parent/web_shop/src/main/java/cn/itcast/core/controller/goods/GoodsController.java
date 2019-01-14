package cn.itcast.core.controller.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.goods.GoodsService;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 添加商品
     * @param goodsVo
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody GoodsVo goodsVo){
        try {
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsVo.getGoods().setSellerId(sellerId);
            goodsService.add(goodsVo);
            return new Result(true,"添加商品成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加商品失败喽");
        }
    }

    /**
     * 查询此商家商品
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){
        //将商家id存入goods
        String seller_id = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(seller_id);
        //查询并返回
        return goodsService.search(page,rows,goods);
    }

    /**
     * 商品回显
     * @return
     */
    @RequestMapping("/findOne.do")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }

    /**
     * 商品更新
     * @param goodsVo
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody GoodsVo goodsVo){
        try {
            goodsService.update(goodsVo);
            return new Result(true,"更新成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败喽");
        }
    }

    /**
     * 是否上架
     * @param ids
     * @param is_marketable
     * @return
     */
    @RequestMapping("/updateIs_marketable.do")
    public Result updateIs_marketable(Long[] ids,String is_marketable){
        try {
            goodsService.updateIs_marketable(ids,is_marketable);
            return new Result(true,"修改成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败喽");
        }
    }
}
