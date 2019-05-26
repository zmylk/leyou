package com.leyou.gateway.config;

import com.leyo.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Configuration
@ConfigurationProperties(prefix = "ly.jwt")
@Data
public class JwtProperties {


    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey; //公钥

    //对象一旦实例化后，就应该读取公钥和私钥
    @PostConstruct
    public void init() throws Exception
    {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }





}
