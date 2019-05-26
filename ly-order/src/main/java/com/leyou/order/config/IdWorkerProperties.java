package com.leyou.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/7 10:46
 */
@Data
@ConfigurationProperties(prefix = "ly.worker")
public class IdWorkerProperties {

    private long workerId; // 当前机器id

    private long dataCenterId; // 序列号
}
