package com.leyou.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购物商品传输对象
 *
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/7 8:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private Long skuId; // 商品skuId
    private Integer num;// 购买数量
}
