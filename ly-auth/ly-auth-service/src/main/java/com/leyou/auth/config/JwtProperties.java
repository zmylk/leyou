package com.leyou.auth.config;

import com.leyo.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
@ConfigurationProperties(prefix = "ly.jwt")
@Data
public class JwtProperties {

    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private int expire;
    private String cookieName;

    private PublicKey publicKey; //公钥
    private PrivateKey privateKey; //私钥

    //对象一旦实例化后，就应该读取公钥和私钥
    @PostConstruct
    public void init() throws Exception
    {
        //公钥私钥不纯在先生成
        File pubPath = new File(pubKeyPath);
        File priPath = new File(priKeyPath);
        if (!pubPath.exists() || !priPath.exists())
        {
            RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
        }
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }





}
