package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.util.StringUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class Userservice {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone:";

    public Boolean checkData(String data, Integer type) {
        User record = new User();
        switch (type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnumm.USER_DATA_TYPE_ERROR);
        }
        return userMapper.selectCount(record) == 0;
    }

    public void sendCode(String phone) {
        // TODO 完成剩下的操作，验证码怎么解决，短信内容的定义
        Map msg = new HashMap<>();
        // 生成验证码
        String key = KEY_PREFIX + phone; // 验证码的key
        String code = NumberUtils.generateCode(6); // 生成6为随机数的验证码
        int outTime = 5; // 验证码过期时间为5分钟
        String content = "您的注册验证码是" + code + "，在" + outTime + "分钟内输入有效。如非本人操作请忽略此短信。";

        msg.put("code", code);
        msg.put("phone", phone);
        msg.put("content", content);

        // 发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);

        // 保存验证码
        redisTemplate.opsForValue().set(key, code, outTime, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {
        //从redis中获取验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //校验验证码
        if (!StringUtils.equals(cacheCode,code)) {
            throw new LyException(ExceptionEnumm.INVALID_VERIFY_CODE);
        }
        //生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        //对密码进行加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
        //写入数据库
        user.setCreated(new Date());
        userMapper.insert(user);
    }

    public User queryUserAndPassword(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = userMapper.selectOne(record);

        //校验
        if (user == null) {
            throw new LyException(ExceptionEnumm.INVALID_USERNAME_PASSWORD);
        }
        //校验密码
        if (!StringUtils.equals(user.getPassword(), CodecUtils.md5Hex(password,user.getSalt()))) {
            throw new LyException(ExceptionEnumm.INVALID_USERNAME_PASSWORD);
        }
        //用户密码正确
        return user;
    }
}
