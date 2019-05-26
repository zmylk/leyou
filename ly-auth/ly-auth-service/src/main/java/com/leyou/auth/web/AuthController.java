package com.leyou.auth.web;

import com.leyo.auth.entity.UserInfo;
import com.leyo.auth.utils.JwtUtils;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@EnableConfigurationProperties({JwtProperties.class})
@RestController
public class AuthController {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private AuthService authService;
    @Value("${ly.jwt.cookieName}")
    private String cookieName;

    /**
     * 登陆授权功能
     * @param username
     * @param passwrod
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username")String username, @RequestParam("password")String passwrod,
                                      HttpServletResponse response, HttpServletRequest request)
    {
        //登陆
        String token = authService.login(username, passwrod);

        CookieUtils.setCookie(request, response, cookieName, token);
        return  ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 验证用户登录
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN")String token,
    HttpServletResponse response, HttpServletRequest request)
    {
        try {
            //解析token
            UserInfo info = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //刷新token，重新生成token
            String newtoken = JwtUtils.generateToken(info, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            CookieUtils.setCookie(request, response, cookieName, newtoken);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            //token以过期或者被篡改
            throw new LyException(ExceptionEnumm.UN_AUTHORIZED);
        }
    }
}
