package cn.itcast.core.controller.user;

import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.user.UserService;
import cn.itcast.core.util.checkphone.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 发送手机验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sendCode.do")
    public Result sendCode(String phone){
        boolean phoneLegal = PhoneFormatCheckUtils.isPhoneLegal(phone);
        if (!phoneLegal){
            return new Result(false,"手机号码格式错误喽");
        }
        try {
            userService.sendCode(phone);
            return new Result(true,"验证码已发送哦");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"验证码发送失败喽");
        }
    }

    /**
     * 用户注册
     * @param smscode
     * @param user
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(String smscode,@RequestBody User user){
        try {
            userService.add(smscode,user);
            return new Result(true,"注册成功喽");
        }catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败喽");
        }
    }
}
