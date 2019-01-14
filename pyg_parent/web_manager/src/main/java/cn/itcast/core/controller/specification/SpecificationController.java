package cn.itcast.core.controller.specification;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.specification.SpecificationService;
import cn.itcast.core.vo.SpecVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    /**
     * 规格列表展示
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        PageResult pageResult = specificationService.search(page,rows,specification);
        return pageResult;
    }


    /**
     * 添加规格和规格选项
     * @param specVo
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody SpecVo specVo){
        try {
            specificationService.add(specVo);
            return new Result(true,"保存成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败喽");
        }
    }

    /**
     * 修改时的规格回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public SpecVo findOne(Long id){
        SpecVo specVo = specificationService.findOne(id);
        return specVo;
    }

    /**
     * 修改规格
     * @param specVo
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody SpecVo specVo){
        try {
            specificationService.update(specVo);
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
            specificationService.delete(ids);
            return new Result(true,"删除成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败喽");
        }
    }

    /**
     *
     * @return
     */
    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList(){
        return specificationService.selectOptionList();
    }

    /**
     * 审核规格,也就是修改规格状态
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids,Integer status){
        try {
            specificationService.updateStatus(ids,status);
            return new Result(true,"审核成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败喽");
        }
    }
}
