package cn.itcast.core.service.template;


import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {

    /**
     * 查询商品模板
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

    /**
     * 添加商品模板
     * @param typeTemplate
     */
    void add(TypeTemplate typeTemplate);

    /**
     * 修改商品模板时回显数据
     * @param id
     * @return
     */
    TypeTemplate findOne(Long id);

    /**
     * 修改商品模板
     * @param typeTemplate
     */
    void update(TypeTemplate typeTemplate);

    /**
     * 批量删除
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 根据id查询规格和规格选项表
     * @param id
     * @return
     */
    List<Map> findBySpecList(Long id);

    /**
     * 审核模板,也就是修改模板状态
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, Integer status);
}
