package com.leyou.user.api;


import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * user远程调用接口
 *
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/2 14:34
 */
public interface UserApi {

    /**
     * 用户登录
     *
     * @param username 账号
     * @param password 密码
     * @return
     */
    @GetMapping("/query")
    User queryUserByUsernameAndPassword(
            @RequestParam(required = true, value = "username") String username,
            @RequestParam(required = true, value = "password") String password);

}
