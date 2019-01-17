package cn.itcast.core.service.pay;


import java.util.Map;

public interface PayService {

    /**
     * 生成支付二维码
     * @return
     * @throws Exception
     */
    Map<String,String> createNative(String username) throws Exception;


    /**
     * 查询支付状态
     * @return
     */
    Map<String,String> queryPayStatus(String out_trade_no,String username) throws Exception;



    /**
     * 关闭订单
     * @return
     */
    Map<String,String> ClosePayOrder(String out_trade_no,String username) throws Exception;
}
