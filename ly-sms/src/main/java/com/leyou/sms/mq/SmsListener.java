package com.leyou.sms.mq;

import com.leyou.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 监听业务消息，发送短信
 *
 * author 暗氵愧
 * HostName dell
 * Date 2018/12/25 20:45
 */
@Slf4j
@Component
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    /**
     * 发送短信验证码
     *
     * @param msg 该map要存储phone：手机号码、code：验证码、content：短信的内容
     */
    @RabbitListener(bindings =
        @QueueBinding(value = @Queue(value = "sms.verify.code.queue", durable = "true"),
        exchange = @Exchange(name = "ly.sms.exchange", type = ExchangeTypes.TOPIC),
        key = "sms.verify.code"
    ))
    public void ListenerRegister(Map<String, String> msg){
        if(CollectionUtils.isEmpty(msg)){
            return;
        }
        // 从map中获取值后删除掉，map中就没有phone这个键值对了
        String phone = msg.remove("phone");
        String code = msg.get("code");
        String content = msg.get("content");
        if(StringUtils.isBlank(phone)){
            return;
        }
        // 短信发送出现异常最好不要try起来，抛异常会触发重试机制。
        Integer result = smsUtils.singleSend(phone, content);
        if(result.equals(1)){
            log.info("[短信服务]，发送短信验证码，手机号：" + phone);
        }
    }
}

















