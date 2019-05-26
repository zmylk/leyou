package com.leyou.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnumm {
    PRICE_CANNOT_BE_NULL(400,"几个不能为空！"),
    CATEGORY_NOT_FOND(404,"商品没有查到！"),
    CATEGORY_DETAIL_NOT_FOND(404,"商品详情没有查到！"),
    GOOD_SKU_NOT_FOND(404,"商品SKU没有查到！"),
    GOOD_STORE_NOT_FOND(404,"商品库存没有查到！"),

    SPEC_GROUP_NOT_FOND(404,"商品规格组查到！"),
    SPEC_PRAM_NOT_FOND(404,"商品规格参数不存在！"),
    GOODS_NOT_FOND(404,"商品不存在！"),
    BRAND_NOT_FOUND(404,"品牌没有查到！"),
    BRAND_SAVE_ERROR(500,"品牌保存错误！"),
    CATEGORY_BRAND_SAVE_ERROR(500,"品牌商品中间表保存错误！"),
    UPLOAD_FILE_ERROR(500,"文件上传失败！"),
    INVALID_FILE_TYPE(400,"文件类型不允许！"),
    CATEGORY_NOT_FOUND(404,"商品信息没查到！"),
    ORDER_NOT_FOUND(404,"订单没有查到！"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情没有查到！"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态没有查到！"),
    GOOD_SAVE_ERROR(500,"新增商品失败！"),
    GOODS_UPDATE_ERROR(500,"更新商品失败！"),
    DETAIL_UPDATE_ERROR(500,"更新DETAIL失败！"),
    GOODS_ID_CAN_NOT(404,"商品id不能为空"),
    USER_DATA_TYPE_ERROR(400,"校验类型错误！"),
    INVALID_VERIFY_CODE(400,"验证码错误！"),
    INVALID_USERNAME_PASSWORD(400,"无效的用户名或密码错误！！"),
    CREATE_TOKEN_ERROR(500,"用户凭证生成失败！"),
    UN_AUTHORIZED(403,"未授权！"),
    CART_NOT_FOUND(404,"购物车为空"),
    CREATE_ORDER_ERROR(500,"创建订单失败！"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    WX_PAY_ORDER_FAIL(500,"微信下单失败"),
    ORDER_STATUS_ERROR(400,"订单状态异常，不正确"),
    INVALID_SIGN_ERROR(400,"无效的签名异常！"),
    INVALID_ORDER_PARAM(400,"订单参数异常"),
    UPDATE_ORDER_STATUS_ERROR(500,"订单参数异常")


    ;

    private int code;
    private String msg;

}
