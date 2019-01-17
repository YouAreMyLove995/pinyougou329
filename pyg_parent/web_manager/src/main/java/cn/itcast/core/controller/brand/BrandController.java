package cn.itcast.core.controller.brand;


import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.brand.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 查询所有商品品牌
     * @return
     */
    @RequestMapping("/findAll.do")
    public List<Brand> findAllBrand(){
        return brandService.findAllBrand();
    }


    /**
     * 分页查询
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/findPage.do")
    public PageResult findPage(int page, int rows){
        PageResult p = brandService.findPage(page, rows);
        return p;
    }

    /**
     * 条件查询未通过审核的品牌
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/searchOne.do")
    public PageResult searchOne(Integer page,Integer rows,@RequestBody Brand brand){
        PageResult pageResult = brandService.searchOne(page, rows,brand);
        return pageResult;
    }


    /**
     * 条件查询
     * @param page
     * @param rows
     * @param brand
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page,Integer rows, @RequestBody Brand brand){
        PageResult pageResult = brandService.search(page, rows, brand);
        return pageResult;
    }

    /**
     * 添加商品品牌
     * @param brand
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody Brand brand){
        try {
            brandService.add(brand);
            return new Result(true,"保存成功啦");
        } catch (Exception e) {
            return new Result(false,"保存失敗啊");
        }
    }

    /**
     * 修改时商品品牌回显
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public Brand findOne(Long id){
        Brand brand = brandService.findOne(id);
        return brand;
    }

    /**
     * 修改商品品牌
     * @param brand
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败喽");
        }
    }

    /**
     *批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
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
        return brandService.selectOptionList();
    }

    /**
     * 审核品牌,也就是修改品牌状态
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids,Integer status){
        try {
            brandService.updateStatus(ids,status);
            return new Result(true,"审核成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败喽");
        }
    }
}
