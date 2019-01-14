package cn.itcast.core.service.address;

import cn.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddressService {

    /**
     * 根据登录用户获取收货地址和收货人等信息
     * @param name
     * @return
     */
    List<Address> findListByLoginUser(String name);
}
