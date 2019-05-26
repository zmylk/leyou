package com.leyou.order.dto;

import lombok.Data;

/**
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/7 12:53
 */
@Data
public class AddressDTO {

    private Long id;
    private String name;// 收件人姓名
    private String phone;// 电话
    private String state;// 省份
    private String city;// 城市
    private String district;// 区
    private String address;// 街道地址
    private String zipCode;// 邮政编号
    private Boolean isDefaulti;// 是否为默认地址
}
