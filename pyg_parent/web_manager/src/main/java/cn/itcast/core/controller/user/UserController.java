package cn.itcast.core.controller.user;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.user.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;


    /**
     * 查询所有用户
     * @return
     */
    @RequestMapping("/findAll.do")
    public List<User> findAllBrand(){
        return userService.findAllBrand();
    }

    /**
     * 分页查询所有用户
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/findPage.do")
    public PageResult findPage(int page, int rows){
        PageResult p = userService.findPage(page, rows);
        return p;
    }

    /**
     * 条件查询
     * @param page
     * @param rows
     * @param user
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page,Integer rows, @RequestBody User user){
        PageResult pageResult = userService.search(page, rows, user);
        return pageResult;
    }

    //===================================================================================

    /**
     * 查询活跃人数
     * @return
     */
    @RequestMapping("/searchActive.do")
    public Integer searchActive(){
        return userService.searchActive();
    }

    //===================================================================================


    /**
     * 审核用户,也就是修改用户状态
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids, String status){
        try {
            userService.updateStatus(ids,status);
            return new Result(true,"审核成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败喽");
        }
    }


    /**
     * 用户数据导出
     * @return
     */
    @RequestMapping("/createExcel.do")
    public Result createExcel(){
        try {
            userService.createExcel();
            return new Result(true,"导出数据成功喽");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"导出数据失败喽");
        }
    }
}
