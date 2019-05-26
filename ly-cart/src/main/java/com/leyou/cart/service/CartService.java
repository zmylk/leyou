package com.leyou.cart.service;

import com.leyo.auth.entity.UserInfo;
import com.leyo.auth.utils.JwtUtils;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private  static final String KEY_PREFIX = "cart:uid:";
    public void addCart(Cart cart) {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();
        //hashKey
        String hashKey = cart.getSkuId().toString();
        Integer num = cart.getNum();
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        //判断当前商品是否存在
        if (operation.hasKey(hashKey))
        {
            //存在，修改数量 原来的！！！
            String json = operation.get(hashKey).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(cart.getNum()+num);

        }
        operation.put(hashKey, JsonUtils.serialize(cart));


        //判断购物车是否存在
    }

    public List<Cart> queryCartList() {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();
        if (!redisTemplate.hasKey(key))
        {
            //key不存在
            throw new LyException(ExceptionEnumm.CART_NOT_FOUND);
        }
        //获取登陆用户的所有数据
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        List<Cart> carts = operation.values().stream().
                map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
        return carts;
    }

    public void updateCartNum(Long skuId, Integer num) {
        //获取用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();
        String hashkey = skuId.toString();
        //获取操作
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        //查询
        if (!redisTemplate.hasKey(key))
        {
            //key不存在
            throw new LyException(ExceptionEnumm.CART_NOT_FOUND);
        }
        Cart cart = JsonUtils.parse(operation.get(skuId.toString()).toString(), Cart.class);
        cart.setNum(num);
        operation.put(hashkey,JsonUtils.serialize(cart));

    }

    public void deleteCart(Long skuId) {

        //获取用户
        UserInfo user = UserInterceptor.getUser();
        //key
        String key = KEY_PREFIX + user.getId();
        //删除
        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
