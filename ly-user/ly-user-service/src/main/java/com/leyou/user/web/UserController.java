package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.Userservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    private Userservice userservice;

    /**
     * 校验数据
     * @param data
     * @param type
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,@PathVariable("type")Integer type)
    {
        return ResponseEntity.ok(userservice.checkData(data,type));
    }

    /**
     * 发送短信验证码
     * @param phone
     * @return
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone")String phone)
    {
        userservice.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 注册用户
     * @param user
     * @param result
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid  User user , BindingResult result, @RequestParam("code") String code)
    {
//        if (result.hasFieldErrors())
//        {
//            throw new RuntimeException(result.getFieldErrors().stream()
//                    .map(e->e.getDefaultMessage()).collect(Collectors.joining("|")));
//
//        }
        userservice.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/query")
    public ResponseEntity<User> queryUserAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    ){
        return ResponseEntity.ok(userservice.queryUserAndPassword(username,password));
    }
}
