package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/9 12:55
 */
@Configuration
public class WXPayConfiguration {

    /**
     * 注入wxPay
     *
     * @param payConfig
     * @return
     */
    @Bean
    public WXPay wxPay(PayConfig payConfig){
        return new WXPay(payConfig, WXPayConstants.SignType.HMACSHA256);
    }

    /**
     * 注入payConfig到SpringIoc容器中
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "ly.pay")
    public PayConfig payConfig(){
        return new PayConfig();
    }
}
