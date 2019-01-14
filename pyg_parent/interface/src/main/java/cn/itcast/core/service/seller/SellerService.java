package cn.itcast.core.service.seller;


import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {

    /**
     * 商家入驻
     * @param seller
     */
    void add(Seller seller);

    /**
     *  待审核商家列表查询
     * @param page
     * @param rows
     * @param seller
     * @return
     */
    public PageResult search(Integer page,Integer rows,Seller seller);

    /**
     * 查看待审核商家详情
     * @param id
     * @return
     */
    Seller findOne(String id);

    /**
     * 审核商家
     * @param sellerId
     * @param status
     */
    void updateStatus(String sellerId,String status);

}
