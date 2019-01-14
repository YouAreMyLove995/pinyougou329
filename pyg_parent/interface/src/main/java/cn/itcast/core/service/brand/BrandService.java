package cn.itcast.core.service.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;


public interface BrandService {

    /**
     * 查询所有商品品牌
     * @return
     */
    public List<Brand> findAllBrand();

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize);

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    public PageResult search(Integer pageNum,Integer pageSize,Brand brand);

    /**
     * 添加商品品牌
     * @param brand
     */
    public void add(Brand brand);

    /**
     * 修改时商品品牌回显
     * @param id
     * @return
     */
    Brand findOne(Long id);

    /**
     * 修改商品品牌
     * @param brand
     */
    void update(Brand brand);

    /**
     * 批量删除
     * @param ids
     */
    void delete(Long[] ids);

    /**
     *
     * @return
     */
    public List<Map<String, String>> selectOptionList();

    /**
     * 审核品牌,也就是修改品牌状态
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, Integer status);
}
