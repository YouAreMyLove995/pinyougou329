package cn.itcast.core.controller.pay;

import cn.itcast.core.entity.Result;
import cn.itcast.core.service.pay.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;

    /**
     *生成支付二维码
     * @return
     */
    @RequestMapping("/createNative.do")
    public Map<String,String> createNative() throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(username);
    }

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus.do")
    public Result queryPayStatus(String out_trade_no){
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            int time = 0;
            while (true){
                Map<String, String> map = payService.queryPayStatus(out_trade_no,username);
                if ("SUCCESS".equals(map.get("trade_state"))){
                    return new Result(true,"支付成功啦");
                }else {
                    Thread.sleep(5000);
                    time++;
                }
                if (time > 360){
                    return new Result(false,"二维码超时喽");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败喽");
        }
    }
}
