package cn.itcast.core.service.itemSearch;

import java.util.Map;

public interface ItemSearchService {

    /**
     * 前台系统的全文检索
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String, String> searchMap);

    /**
     * 将商品信息保存到索引库中
     * @param id
     */
    public void updateSolr(Long id);

    /**
     * 将商品信息从索引库中删除
     * @param id
     */
    public void deleteSolr(Long id);
}
