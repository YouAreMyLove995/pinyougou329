package cn.itcast.core.controller.sale;


import cn.itcast.core.service.sale.SaleService;
import cn.itcast.core.vo.DaySaleVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONArray;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sale")
public class SaleController {

    @Reference
    private SaleService saleService;

    /**
     * 运营商获取销售结果
     * @return
     */
        @RequestMapping("/findDaySaleList.do")
    public Map<Object,Object>  findDaySaleList() throws ParseException {
        Date date = new Date();
        return saleService.findSaleMap(date);
    }
}
