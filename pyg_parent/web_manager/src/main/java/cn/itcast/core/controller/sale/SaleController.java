package cn.itcast.core.controller.sale;


import cn.itcast.core.service.sale.SaleService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestMapping("/findSaleMap.do")
    public List<Map<String,String>> findSaleMap(){
        Date date = new Date();
        return saleService.findSaleMap(date);
    }
}
