package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key",required = false) String key
    ) {
        return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));
    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu)
    {
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spuId查询详情detail
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> queryDetailById(@PathVariable("id")Long spuId)
    {
        return ResponseEntity.ok(goodsService.queryDetailById(spuId));
    }

    /**
     * 根据spuid查询sku所有信息
     * @param spuId
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id")Long spuId)
    {
        return ResponseEntity.ok(goodsService.querySkuBySpuId(spuId));
    }
    /**
     * 根据spuid的集合查询sku所有信息
     * @param spuId
     * @return
     */
    @GetMapping("/sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuBySpuIds(@RequestParam("ids")List<Long> ids)
    {
        return ResponseEntity.ok(goodsService.querySkuBySpuIds(ids));
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu)
    {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id)
    {
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts)
    {
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
