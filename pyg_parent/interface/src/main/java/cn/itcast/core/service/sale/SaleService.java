package cn.itcast.core.service.sale;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface SaleService {
    /**
     * 运营商获取销售结果
     * @param date
     * @return
     */
    List<Map<String,String>> findSaleMap(Date date);

}
