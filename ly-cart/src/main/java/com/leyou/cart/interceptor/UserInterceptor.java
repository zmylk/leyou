package com.leyou.cart.interceptor;

import com.leyo.auth.entity.UserInfo;
import com.leyo.auth.utils.JwtUtils;
import com.leyou.cart.config.JwtProperties;
import com.leyou.common.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Slf4j
public class UserInterceptor implements HandlerInterceptor {


    private JwtProperties jwtProperties;
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();

    public UserInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //获取cookie
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            //解析token
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //传递用户
            tl.set(user);
            return true;
        } catch (Exception e) {
            log.error("[购物车服务] 解析用户身份失败",e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        //用完数据清空
        tl.remove();
    }

    public static UserInfo getUser()
    {
        return tl.get();
    }
}
