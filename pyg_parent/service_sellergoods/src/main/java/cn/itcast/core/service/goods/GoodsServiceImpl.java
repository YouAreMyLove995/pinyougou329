package cn.itcast.core.service.goods;


import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.staticPage.StaticPageService;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceImpl implements GoodsService{

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private SolrTemplate solrTemplate;

//    @Resource
//    private StaticPageService staticPageService;

    @Resource
    private JmsTemplate jmsTemplate;

    @Resource
    private Destination topicPageAndSolrDestination;

    @Resource
    private Destination queueSolrDeleteDestination;

    /**
     * 添加商品
     * @param goodsVo
     */
    @Override
    public void add(GoodsVo goodsVo){
        //获取商品信息 tb_goods
        Goods goods = goodsVo.getGoods();
        //设置审核状态为未审核
        goods.setAuditStatus("0");
        //设置是否上架为未上架
        goods.setIsMarketable("0");
        //保存商品
        goodsDao.insertSelective(goods);
        //获取商品明细 tb_goods_desc
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        //设置外键
        goodsDesc.setGoodsId(goods.getId());
        //保存商品明细
        goodsDescDao.insertSelective(goodsDesc);

        //保存库存 tb_item
        //首先判断是否启用规格
        if ("1".equals(goods.getIsEnableSpec())){
            List<Item> itemList = goodsVo.getItemList();
            //设置商品标题title
            for (Item item : itemList) {
                String title = goods.getGoodsName()+" "+goods.getCaption();
                String spec = item.getSpec();
                Map<String,String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " "+entry.getValue();
                }
                item.setTitle(title);

                setAttributeForItem(goods,goodsDesc,item);

                itemDao.insertSelective(item);
            }
        }else {
            Item item = new Item();
            item.setTitle(goods.getGoodsName()+" "+goods.getCaption());//设置商品标题
            item.setPrice(goods.getPrice());//设置价格
            item.setNum(9999);//设置库存
            item.setIsDefault("1");//是否默认
            item.setSpec("{}");
            setAttributeForItem(goods,goodsDesc,item);
            itemDao.insertSelective(item);
        }
    }

    /**
     * 查询此商家商品
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        GoodsQuery goodsQuery = new GoodsQuery();
        goodsQuery.setOrderByClause("id desc");
        if (goods.getSellerId()!=null && !"".equals(goods.getSellerId())){
            goodsQuery.createCriteria().andSellerIdEqualTo(goods.getSellerId());
        }
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 商品回显
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        //商品信息
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);
        //商品详细信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);
        //商品对应库存信息
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        goodsVo.setItemList(itemList);
        return goodsVo;
    }

    /**
     * 商品更新
     * @param goodsVo
     */
    @Override
    public void update(GoodsVo goodsVo) {
        //更新商品信息
        Goods goods = goodsVo.getGoods();
        goods.setAuditStatus("0");
        goodsDao.updateByPrimaryKeySelective(goods);
        //更新商品详细信息
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
        //更新商品库存信息

        //先删除
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(itemQuery);
        //在更新
        //判断是否启用规格
        if ("1".equals(goods.getIsEnableSpec())){
            List<Item> itemList = goodsVo.getItemList();
            //设置商品标题title
            for (Item item : itemList) {
                String title = goods.getGoodsName()+" "+goods.getCaption();
                String spec = item.getSpec();
                Map<String,String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " "+entry.getValue();
                }
                item.setTitle(title);

                setAttributeForItem(goods,goodsDesc,item);

                itemDao.insertSelective(item);
            }
        }else {
            Item item = new Item();
            item.setTitle(goods.getGoodsName()+" "+goods.getCaption());//设置商品标题
            item.setPrice(goods.getPrice());//设置价格
            item.setNum(9999);//设置库存
            item.setIsDefault("1");//是否默认
            item.setSpec("{}");
            setAttributeForItem(goods,goodsDesc,item);
            itemDao.insertSelective(item);
        }

    }

    /**
     * 运营商系统查询待审核的商品
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult searchByManager(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        //设置查询条件
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        goodsQuery.setOrderByClause("id desc");
        if (goods.getAuditStatus()!=null &&!"".equals(goods.getAuditStatus().trim())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        criteria.andIsDeleteIsNull();
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 审核商品,也就是修改商品状态
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids!=null && ids.length>0){
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
//                if ("1".equals(status)){
                    //将商品保存到索引库
                    //dataImportToSolr();//将所有商品保存到索引库
                    //将点击的商品保存到索引库
                    //updateSolr(id);
                    // 生成对应的静态页面
                    //staticPageService.getHtml(id);

//                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
//                        @Override
//                        public Message createMessage(Session session) throws JMSException {
//                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
//                            return textMessage;
//                        }
//                    });

//                }
            }
        }
    }


    /**
     * 运营商删除商品  逻辑删除：其实是把商品的是否删除字段设置为1，表示已删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids!=null && ids.length>0){
            Goods goods = new Goods();
            goods.setIsDelete("1");
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                //删除索引库中对应的数据
//                SimpleQuery query = new SimpleQuery("item_goodsid:"+id);
//                solrTemplate.delete(query);
//                solrTemplate.commit();
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });
                //TODO 静态页面可删可不删，根据需求来
            }
        }
    }

    /**
     * 是否上架
     * @param ids
     * @param is_marketable
     */
    @Override
    public void updateIs_marketable(Long[] ids, String is_marketable) {
        if (ids!=null && ids.length>0){
            Goods goods = new Goods();
            goods.setIsMarketable(is_marketable);
            for (final Long id : ids) {
                goods.setId(id);
                Goods goods1 = goodsDao.selectByPrimaryKey(id);
                if ("1".equals(is_marketable) && "1".equals(goods1.getAuditStatus())){
                    goodsDao.updateByPrimaryKeySelective(goods);
                    //将商品保存到索引库
                    //dataImportToSolr();//将所有商品保存到索引库
                    //将点击的商品保存到索引库
                    //updateSolr(id);
                    // 生成对应的静态页面
                    //staticPageService.getHtml(id);
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                            return textMessage;
                        }
                    });
                }
                //删除索引库中对应的数据
                if ("0".equals(is_marketable) && "1".equals(goods1.getAuditStatus())){
                    goodsDao.updateByPrimaryKeySelective(goods);

                    jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                            return textMessage;
                        }
                    });
                }
            }
        }
    }


    //==================================================================================================================

    private void setAttributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        //设置商品图片
        String itemImages = goodsDesc.getItemImages();
        List<Map> list = JSON.parseArray(itemImages, Map.class);
        if (list!=null && list.size()>0){
            String image = list.get(0).get("url").toString();
            item.setImage(image);
        }

        item.setCategoryid(goods.getCategory3Id());//设置所属分类
        item.setStatus("1");//设置商品状态
        item.setCreateTime(new Date());//设置创建时间
        item.setUpdateTime(new Date());//设置更新时间
        item.setGoodsId(goods.getId());//设置商品id
        item.setSellerId(goods.getSellerId());//设置商家id

        //设置店家的名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName());
        //设置品牌
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());
        //设置所属分类
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());
    }

    //将选中的商品保存到索引库，没啥用了，已经转移到service_search下了
    private void updateSolr(Long id) {
        //设置查询条件
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id).andStatusEqualTo("1").andIsDefaultEqualTo("1");
        List<Item> items = itemDao.selectByExample(itemQuery);
        if(items != null && items.size() > 0){
            for (Item item : items) {
                // 处理动态字段
                String spec = item.getSpec();
                Map specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }
    }


    // 将所有商品数据导入索引库
    private void dataImportToSolr() {
        // 查询所有sku
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1");
        List<Item> items = itemDao.selectByExample(itemQuery);
        if(items != null && items.size() > 0){
            for (Item item : items) {
                // 处理动态字段
                String spec = item.getSpec();
                Map specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }
    }
}
