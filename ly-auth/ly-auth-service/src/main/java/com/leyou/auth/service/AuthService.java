package com.leyou.auth.service;

import com.leyo.auth.entity.UserInfo;
import com.leyo.auth.utils.JwtUtils;
import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties jwtProperties;

    public String login(String username, String passwrod) {
        try {
            //校验用户名和密码
            User user = userClient.queryUserByUsernameAndPassword(username, passwrod);
            //判断
            if (user == null)
            {
                throw new LyException(ExceptionEnumm.INVALID_USERNAME_PASSWORD);
            }
            //生成token
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), username), jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            return token;
        } catch (Exception e) {
            log.error("[授权中心] 用户名或密码错误，用户名：{}",username,e);
            e.printStackTrace();
            throw new LyException(ExceptionEnumm.INVALID_USERNAME_PASSWORD);
        }
    }
}
