package cn.itcast.core.service.template;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    /**
     * 查询商品模板
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {

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


        PageHelper.startPage(page,rows);
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        if (typeTemplate.getName()!=null && !"".equals(typeTemplate.getName().trim())){
            typeTemplateQuery.createCriteria().andNameLike("%"+typeTemplate.getName().trim()+"%");
        }
        typeTemplateQuery.setOrderByClause("id desc");
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 添加商品模板
     * @param typeTemplate
     */
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    /**
     * 修改商品模板时回显数据
     * @param id
     * @return
     */
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    /**
     * 修改商品模板
     * @param typeTemplate
     */
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids!=null && ids.length>0){
            typeTemplateDao.deleteByPrimaryKeys(ids);
        }
    }

    /**
     * 根据id查询规格和规格选项表
     * 思路就是根据id获取到模板表，模板表中有spec_ids这个列，列中存着id 和 规格，遍历这个集合，根据这个id可以获得规格选项表，
     * 然后将规格选项添家到spec_ids每个map，最后这个map集合中返回
     * @param id
     * @return
     */
    @Override
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

    /**
     * 审核模板,也就是修改模板状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, Integer status) {
        if (ids!=null && ids.length>0){
            TypeTemplate typeTemplate = new TypeTemplate();
            typeTemplate.setStatus(status);
            for (final Long id : ids) {
                typeTemplate.setId(id);
                typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
            }
        }
    }
}
