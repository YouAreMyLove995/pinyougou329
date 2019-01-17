package cn.itcast.core.service.sale;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.log.PayLogQuery;
import cn.itcast.core.vo.DaySaleVo;
import com.alibaba.dubbo.config.annotation.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SaleServiceImpl implements SaleService {

    @Resource
    private PayLogDao payLogDao;

    /**
     * 运营商获取销售结果
     * @param date
     * @return
     */
    @Override
    public Map<Object,Object> findSaleMap(Date date) throws ParseException {
        HashMap<Object, Object> map = new HashMap<>();

        Object categories[] = {};
        Object data[] = {};

        List<Object> categoriesList = new ArrayList<>();
        List<Object> dataList = new ArrayList<>();

        //new一个Date来存储前一天
        Date date1 = new Date();
        //循环里newMap
        for (int i = 0; i < 7; i++) {
            date1 = date;
            Calendar ca = Calendar.getInstance();
            ca.setTime(date1);
            ca.add(Calendar.DATE, 1);// num为增加的天数，可以改变的
            date1 = ca.getTime();

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            //将当前日期转换为字符串
            String time = df.format(date);
            String time1 = df.format(date1);
                //字符串转回日期
                Date parse = df.parse(time);
                Date parse1 = df.parse(time1);

                //封装查询条件
                PayLogQuery payLogQuery = new PayLogQuery();
                PayLogQuery.Criteria criteria = payLogQuery.createCriteria();
                criteria.andTradeStateEqualTo("1");
                criteria.andPayTimeBetween(parse,parse1);
                List<PayLog> payLogs = payLogDao.selectByExample(payLogQuery);
                Long totalFee = 0L;
                //一天的钱
                for (PayLog payLog : payLogs) {
                    Long totalFeeOne = payLog.getTotalFee();
                    totalFee += totalFeeOne;
                }
                //填充数据
                categoriesList.add(time);
                dataList.add(totalFee);

                Calendar ca1 = Calendar.getInstance();
                ca1.setTime(date);
                ca1.add(Calendar.DATE, -1);// num为增加的天数，可以改变的
                date = ca1.getTime();
        }
        categories = categoriesList.toArray();
        data = dataList.toArray();
        map.put("categories",categories);
        map.put("data",data);
        return map;
    }
}
