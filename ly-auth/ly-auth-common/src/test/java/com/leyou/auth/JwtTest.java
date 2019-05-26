package com.leyou.auth;

import com.leyo.auth.entity.UserInfo;
import com.leyo.auth.utils.JwtUtils;
import com.leyo.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/1 13:44
 */
public class JwtTest {

    // 公钥和秘钥生成的位置
    private static final String pubKeyPath = "D:\\heima\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\heima\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    /**
     * 获取公钥和秘钥设置到对象中
     *
     * @throws Exception
     */
    @Test
    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    /**
     * 生成token
     *
     * @throws Exception
     */
    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"),
                privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU1NzY0NjIzN30.QQrjaemDFznSpuUv_S3y3umpiTH7Baxx9ijGTaZQPf1zRm3PUCMZGeDqigAya5s13L12sgJGkifhe6tNDwpmOn4smFczxmWQ6UUnWCUK8M8SYLzC3baPcNSnYWJ_TvtxSxg9YI3GGooxtafLgX1gKJOoAWSDN0_KXsspMUjeXAg";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
