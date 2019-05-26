package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnumm;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.repository.GoodsRespository;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;

import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;



import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRespository goodsRespository;

    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu)
    {
        Long spuId = spu.getId();
        //查询分类
        List<Category> categories = categoryClient.queryCategoryLIstByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())
        );
        if (CollectionUtils.isEmpty(categories))
        {
            throw new LyException(ExceptionEnumm.CATEGORY_NOT_FOND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null)
        {
            throw  new LyException(ExceptionEnumm.BRAND_NOT_FOUND);
        }
        String all = spu.getTitle() + StringUtils.join(names," ") + brand.getName();
        //查询sku
        List<Sku> skusList = goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skusList))
        {
            throw  new LyException(ExceptionEnumm.GOOD_SKU_NOT_FOND);
        }
        //对sku进行处理
        List<Map<String,Object>> skus = new ArrayList<>();
        Set<Long> priceList = new HashSet<>();
        for (Sku sku : skusList)
        {
            Map<String,Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skus.add(map);
            priceList.add(sku.getPrice());
        }

        //查询规格参数
        List<SpecParam> params = specificationClient.queryParamsByList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params))
        {
            throw  new LyException(ExceptionEnumm.SPEC_PRAM_NOT_FOND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spuId);

        //获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获取特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.
                nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});
        //规格参数 key使规格参数的key 值是规格参数的值
        Map<String,Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            //规格参数名称
            String key = param.getName();
            Object value = "";
            //判断是否是通用规格参数
            if (param.getGeneric())
            {
                value = genericSpec.get(param.getId());
                //判断是否是数值类型
                if (param.getGeneric())
                {
                    //处理分段
                    value = chooseSegment(value.toString(),param);
                }
            }
            else
            {
                value = specialSpec.get(param.getId());
            }
            specs.put(key,value);
        }
        //构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setAll(all);
        goods.setPrice(priceList); //所有价格的sku集合
        goods.setSkus(JsonUtils.serialize(skus)); // 所有sku的json集合
        goods.setSpecs(specs); // 所有可能搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest searchRequest) {
        int page = searchRequest.getPage() -1;
        int size = searchRequest.getSize();
        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //过滤
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", searchRequest.getKey());
        QueryBuilder basicQuery = buildBasicQuery(searchRequest);
        queryBuilder.withQuery(basicQuery);
        //3聚合分类和品牌
        //3.1局和分类
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //3.2聚合品牌
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //查询
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //解析结果
        //解析分页结果
        long totalElements = result.getTotalElements();
        long totalPages = result.getTotalPages();
        List<Goods> content = result.getContent();
        //解析聚合结果
        Aggregations aggregations = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggregations.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggregations.get(brandAggName));

        //6.完成spec 规格参数聚合
        List<Map<String,Object>> specs = null;
        if (categories != null && categories.size() ==1)
        {
            //商品分类存在并且数量为1，可以聚合规格参数
            specs = buildSpecificationAgg(categories.get(0).getId(),basicQuery);
        }
        return new SearchResult(totalElements,totalPages,content,categories,brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //1.创建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //2.查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        //3.过滤条件
        Map<String, String> map = request.getFilter();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            //处理key
            if (!"cid".equals(key) && !"brandId".equals(key))
            {
                key = "specs."+key+".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        //查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.queryParamsByList(null,cid,true);
        //2.聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1带上查询条件
        queryBuilder.withQuery(basicQuery);
        //2.2聚合
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        //3.获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //4.解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            String name = param.getName();
            StringTerms terms = aggs.get(name);

            //准备map
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",terms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber()
                    .longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        } catch (Exception e) {
            log.error("[搜索服务]查询品牌异常",e);
            return  null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber()
                    .longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryLIstByIds(ids);
            return categories;
        } catch (Exception e) {
            log.error("[搜索服务]查询分类异常",e);
            return  null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRespository.save(goods);
    }


    public void deleteIndex(Long spuId) {
        goodsRespository.deleteById(spuId);
    }
}
