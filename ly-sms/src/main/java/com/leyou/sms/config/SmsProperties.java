package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信属性对象
 *
 * author 暗氵愧
 * HostName dell
 * Date 2018/12/25 16:48
 */
@Data
@ConfigurationProperties(prefix = "ly.sms")
public class SmsProperties {

    private String apikey;
    private String userId;
    private String pwd;
    private String masterIpAddress;
    private String ipAddress1;
    private String ipAddress2;
    private String ipAddress3;

}
