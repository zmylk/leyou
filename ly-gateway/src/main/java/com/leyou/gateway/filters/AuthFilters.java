package com.leyou.gateway.filters;

import com.leyo.auth.entity.UserInfo;
import com.leyo.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
@Component
public class AuthFilters extends ZuulFilter {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;//过滤器类型，前置过了。
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER -1;//过滤顺序
    }

    //是否过滤
    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取请求的url
        String path = request.getRequestURI();
//        String method = request.getMethod();
        //判断是否放行，放行返回flase
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path) {
        //遍历白名单
        for (String allowPath : filterProperties.getAllowPaths()) {
            //判断是否允许
            if (path.startsWith(allowPath))
            {
                return true;
            }
        }
        return false;
    }


    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取cookie中token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            // TODO 校验权限
        } catch (Exception e) {
            //解析token失败未登录，拦截！
            ctx.setSendZuulResponse(false);
            //返回状态码
            ctx.setResponseStatusCode(403);
        }
        //解析token

        //校验权限
        return null;
    }
}
