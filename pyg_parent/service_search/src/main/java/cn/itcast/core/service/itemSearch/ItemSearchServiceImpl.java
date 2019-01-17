package cn.itcast.core.service.itemSearch;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.pojo.user.UserQuery;
import cn.itcast.core.service.itemSearch.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private ItemDao itemDao;

    @Resource
    private UserDao userDao;

    /**
     * 前台系统的全文检索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap,String name) {

        //==================================================================
        if (name != null){

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String today = format.format(new Date());

            UserQuery userQuery = new UserQuery();
            userQuery.createCriteria().andUsernameEqualTo(name);
            List<User> users = userDao.selectByExample(userQuery);
            for (User user : users) {
                Long id = user.getId();
                redisTemplate.opsForValue().setBit("daily_active_users:"+today,id,true);
            }
        }

        //==================================================================




        //创建一个Map封装结果集
        Map<String,Object> resultMap = new HashMap<>();
        //处理关键字----去除空格
        String keywords = searchMap.get("keywords");
        if (keywords != null && !"".equals(keywords)){
            keywords = keywords.replace(" ","");
            searchMap.put("keywords",keywords);
        }
        //商品结果集
//        Map<String,Object> map = searchForPage(searchMap);
        Map<String, Object> map = searchForHighlightPage(searchMap);
        resultMap.putAll(map);
        //商品分类列表categoryList
        List<String> categoryList = searchForGroupPage(searchMap);
        if (categoryList != null && categoryList.size()>0){
            resultMap.put("categoryList",categoryList);
            //加载第一个分类下的品牌和规格
            Map<String,Object> brandAndSpecMap = searchBrandAndSpecListByCategory(categoryList.get(0));
            resultMap.putAll(brandAndSpecMap);
        }
        return resultMap;
    }

    /**
     * 将商品信息保存到索引库中
     * @param id
     */
    @Override
    public void updateSolr(Long id) {
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

    /**
     * 将商品信息从索引库中删除
     * @param id
     */
    @Override
    public void deleteSolr(Long id) {
        SimpleQuery query = new SimpleQuery("item_goodsid:"+id);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }


    //===========================================================================================================

    //加载第一个分类下的品牌和规格
    private Map<String, Object> searchBrandAndSpecListByCategory(String category) {
        HashMap<String, Object> brandAndSpecMap = new HashMap<>();
        //从缓存中获取模板id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(category);
        //从缓存中获取品牌
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        brandAndSpecMap.put("brandList",brandList);
        //从缓存中获取规格
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        brandAndSpecMap.put("specList",specList);
        return brandAndSpecMap;
    }


    //加载商品分类

    private List<String> searchForGroupPage(Map<String, String> searchMap) {
        // 设置关键字
        Criteria criteria = new Criteria("item_keywords");
        criteria.is(searchMap.get("keywords"));
        SimpleQuery query = new SimpleQuery(criteria);
        // 设置分组
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category"); // 根据哪个字段进行分组
        query.setGroupOptions(groupOptions);
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        // 获取分组结果
        List<String> list = new ArrayList<>();
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue(); // 分组结果
            list.add(groupValue);
        }
        return list;
    }


    //在分页的基础上高亮搜索词
    private Map<String, Object> searchForHighlightPage(Map<String, String> searchMap) {
        String keywords = searchMap.get("keywords");
        //封装检索条件-----关键字
        Criteria criteria = new Criteria("item_keywords");
        if (keywords!=null &&!"".equals(keywords)){
            criteria.is(keywords);
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);
        //设置分页条件
        //先获取每页个数和现在在第几页
        Integer pageNo =Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        //计算出本页第一个的id
        Integer pageRows = (pageNo-1)*pageSize;
        query.setOffset(pageRows);
        query.setRows(pageSize);
        //关键字高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//对哪个字段进行高亮
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);


        //根据分类过滤
        if (searchMap.get("category") != null && !"".equals(searchMap.get("category"))){
            Criteria criteria1 = new Criteria("item_category");
            criteria1.is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }
        //根据品牌过滤
        if (searchMap.get("brand") != null && !"".equals(searchMap.get("brand"))){
            Criteria criteria1 = new Criteria("item_brand");
            criteria1.is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }
        //根据规格过滤
        if(searchMap.get("spec") != null && !"".equals(searchMap.get("spec"))){
            Map<String, String> specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entrySet = specMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                Criteria cri = new Criteria("item_spec_"+entry.getKey());
                cri.is(entry.getValue());
                FilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }
        //根据价格过滤
        if (searchMap.get("price") != null && !"".equals(searchMap.get("price"))){
            String[] prices = searchMap.get("price").split("-");
            Criteria criteria1 = new Criteria("item_price");
            if (searchMap.get("price").contains("*")){
                criteria1.greaterThan(prices[0]);
            }else{
                criteria1.between(prices[0],prices[1],true,true);
            }
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteria1);
            query.addFilterQuery(filterQuery);
        }

        //排序
        if (searchMap.get("sort") != null && !"".equals(searchMap.get("sort"))){
            if ("ASC".equals(searchMap.get("sort"))){
                Sort s = new Sort(Sort.Direction.ASC,"item_"+searchMap.get("sortField"));
                query.addSort(s);
            }else {
                Sort s = new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField"));
                query.addSort(s);
            }
        }

        //查询
        HighlightPage<Item> highlightPage   = solrTemplate.queryForHighlightPage(query,Item.class);
        //处理结果集
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if (highlighted !=null && highlighted.size()>0){
            for (HighlightEntry<Item> highlightEntry : highlighted) {
                Item item = highlightEntry.getEntity();//普通结果集
                List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
                if (highlights != null && highlights.size()>0){
                    for (HighlightEntry.Highlight highlight : highlights) {
                        item.setTitle(highlight.getSnipplets().get(0));
                    }
                }
            }
        }
        //将结果封装到map中
        Map<String,Object> map = new HashMap<>();
        map.put("totalPages",highlightPage.getTotalPages());
        map.put("total",highlightPage.getTotalElements());
        map.put("rows",highlightPage.getContent());
        return map;
    }

    //查询结果集并分页查询
    private Map<String, Object> searchForPage(Map<String, String> searchMap) {
        String keywords = searchMap.get("keywords");
        //封装检索条件-----关键字
        Criteria criteria = new Criteria("item_keywords");
        if (keywords!=null &&!"".equals(keywords)){
            criteria.is(keywords);
        }
        SimpleQuery query = new SimpleQuery(criteria);
        //设置分页条件
        //先获取每页个数和现在在第几页
        Integer pageNo =Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        //计算出本页第一个的id
        Integer pageRows = (pageNo-1)*pageSize;
        query.setOffset(pageRows);
        query.setRows(pageSize);
        //查询
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);
        //将结果封装到map中
        Map<String,Object> map = new HashMap<>();
        map.put("totalPages",scoredPage.getTotalPages());
        map.put("total",scoredPage.getTotalElements());
        map.put("rows",scoredPage.getContent());
        return map;
    }
}
