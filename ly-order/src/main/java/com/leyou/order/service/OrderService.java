package com.leyou.order.service;

import com.leyo.auth.entity.UserInfo;


import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;


import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;


    public Long createOrder(OrderDTO orderDTO) {
        //1.新增订单
        Order order = new Order();
        //1.1订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        //1.2用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);
        //1.3收货人地址
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
        //1.4金额
        //把cartdto转化成一个map，key时sku的id，值是num
        Map<Long, Integer> numMap = orderDTO.getCarts().stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //获取所有id
        Set<Long> ids = numMap.keySet();
        //查询所有商品
        List<Sku> skus = goodsClient.querySkuBySpuIds(new ArrayList<>(ids));
        Long totalPay = 0L;

        //准备orderdetails的集合
        List<OrderDetail> details = new ArrayList<>();
        for (Sku sku : skus) {
            totalPay += sku.getPrice() * numMap.get(sku.getId());
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }
        order.setTotalPay(totalPay);
        //实付金额等于总的加邮费减去优惠
        order.setActualPay(totalPay + order.getPostFee() - 0);
        //1.5 写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1)
        {
            log.error("[创建订单失败，orderId:{}]",orderId);
            throw new LyException(ExceptionEnumm.CREATE_ORDER_ERROR);
        }

        //2 新增订单详情
        count = orderDetailMapper.insertList(details);
        if (count != details.size())
        {
            log.error("[创建订单失败，orderId:{}]",orderId);
            throw new LyException(ExceptionEnumm.CREATE_ORDER_ERROR);
        }
        //3 新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1)
        {
            log.error("[创建订单失败，orderId:{}]",orderId);
            throw new LyException(ExceptionEnumm.CREATE_ORDER_ERROR);
        }
        //4 减库存
        List<CartDTO> carts = orderDTO.getCarts();
        goodsClient.decreaseStock(carts);
        return orderId;
    }

    public Order queryOrderId(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null)
        {
            throw new LyException(ExceptionEnumm.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);
        if (select == null)
        {
            throw new LyException(ExceptionEnumm.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(select);
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if (orderStatus == null)
        {
            throw new LyException(ExceptionEnumm.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        //查询订单，获取订单总金额
        Order order = queryOrderId(orderId);

        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != OrderStatusEnum.UN_PAY.value())
        {
            throw new LyException(ExceptionEnumm.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = 1L;//order.getActualPay();
        //产品描述
        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String desc = orderDetail.getTitle();
        return payHelper.createOrder(orderId,actualPay,desc);
    }

    public void handleNotify(Map<String, String> result) {
        //1 数据校验
        payHelper.isSuccess(result);
        //2 校验签名
        payHelper.isValidSign(result);

        //3 校验金额
        String totalFreeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFreeStr) || StringUtils.isEmpty(tradeNo))
        {
            throw new LyException(ExceptionEnumm.INVALID_ORDER_PARAM);
        }
        //3.1 获取结果中的金额
        Long totalFree = Long.valueOf(totalFreeStr);
        //获取订单金额
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (totalFree != 1)
        {
            //金额不符
            throw new LyException(ExceptionEnumm.INVALID_ORDER_PARAM);
        }

        //4 修改订单状态

        OrderStatus status = new OrderStatus();
        status.setStatus(OrderStatusEnum.PAYED.value());
        status.setOrderId(orderId);
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count != 1)
        {
            throw new LyException(ExceptionEnumm.UPDATE_ORDER_STATUS_ERROR);

        }

        log.info("[订单回调]， 订单支付成功！ 订单编号:{}" ,orderId);
    }

    public PayState queryOrderState(Long orderId) {

        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();

        //判断是否支付
        if (status != OrderStatusEnum.UN_PAY.value())
        {
            //如果已支付那就是真的支付了
            return PayState.SUCCESS;
        }

        //如果未支付其实也不一定是未支付，必须去微信查询支付状态

        return payHelper.queryPayState(orderId);
    }
}
