package cn.itcast.core.service.brand;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.pojo.good.Goods;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.jms.core.MessageCreator;


import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;


@Service
public class BrandServiceImpl implements BrandService {

    @Resource
    private BrandDao brandDao;


    /**
     * 查询所有商品品牌
     * @return
     */
    @Override
    public List<Brand> findAllBrand() {
        return brandDao.selectByExample(null);
    }

    /**
     * 分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        //设置分页参数
        PageHelper.startPage(pageNum,pageSize);
        //查询结果集
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        //创建pageResult并填充数据
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        //设置分页参数
        PageHelper.startPage(pageNum,pageSize);
        //设置查询条件
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if (brand.getName() !=null && !"".equals(brand.getName().trim())){
            criteria.andNameLike("%"+brand.getName().trim()+"%");
        }
        if (brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())){
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }

        //根據id降序排列
        PageHelper.orderBy("id desc");

        //查询结果集
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        //创建pageResult并填充结果集
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 添加商品品牌
     * @param brand
     */
    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    /**
     * 修改时商品品牌回显
     * @param id
     * @return
     */
    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    /**
     * 修改商品品牌
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids!=null && ids.length>0){
            brandDao.deleteByPrimaryKeys(ids);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<Map<String, String>> selectOptionList() {
        return brandDao.selectOptionList();
    }

    /**
     * 审核品牌,也就是修改品牌状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, Integer status) {
        if (ids!=null && ids.length>0){
            Brand brand = new Brand();
            brand.setStatus(status);
            for (final Long id : ids) {
                brand.setId(id);
                brandDao.updateByPrimaryKeySelective(brand);
            }
        }
    }


}
