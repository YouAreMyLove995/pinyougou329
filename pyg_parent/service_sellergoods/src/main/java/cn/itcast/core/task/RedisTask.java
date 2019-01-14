package cn.itcast.core.task;


import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class RedisTask {

    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Scheduled(cron = "00 10 16 28 12 ?")
    private void autoToRedisForItemCat(){
        //获取到所有分类
        List<ItemCat> itemCatList1 = itemCatDao.selectByExample(null);
        //将分类放入缓存中
        if (itemCatList1 != null && itemCatList1.size()>0){
            for (ItemCat itemCat : itemCatList1) {
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
            }
        }
        System.out.println("分类已加载至redis");
    }

    @Scheduled(cron = "00 10 16 28 12 ?")
    private void autoToRedisForTemplate(){
        //查询商品模板
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        if (typeTemplates != null && typeTemplates.size()>0){
            //将模板下的品牌和规格和规格属性放入redis中,放入redis前应转为map
            for (TypeTemplate template : typeTemplates) {
                //先放品牌
                List<Map> brandList = JSON.parseArray(template.getBrandIds(), Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);
                //再放规格和规格属性
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(),specList);
            }
        }
        System.out.println("模板已加载至redis");
    }

    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds();
        List<Map> list = JSON.parseArray(specIds, Map.class);
        for (Map map : list) {
            Long spec_id = Long.parseLong(map.get("id").toString());
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
            specificationOptionQuery.createCriteria().andSpecIdEqualTo(spec_id);
            List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
            map.put("options",specificationOptionList);
        }
        return list;
    }
}
