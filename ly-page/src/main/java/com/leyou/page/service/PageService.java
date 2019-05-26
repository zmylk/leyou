package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
@Slf4j
@Service
public class PageService {

    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //sku
        List<Sku> skus = spu.getSkus();
        //查询详情
        SpuDetail spuDetail = spu.getSpuDetail();
        //查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询商品分类
        List<Category> categories = categoryClient.queryCategoryLIstByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数
        List<SpecGroup> groups = specificationClient.queryGroupByCid(spu.getCid3());


        //补丁
        // 查询商品分类下的特有规格参数
        List<SpecParam> params =
                this.specificationClient.queryParamsByList(null, spu.getCid3(), null);
        // 处理成id:name格式的键值对
        Map<Long,String> paramMap = new HashMap<>();
        for (SpecParam param : params) {
            paramMap.put(param.getId(), param.getName());
        }



        Map<String, Object> modl = new HashMap<>();
        modl.put("groups",groups);
        modl.put("paramMap",paramMap);
        modl.put("categories",categories);
        modl.put("spu",spu);
        modl.put("spuDetail",spuDetail);
        modl.put("skus",skus);
        modl.put("brand",brand);

        return modl;
    }

    public void crateHtml(Long spuId)
    {
        //上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File dest = new File("D:/favorcode/XiangMu/upload", spuId + ".html");
        if (dest.exists())
        {
            dest.delete();
        }
        try {
            PrintWriter writer = new PrintWriter(dest,"UTF-8");
            templateEngine.process("item",context,writer);
        } catch (Exception e) {
            log.error("静态页服务错误！",e);
        }
        //生成html
    }

    public void deleteHtml(Long spuId) {
        File dest = new File("D:/favorcode/XiangMu/upload", spuId + ".html");

        if (dest.exists())
        {
            dest.delete();
        }
    }
}
