package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.additional.insert.InsertListMapper;

    public interface StockMapper extends BaseMapper<Stock> {

        /**
         * 减少商品库存
         *
         * @param id
         * @param num
         * @return
         */
        @Update("update tb_stock set stock = stock - #{num} where sku_id = #{id} AND stock >= #{num}")
        int descreaseStock(@Param("id")Long id, @Param("num")Integer num);
}
