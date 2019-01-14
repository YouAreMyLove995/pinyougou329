package cn.itcast.core.service.itemCat;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    /**
     * 商品分类查询
     * @param parentId
     * @return
     */
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //获取到所有分类
        List<ItemCat> itemCatList1 = itemCatDao.selectByExample(null);
        //将分类放入缓存中
        if (itemCatList1 != null && itemCatList1.size()>0){
            for (ItemCat itemCat : itemCatList1) {
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
            }
        }

        ItemCatQuery itemCatQuery = new ItemCatQuery();
        itemCatQuery.createCriteria().andParentIdEqualTo(parentId);
        List<ItemCat> itemCatList = itemCatDao.selectByExample(itemCatQuery);
        return itemCatList;
    }


    /**
     * 添加商品分类
     * @param itemCat
     */
    @Override
    public void add(ItemCat itemCat) {
        itemCatDao.insertSelective(itemCat);
    }

    /**
     * 商品分类时数据回显
     * @param id
     * @return
     */
    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    /**
     * 更新商品分类
     * @param itemCat
     */
    @Override
    public void update(ItemCat itemCat) {
        itemCatDao.deleteByPrimaryKey(itemCat.getId());
        itemCatDao.insertSelective(itemCat);
    }

    /**
     * 批量删除商品分类
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids!=null && ids.length>0){
            itemCatDao.deleteByPrimaryKeys(ids);
        }
    }

    /**
     * 查询所有分类信息
     * @return
     */
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }


    /**
     * 审核分类,也就是修改分类状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, Integer status) {
        if (ids!=null && ids.length>0){
            ItemCat itemCat = new ItemCat();
            itemCat.setStatus(status);
            for (Long id : ids) {
                itemCat.setId(id);
                itemCatDao.updateByPrimaryKeySelective(itemCat);
            }
        }
    }

    /**
     * 分页查询
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult findPage(int page, int rows) {
        PageHelper.startPage(page,rows);
        //根據id降序排列
        PageHelper.orderBy("id desc");
        Page<ItemCat> page1=   (Page<ItemCat>) itemCatDao.selectByExample(null);
        return new PageResult(page1.getTotal(), page1.getResult());
    }

}
