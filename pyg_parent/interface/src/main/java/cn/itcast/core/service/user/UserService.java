package cn.itcast.core.service.user;


import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.user.User;

import java.util.List;

public interface UserService {

    /**
     * 发送手机验证码
     * @param phone
     */
    void sendCode(String phone);

    /**
     * 用户注册
     * @param smscode
     * @param user
     */
    void add(String smscode, User user);

    /**
     * 查询所有用户
     * @param page
     * @param rows
     * @return
     */
    PageResult findPage(int page, int rows);

    /**
     * 审核用户,也就是修改用户状态
     * @param ids
     * @param status
     */
    void updateStatus(Long[] ids, String status);

    /**
     * 查询所有用户
     * @return
     */
    List<User> findAllBrand();

    /**
     * 条件查询
     * @param page
     * @param rows
     * @param user
     * @return
     */
    PageResult search(Integer page, Integer rows, User user);
}
