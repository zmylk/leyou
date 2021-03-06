package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //路径看起来很陌生，如果出现问题，去找订单的路径，可能不一样
    //使用request body的原因 json对象

    /**
     * 创建订单的功能
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO)
    {

        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderId(@PathVariable("id")Long id)
    {
            return ResponseEntity.ok(orderService.queryOrderId(id));
    }

    @GetMapping("/url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id")Long id)
    {
        return ResponseEntity.ok(orderService.createPayUrl(id));
    }

    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id")Long orderId)
    {
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getPayStateCode());
    }


}
