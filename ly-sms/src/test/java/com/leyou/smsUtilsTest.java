package com.leyou;

import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * author 暗氵愧
 * HostName dell
 * Date 2018/12/25 17:39
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@EnableConfigurationProperties(SmsProperties.class)
public class smsUtilsTest {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private SmsProperties smsProperties;

    // TODO 封装成功
    @Test
    public void singleTest() throws Exception {

        // String code = UUID.randomUUID().toString();
        // code = code.substring(0, 6);
        int time = 1;
        String code = smsUtils.generateNumberCode();
        String content = "您的验证码是"+ code +"，在" + 1 + "分钟内有效。如非本人操作请忽略本短信。";
//        String content = "恭喜你，支付成功！";

        // 单条发送
        String phone = "18845120147";
        smsUtils.singleSend(phone, content);
    }

    @Test
    public void AmqpSendTest() throws InterruptedException {
        Map<String, String> msg = new HashMap<>();
        String code = smsUtils.generateNumberCode();
        String phone = "18846752837";
        String content = "您的验证码是"+ code +"，在" + 1 + "分钟内有效。如非本人操作请忽略本短信。";

        msg.put("phone", phone);
        msg.put("code", code);
        msg.put("content", content);
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);
    }

    @Test
    public void test()
    {
        System.out.println(smsProperties.getUserId());
        System.out.println(smsProperties.getPwd());
        System.out.println(smsProperties.getPwd());
        System.out.println(smsProperties.getMasterIpAddress());
    }
}
