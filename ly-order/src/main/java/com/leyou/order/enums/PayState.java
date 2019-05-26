package com.leyou.order.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 支付状态枚举对象
 */
@AllArgsConstructor
@NoArgsConstructor
public enum PayState {
    NOT_PAY(0),SUCCESS(1),FAIL(2);

    private int value;

    /**
     * 获取支付状态码
     *
     * @return
     */
    public int getPayStateCode(){
        return this.value;
    }
}
