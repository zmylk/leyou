package com.leyou.sms.utils;

import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 短信工具类
 *
 * author 暗氵愧
 * HostName dell
 * Date 2018/12/25 16:50
 */
@Component
@Slf4j
@EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String KEY_PREFIX = "sms:phone:";

    /**
     * 短信验证最短的验证时间：60000 毫秒
     */
    private final static Long SMS_MIN_INTERVAL_IN_MILLIS = 60000L;

    /**
     * 单条发送短信
     *
     * @param phone
     * @param content
     */
    public Integer singleSend(String phone, String content){
        // 发送短信之前读取时间
        String key = KEY_PREFIX + phone;
        String lastTime = redisTemplate.opsForValue().get(key);
        // 如果发送短信验证期间小于60秒则不发送短信
        if(StringUtils.isNotBlank(lastTime)){
            Long time = Long.valueOf(lastTime);
            if(System.currentTimeMillis() - time < SMS_MIN_INTERVAL_IN_MILLIS){
                log.info("[短信服务] 发送短信频率过高，被拦截，手机号码：{}", phone);
                return -1;
            }
        }

        // 发送短信
        try{
            int result = SendSmsTemlpate.singleSend(phone, content, smsProperties.getUserId(), smsProperties.getPwd());
            if(result != 0){
                log.error("[短信服务] 发送信息失败，失败手机号码：" + phone);
            }
            // 短信发送成功后，写入redis，在set方法中还能过期时间，设置1分钟之后就删除
            redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);
            return result;
        }catch (Exception e){
            log.error("[短信服务] 发送短信异常， 手机号:{}", phone, e);
        }
        return -1;
    }

    /**
     * 相同内容群发
     *
     * @param phone
     * @param content
     */
    public void batchSend(String phone, String content){
        SendSmsTemlpate.batchSend(phone, content, smsProperties.getUserId(), smsProperties.getPwd());
    }

    /**
     * 个性化群发
     *
     * @param multixMts
     */
    public void multiSend(List<MultiMt> multixMts){
        SendSmsTemlpate.multiSend(multixMts, smsProperties.getUserId(), smsProperties.getPwd());
    }

    /**
     * 查询余额
     */
    public void getBalance(){
        SendSmsTemlpate.getBalance(smsProperties.getUserId(), smsProperties.getPwd());
    }

    /**
     * 查询剩余金额或条数接
     */
    public void getRemains(){
        SendSmsTemlpate.getRemains(smsProperties.getUserId(), smsProperties.getPwd());
    }

    /**
     * 生成纯数字的6位随机数验证码
     *
     * @return
     */
    public String generateNumberCode(){
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0;i < 6;i++){
            sb.append(Integer.valueOf(new Random().nextInt(10)));
        }
        return sb.toString();
    }

    /**
     * 生成6位数字和字母的验证码
     *
     * @return
     */
    public String generateCode(){
        String code = UUID.randomUUID().toString().substring(0, 6);
        return code;
    }
}
