package com.leyou.order.dto;


import com.leyou.common.dto.CartDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 提交数据传输对象
 *
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/7 8:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {

    @NotNull
    private Long addressId; // 收货人地址id
    @NotNull
    private Integer paymentType;// 付款类型
    @NotNull
    private List<CartDTO> carts; // 订单详情
}
