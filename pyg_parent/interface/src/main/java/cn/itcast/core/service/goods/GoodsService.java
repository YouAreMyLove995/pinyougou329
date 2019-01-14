package cn.itcast.core.service.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.vo.GoodsVo;

public interface GoodsService {

    /**
     * 添加商品
     * @param goodsVo
     */
    public void add(GoodsVo goodsVo);

    /**
     * 查询此商家商品
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    PageResult search(Integer page, Integer rows, Goods goods);

    /**
     * 商品回显
     * @param id
     * @return
     */
    GoodsVo findOne(Long id);

    /**
     * 商品更新
     * @param goodsVo
     */
    void update(GoodsVo goodsVo);

    /**
     * 运营商系统查询待审核的商品
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    PageResult searchByManager(Integer page,Integer rows,Goods goods);


    /**
     * 审核商品,也就是修改商品状态
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, String status);


    /**
     * 运营商删除商品
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 是否上架
     * @param ids
     * @param is_marketable
     */
    void updateIs_marketable(Long[] ids, String is_marketable);

}
