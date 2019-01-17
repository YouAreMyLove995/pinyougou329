package cn.itcast.core.controller.template;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.template.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference
    private TypeTemplateService typeTemplateService;

    /**
     * 查询商品模板
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows,@RequestBody TypeTemplate typeTemplate){
        PageResult pageResult = typeTemplateService.search(page,rows,typeTemplate);
        return pageResult;
    }

    /**
     * 查询未通过审核的商品模板
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/searchOne.do")
    public PageResult searchOne(Integer page, Integer rows,@RequestBody TypeTemplate typeTemplate){
        PageResult pageResult = typeTemplateService.searchOne(page,rows,typeTemplate);
        return pageResult;
    }

    /**
     * 添加商品模板
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.add(typeTemplate);
            return new Result(true,"添加成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败喽");
        }
    }

    /**
     * 修改商品模板时回显数据
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public TypeTemplate findOne(Long id){
        TypeTemplate typeTemplate = typeTemplateService.findOne(id);
        return typeTemplate;
    }

    /**
     * 修改商品模板
     * @param typeTemplate
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody TypeTemplate typeTemplate){
        try {
            typeTemplateService.update(typeTemplate);
            return new Result(true,"修改成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败喽");
        }
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            typeTemplateService.delete(ids);
            return new Result(true,"删除成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败喽");
        }
    }

    /**
     * 审核模板,也就是修改模板状态
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids,Integer status){
        try {
            typeTemplateService.updateStatus(ids,status);
            return new Result(true,"审核成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败喽");
        }
    }
}
