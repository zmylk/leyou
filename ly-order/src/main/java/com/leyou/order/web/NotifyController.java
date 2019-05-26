package com.leyou.order.web;


import com.github.wxpay.sdk.WXPayConstants;
import com.leyou.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    @GetMapping("{id}")
    public String hello(@PathVariable("id")Long id)
    {
        System.out.println("id = " + id);
        return "id: " + id;
    }

    @PostMapping(value = "pay", produces = "application/xml")
    public Map<String,String> hello(@RequestBody Map<String,String> result)
    {
        orderService.handleNotify(result);

        log.info("[支付回调] 接受微信支付回调，结果:{}",result);

        Map msg = new HashMap<>();
        msg.put("return_code", WXPayConstants.SUCCESS);
        msg.put("return_msg", "OK");
        return msg;
    }
}
