package cn.itcast.core.service.staticPage;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPageServiceImpl implements StaticPageService,ServletContextAware {

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private ItemDao itemDao;


    private Configuration configuration;
    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
        this.configuration = freeMarkerConfigurer.getConfiguration();
    }


    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * 生成商品对应的静态页面
     * @param id
     */
    @Override
    public void getHtml(Long id) {
        try {
            //获取模板
            Template template = configuration.getTemplate("item.ftl");
            //准备业务数据
            Map<String,Object> dateModel = getDateModel(id);
            //模板+数据=`输出
            String pathName = "/"+ id + ".html";
            String path = servletContext.getRealPath(pathName);
            File file = new File(path);
            Writer out = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
            template.process(dateModel,out);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //获取静态页面需要的数据
    private Map<String, Object> getDateModel(Long id) {
        Map<String, Object> dateModel = new HashMap<>();
        //获取商品基本数据 goods表
        Goods goods = goodsDao.selectByPrimaryKey(id);
        dateModel.put("goods",goods);
        //获取商品详细数据 goods_desc表
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        dateModel.put("goodsDesc",goodsDesc);
        //获取商品分类 item_cat表
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        dateModel.put("itemCat1",itemCat1);
        dateModel.put("itemCat2",itemCat2);
        dateModel.put("itemCat3",itemCat3);
        //获取库存 item表
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id).andNumGreaterThan(0);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        dateModel.put("itemList",itemList);
        return dateModel;
    }


}
