package com.leyou.order.client;

import com.leyou.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 商品微服务远程调用接口
 *
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/8 10:39
 */
@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
