package com.leyou.order.utils;

import com.github.wxpay.sdk.WXPay;
import static com.github.wxpay.sdk.WXPayConstants.*;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.order.config.PayConfig;
import com.leyou.order.enums.OrderStatusEnum;
//import com.leyou.order.enums.PayState;
import com.leyou.order.enums.PayState;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static com.github.wxpay.sdk.WXPayConstants.SignType;

/**
 * 支付工具类
 *
 * author 暗氵愧
 * HostName dell
 * Date 2019/1/9 13:03
 */
@Component
@Slf4j
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    @Autowired
    private PayConfig config;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    /**
     * 创建微信支付订单
     *
     * @param orderId
     * @param totalPay
     * @param desc
     * @return
     */
    public String createOrder(Long orderId, Long totalPay, String desc){
        try{
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            // 金额，单价是分
            data.put("total_fee", totalPay.toString());
            // 调用微信支付的终端IP
            data.put("spbill_create_ip", "127.0.0.1");
            // 回调地址
            data.put("notify_url", config.getNotifyUrl());
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");

            // 利用wxPay工具，完成下单
            Map<String, String> result = wxPay.unifiedOrder(data);

            // 判断通信和业务标识

             isSuccess(result);

            // 下单成功，获取支付链接
            String code_url = result.get("code_url");
            return code_url;
        }catch (Exception e){
            log.error("[微信下单] 创建预交易异常失败", e);
            return null;
        }
    }

    /**
     * 判断通信和业务标识
     *
     * @param result
     */
    public void isSuccess(Map<String, String> result) {
        // 判断通信标识
        String returnCode = result.get("return_code");
        if(FAIL.equals(returnCode)){ // 静态导入WXPayConstants类
            // 记录日志，写入失败信息
            log.error("[微信下单] 微信下单通信失败，失败原因：{}", result.get("return_msg"));
            throw new LyException(ExceptionEnumm.WX_PAY_ORDER_FAIL);
        }

        String resultCode = result.get("result_code");
        if(FAIL.equals(resultCode)){ // 静态导入WXPayConstants类
            // 记录日志，写入失败信息，错误信息，根据{}的位置来写入
            log.error("[微信下单] 微信下单业务失败，错误码：{}，错误原因：{}",
                    result.get("err_code"), result.get("err_code_des"));
            throw new LyException(ExceptionEnumm.WX_PAY_ORDER_FAIL);
        }
    }

    /**
     * 校验签名
     *
     * @param data
     */
    public void isValidSign(Map<String, String> data) {
        // 重新生成签名
        try{
            String sign1 = WXPayUtil.generateSignature(data, config.getKey(), SignType.HMACSHA256);
            String sign2 = WXPayUtil.generateSignature(data, config.getKey(), SignType.MD5);

            String sign = data.get("sign");
            if(!StringUtils.equals(sign, sign1) && !StringUtils.equals(sign, sign2)){
                throw new LyException(ExceptionEnumm.INVALID_SIGN_ERROR);
            }
        }catch (Exception e){
            log.error("[微信签名] 签名生成失败");
            throw new LyException(ExceptionEnumm.INVALID_SIGN_ERROR);
        }
        // 和传过来的签名进行比较
    }

    /**
     * 根据订单编号查询支付状态
     *
     * @param orderId
     */
    public PayState queryPayState(Long orderId) {
        // 组织请求参数
        Map<String, String> reqData = new HashMap<>();
        // 订单号
        reqData.put("out_trade_no", orderId.toString());
        try {
            // 查询订单状态获取结果
            Map<String, String> result = wxPay.orderQuery(reqData);

            // 校验支付状态
            isSuccess(result);

            // 校验签名
            isValidSign(result);

            // 校验金额
            // 3 校验订单金额是否一致
            String totalFeeStr = result.get("total_fee");
            String tradeNo = result.get("out_trade_no");
            if(StringUtils.isBlank(totalFeeStr) || StringUtils.isBlank(tradeNo)){
                throw new LyException(ExceptionEnumm.INVALID_ORDER_PARAM);
            }

            // 查询订单
            // Order order = orderMapper.selectByPrimaryKey(orderId);

            // 获取结果中的金额
            Long totalFee = Long.valueOf(totalFeeStr);

            // 3.2 校验订单的金额与实付金额是否一致（这里我们把所有商品都设置成了1分钱...）
            if(totalFee != /* order.getActualPay() */ 1L){
                // 金额不符，抛异常
                throw new LyException(ExceptionEnumm.INVALID_ORDER_PARAM);
            }

            /**
             * SUCCESS—支付成功
             * REFUND—转入退款
             * NOTPAY—未支付
             * CLOSED—已关闭
             * REVOKED—已撤销（付款码支付）
             * USERPAYING--用户支付中（付款码支付）
             * PAYERROR--支付失败(其他原因，如银行返回失败)
             */

            // 获取交易状态
            String state = result.get("trade_state");

            // 查询订单状态
            OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);

            // 支付成功
            if(WXPayConstants.SUCCESS.equals(state)){
                // 4 修改订单状态
                // 4.1 根据订单id查询订单状态

                // 4.2 校验订单状态
                if(OrderStatusEnum.UN_PAY.value() != orderStatus.getStatus()){
                    throw new LyException(ExceptionEnumm.ORDER_STATUS_ERROR);
                }

                // 4.3 设置订单属性
                orderStatus.setOrderId(orderId);
                orderStatus.setStatus(OrderStatusEnum.PAYED.value());
                orderStatus.setPaymentTime(new Date());

                // 4.4 修改订单
                int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
                if(count != 1){
                    throw new LyException(ExceptionEnumm.UPDATE_ORDER_STATUS_ERROR);
                }
                return PayState.SUCCESS;
            }

            // 未支付
            if("NOTPAY".equals(state) || "USERPAYING".equals(state)){
                return PayState.NOT_PAY;
            }

            // 支付失败
            return PayState.FAIL;
        } catch (Exception e) {
            return PayState.NOT_PAY;
        }
    }
}
