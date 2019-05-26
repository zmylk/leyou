package com.leyou.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRespositoryTest {
    @Autowired
    private GoodsRespository respository;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void testCreateIndex()
    {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData()
    {
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            //查询spu信息
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
            List<Spu> spuList = result.getItems();
            if (CollectionUtils.isEmpty(spuList))
            {
                break;
            }
            //构建成goods
            List<Goods> goodsList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
            respository.saveAll(goodsList);
            page++;
            size = spuList.size();
        }
        while (size == 100);
    }

}