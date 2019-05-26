package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/7 13:02
 */
public abstract class AddressClient {

    public static final List<AddressDTO> addressList = new ArrayList<AddressDTO>(){
        {
            AddressDTO address = new AddressDTO();
            address.setId(1L);
            address.setAddress("航头镇航头路18号传智播客 3号楼");
            address.setCity("上海");
            address.setDistrict("浦东新区");
            address.setName("虎哥");
            address.setPhone("1580056000");
            address.setState("上海");
            address.setZipCode("510000");
            address.setIsDefaulti(true);
            add(address);

            AddressDTO address2 = new AddressDTO();
            address2.setId(2L);
            address2.setAddress("航头镇航头路18号传智播客 3号楼");
            address2.setCity("长沙");
            address2.setDistrict("浦东新区");
            address2.setName("龙哥");
            address2.setPhone("158009200");
            address2.setState("长沙");
            address2.setZipCode("510500");
            address2.setIsDefaulti(false);
            add(address2);
        }
    };

    /**
     * 根据id获取地址
     *
     * @param id
     * @return
     */
    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList) {
            if(addressDTO.getId() == id){
                return addressDTO;
            }
        }
        return null;
    }
}
