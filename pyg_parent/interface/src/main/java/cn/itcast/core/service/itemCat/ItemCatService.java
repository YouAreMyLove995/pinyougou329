package cn.itcast.core.service.itemCat;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {

    /**
     * 商品分类列表查询
     * @param parentId
     * @return
     */
    public List<ItemCat> findByParentId(Long parentId);

    /**
     *添加商品分类
     * @param itemCat
     */
    void add(ItemCat itemCat);

    /**
     * 修改商品分类时回显数据
     * @param id
     * @return
     */
    ItemCat findOne(Long id);

    /**
     * 更新商品分类
     * @param itemCat
     */
    void update(ItemCat itemCat);

    /**
     * 批量删除商品分类
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 查询所有分类信息
     * @return
     */
    List<ItemCat> findAll();

    /**
     * 审核分类,也就是修改分类状态
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, Integer status);

    /**
     * 分页查询
     * @param page
     * @param rows
     * @return
     */
    PageResult findPage(int page, int rows);
}
