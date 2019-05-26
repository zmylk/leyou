package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.beans.Transient;
import java.lang.reflect.Executable;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {

        PageHelper.startPage(page, rows);
        // 创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        // 是否过滤上下架
        if (saleable != null) {
            criteria.orEqualTo("saleable", saleable);
        }

        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 默认排序
        example.setOrderByClause("last_update_time DESC");

        // 查询
        List<Spu> spus = spuMapper.selectByExample(example);

        // 判断
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnumm.GOODS_NOT_FOND);
        }

        // 解析分类和
        loadCategoryAndBrandName(spus);

        // 解析分页的结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(), spus);
    }
    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // 处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    @Transient
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count!=1)
        {
            throw new LyException(ExceptionEnumm.GOOD_SAVE_ERROR);
        }
        //新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        spuDetailMapper.insert(detail);
        //新增库存
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());

    }

    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null)
        {
            throw new LyException(ExceptionEnumm.CATEGORY_DETAIL_NOT_FOND);
        }
        return spuDetail;
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList))
        {
            throw new LyException(ExceptionEnumm.GOOD_SKU_NOT_FOND);
        }
        //查询库存
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockinSku(ids, skuList);

        return skuList;
    }




    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null)
        {
            throw new LyException(ExceptionEnumm.GOODS_ID_CAN_NOT);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList))
        {
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count !=1)
        {
            throw new LyException(ExceptionEnumm.GOODS_UPDATE_ERROR);
        }
        //修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count !=1)
        {
            throw new LyException(ExceptionEnumm.DETAIL_UPDATE_ERROR);
        }
        //新增stock和sku
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }




    private void saveSkuAndStock(Spu spu) {
        //定义库存集合
        List<Stock> stockList = new ArrayList<>();
        //新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku: skus)
        {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            int lk = skuMapper.insert(sku);
            if (lk != 1)
            {
                throw new LyException(ExceptionEnumm.GOOD_SAVE_ERROR);
            }

            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }

        //批量新增库存
        int i = stockMapper.insertList(stockList);
        if (i!=stockList.size())
        {
            throw new LyException(ExceptionEnumm.GOOD_SAVE_ERROR);
        }

    }

    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null)
        {
            throw  new LyException(ExceptionEnumm.GOODS_NOT_FOND);
        }
        //查询sku
        spu.setSkus(querySkuBySpuId(id));
        //查询详情
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    public List<Sku> querySkuBySpuIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus))
        {
            throw new LyException(ExceptionEnumm.GOODS_NOT_FOND);
        }
        loadStockinSku(ids, skus);
        return skus;
    }

    private void loadStockinSku(List<Long> ids, List<Sku> skus) {
        //查询库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new LyException(ExceptionEnumm.GOOD_STORE_NOT_FOND);
        }
        //把stock变成一个map，其中key是sku的id，值是其库存，
        Map<Long, Integer> stockMap = stockList.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s -> s.setStock(stockMap.get(s.getId())));
    }

    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            //减少库存
            int i = stockMapper.descreaseStock(cart.getSkuId(), cart.getNum());
            if (i !=1 )
            {
                throw new LyException(ExceptionEnumm.STOCK_NOT_ENOUGH);
            }
        }
    }
}
