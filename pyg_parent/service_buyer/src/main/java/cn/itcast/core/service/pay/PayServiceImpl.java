package cn.itcast.core.service.pay;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.util.httpClient.HttpClient;
import cn.itcast.core.util.uniqueKey.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Value("${appid}")
    private String appid;//微信公众账号或开放平台APP的唯一标识

    @Value("${partner}")
    private String partner;//财付通平台的商户账号

    @Value("${partnerkey}")
    private String partnerkey;//财付通平台的商户密钥

    @Value("${notifyurl}")
    private String notifyurl;//回调地址（判断本次支付成功是否成功）

    @Resource
    private IdWorker idWorker;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private PayLogDao payLogDao;

    @Resource
    private OrderDao orderDao;

    /**
     * 生成支付二维码
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> createNative(String username) throws Exception {

        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(username);

        //生成支付页面需要的数据
        Map<String,String> data = new HashMap<>();
        //生成交易流水号
//        long out_trade_no = idWorker.nextId();
        //==========================这个没吊用，测试了一下二维码的生成===============================================
//        map.put("out_trade_no",String.valueOf(out_trade_no));
//        //生成金额
//        map.put("total_fee","1");
//        //生成二维码url地址
//        map.put("code_url","http://www.itcast.cn");
//        //返回给前端，交给前端展示
        //==========================================================================================================
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid",appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id",partner);
//        随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
        data.put("nonce_str", WXPayUtil.generateNonceStr());
//        签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
        //PS:签名在提交时会自动生成
//        商品描述	body	是	String(128)	腾讯充值中心-QQ会员充值
//        商品简单描述，该字段请按照规范传递，具体请见参数规定
        data.put("body","一分钱也是钱！");
//        商户订单号	out_trade_no	是	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。详见商户订单号
//        data.put("out_trade_no",String.valueOf(out_trade_no));
        data.put("out_trade_no",payLog.getOutTradeNo());
//        标价金额	total_fee	是	Int	88	订单总金额，单位为分，详见支付金额
//        data.put("total_fee",String.valueOf(payLog.getTotalFee()));
        data.put("total_fee","1");
//        终端IP	spbill_create_ip	是	String(16)	123.12.12.123	支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
        data.put("spbill_create_ip","192.168.200.128");
//        通知地址	notify_url	是	String(256)	http://www.weixin.qq.com/wxpay/pay.php	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("notify_url",notifyurl);
//        交易类型	trade_type	是	String(16)	JSAPI
//        JSAPI -JSAPI支付
//        NATIVE -Native支付
//        APP -APP支付
        data.put("trade_type","NATIVE");

        //要用httpClient模拟发送请求
        HttpClient httpClient = new HttpClient(url);
        //首先需要把map转为xml
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);
        httpClient.setHttps(true);//支持https
        httpClient.setXmlParam(xmlParam);//微信下单需要的数据
        httpClient.post();//请求方式为post

        String content = httpClient.getContent();//获取相应结果，结果也为xml
        Map<String, String> map = WXPayUtil.xmlToMap(content);
        map.put("out_trade_no",payLog.getOutTradeNo());
        map.put("total_fee",String.valueOf(payLog.getTotalFee()));
        return map;
    }


    /**
     * 查询支付状态
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String out_trade_no,String username) throws Exception {
        //微信查询订单的接口
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        HashMap<String, String> data = new HashMap<>();

        //封装借口需要的参数
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid",appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id",partner);
//        商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        data.put("out_trade_no",out_trade_no);
//        随机字符串	nonce_str	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
        data.put("nonce_str",WXPayUtil.generateNonceStr());
//        签名	sign	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	通过签名算法计算得出的签名值，详见签名生成算法

        //要用httpClient模拟发送请求
        HttpClient httpClient = new HttpClient(url);
        //首先需要把map转为xml
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);
        httpClient.setHttps(true);//支持https
        httpClient.setXmlParam(xmlParam);//微信下单需要的数据
        httpClient.post();//请求方式为post

        //获取结果转为map返回
        String content = httpClient.getContent();
        Map<String, String> map = WXPayUtil.xmlToMap(content);

        //更新日志表
        if ("SUCCESS".equals(map.get("trade_state"))){
            //更新支付日志表
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(out_trade_no);//设置主键，用于更新
            payLog.setPayTime(new Date());//支付时间
            payLog.setTransactionId(map.get("transaction_id"));//交易流水号
            payLog.setTradeState("1");//更改支付状态
            payLogDao.updateByPrimaryKeySelective(payLog);

            PayLog newPayLog = payLogDao.selectByPrimaryKey(out_trade_no);

            //更新订单表
            String orderList = newPayLog.getOrderList();
            String[] orderArray = orderList.split(", ");
            for (String s : orderArray) {
                Order order = new Order();
                order.setOrderId(Long.parseLong(s));
                order.setStatus("2");
                order.setUpdateTime(new Date());
                order.setPaymentTime(new Date());
                orderDao.updateByPrimaryKeySelective(order);
            }

            //清除缓存中的payLog
            redisTemplate.boundHashOps("payLog").put(username,null);

        }
        return map;
    }

    /**
     * 关闭订单
     * @param out_trade_no
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> ClosePayOrder(String out_trade_no, String username) throws Exception {
        //微信查询订单的接口
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        HashMap<String, String> data = new HashMap<>();

        //封装借口需要的参数
//        公众账号ID	appid	是	String(32)	wxd678efh567hg6787	微信支付分配的公众账号ID（企业号corpid即为此appId）
        data.put("appid",appid);
//        商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
        data.put("mch_id",partner);
//        商户订单号	out_trade_no	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        data.put("out_trade_no",out_trade_no);
//        随机字符串	nonce_str	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	随机字符串，不长于32位。推荐随机数生成算法
        data.put("nonce_str",WXPayUtil.generateNonceStr());
//        签名	sign	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	通过签名算法计算得出的签名值，详见签名生成算法

        //要用httpClient模拟发送请求
        HttpClient httpClient = new HttpClient(url);
        //首先需要把map转为xml
        String xmlParam = WXPayUtil.generateSignedXml(data, partnerkey);
        httpClient.setHttps(true);//支持https
        httpClient.setXmlParam(xmlParam);//微信下单需要的数据
        httpClient.post();//请求方式为post

        //获取结果转为map返回
        String content = httpClient.getContent();
        Map<String, String> map = WXPayUtil.xmlToMap(content);

        //更新日志表
        if ("SUCCESS".equals(map.get("return_code"))){
            //更新支付日志表
            PayLog payLog = new PayLog();
            payLog.setOutTradeNo(out_trade_no);//设置主键，用于更新
            payLog.setPayTime(new Date());//支付时间
            payLog.setTransactionId(map.get("transaction_id"));//交易流水号
            payLog.setTradeState("2");//更改支付状态
            payLogDao.updateByPrimaryKeySelective(payLog);

            PayLog newPayLog = payLogDao.selectByPrimaryKey(out_trade_no);

            //更新订单表
            String orderList = newPayLog.getOrderList();
            String[] orderArray = orderList.split(", ");
            for (String orderId : orderArray) {
                Order order = new Order();
                order.setOrderId(Long.parseLong(orderId));
                order.setStatus("6");
                order.setUpdateTime(new Date());
                orderDao.updateByPrimaryKeySelective(order);
            }

            //清除缓存中的payLog
            redisTemplate.boundHashOps("payLog").put(username,null);

        }
        return map;
    }
}
