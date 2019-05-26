package com.leyou.order.config;

import com.leyou.common.utils.IdWorker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IdWorker配置类
 *
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/7 10:48
 */
@Configuration
@EnableConfigurationProperties(IdWorkerProperties.class)
public class IdWorkerConfig {

    /**
     * 把idWorker对象注入到Spring容器中
     *
     * @param prop
     * @return
     */
    @Bean
    public IdWorker idWorker(IdWorkerProperties prop){
        return new IdWorker(prop.getWorkerId(), prop.getDataCenterId());
    }
}
