package cn.itcast.core.service.specification;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.vo.SpecVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Resource
    private SpecificationDao specificationDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;
    /**
     * 规格列表展示
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        PageHelper.startPage(page,rows);
        SpecificationQuery specificationQuery = new SpecificationQuery();
        if (specification.getSpecName()!=null && !"".equals(specification.getSpecName().trim())){
            specificationQuery.createCriteria().andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        specificationQuery.setOrderByClause("id desc");
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 添加规格和规格选项
     * @param specVo
     */
    @Override
    public void add(SpecVo specVo) {
        //添加规格
        Specification specification = specVo.getSpecification();
        specificationDao.insertSelective(specification);
        //添加规格选项
        List<SpecificationOption> specificationOptionList = specVo.getSpecificationOptionList();
        if (specificationOptionList!=null && specificationOptionList.size()>0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specification.getId());
                //specificationOptionDao.insertSelective(specificationOption);
            }
            //批量添加
            specificationOptionDao.insertSelectives(specificationOptionList);
        }

    }

    /**
     * 修改时的规格回显
     * @param id
     * @return
     */
    @Override
    public SpecVo findOne(Long id) {
        SpecVo specVo = new SpecVo();
        //查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);
        specVo.setSpecification(specification);
        //查询规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
        specVo.setSpecificationOptionList(specificationOptionList);
        return specVo;
    }

    /**
     * 修改规格
     * @param specVo
     */
    @Override
    public void update(SpecVo specVo) {
        Specification specification = specVo.getSpecification();
        //更新规格
        specificationDao.updateByPrimaryKeySelective(specification);
        //删除规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        //添加规格选项
        List<SpecificationOption> specificationOptionList = specVo.getSpecificationOptionList();
        if (specificationOptionList!=null && specificationOptionList.size()>0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                specificationOption.setSpecId(specification.getId());
            }
            //批量添加
            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids!=null && ids.length>0){
            specificationDao.deleteByPrimaryKeys(ids);
            specificationOptionDao.deleteByPrimaryKeys(ids);
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }

    /**
     * 审核规格,也就是修改规格状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, Integer status) {
        if (ids!=null && ids.length>0){
            Specification specification = new Specification();
            specification.setStatus(status);
            for (final Long id : ids) {
                specification.setId(id);
                specificationDao.updateByPrimaryKeySelective(specification);
            }
        }
    }

    /**
     * 规格列表并且只展示未通过审核的
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult searchOne(Integer page, Integer rows, Specification specification) {
        PageHelper.startPage(page,rows);
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
        if (specification.getSpecName()!=null && !"".equals(specification.getSpecName().trim())){
            criteria.andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        criteria.andStatusEqualTo(0);
        specificationQuery.setOrderByClause("id desc");
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        return new PageResult(p.getTotal(),p.getResult());

    }
}
