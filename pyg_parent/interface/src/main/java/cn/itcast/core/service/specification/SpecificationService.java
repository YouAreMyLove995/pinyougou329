package cn.itcast.core.service.specification;


import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.vo.SpecVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    /**
     * 规格列表展示
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    PageResult search(Integer page, Integer rows, Specification specification);

    /**
     * 添加规格和规格选项
     * @param specVo
     */
    void add(SpecVo specVo);

    /**
     * 修改时的规格回显
     * @param id
     * @return
     */
    SpecVo findOne(Long id);


    /**
     * 修改规格
     * @param specVo
     */
    void update(SpecVo specVo);


    /**
     * 批量删除
     * @param ids
     */
    void delete(Long[] ids);

    /**
     *
     * @return
     */
    List<Map<String,String>> selectOptionList();

    /**
     * 审核规格,也就是修改规格状态
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, Integer status);
}
