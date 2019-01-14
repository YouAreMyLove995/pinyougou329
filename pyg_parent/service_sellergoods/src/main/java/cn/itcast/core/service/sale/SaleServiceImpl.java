package cn.itcast.core.service.sale;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.log.PayLogQuery;
import com.alibaba.dubbo.config.annotation.Service;

import javax.annotation.Resource;
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
    public List<Map<String, String>> findSaleMap(Date date) {
        ArrayList<Map<String,String>> list = new ArrayList<>();

        //new一个Date来存储前一天
        Date date1 = new Date();
        //循环里newMap
        for (int i = 0; i < 7; i++) {
            Calendar ca = Calendar.getInstance();
            ca.setTime(date);
            ca.add(Calendar.DATE, -1);// num为增加的天数，可以改变的
            date1 = ca.getTime();

            Map<String, String> map = new HashMap<>();
            PayLogQuery payLogQuery = new PayLogQuery();
            PayLogQuery.Criteria criteria = payLogQuery.createCriteria();
            criteria.andPayTimeBetween(date1,date);
            criteria.andTradeStateEqualTo("1");
            List<PayLog> payLogs = payLogDao.selectByExample(payLogQuery);
            Long totalFee = 0L;
            //一天的钱
            for (PayLog payLog : payLogs) {
                Long totalFeeOne = payLog.getTotalFee();
                totalFee += totalFeeOne;
            }
            map.put(date.toString(),totalFee.toString());
            list.add(map);

            Calendar ca1 = Calendar.getInstance();
            ca.setTime(date);
            ca.add(Calendar.DATE, -1);// num为增加的天数，可以改变的
            date = ca.getTime();
        }

        return list;
    }
}
