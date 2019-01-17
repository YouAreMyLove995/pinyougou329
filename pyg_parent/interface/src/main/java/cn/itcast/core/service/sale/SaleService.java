package cn.itcast.core.service.sale;

import cn.itcast.core.vo.DaySaleVo;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SaleService {
    /**
     * 运营商获取销售结果
     * @param date
     * @return
     */
    Map<Object,Object> findSaleMap(Date date) throws ParseException;

}
