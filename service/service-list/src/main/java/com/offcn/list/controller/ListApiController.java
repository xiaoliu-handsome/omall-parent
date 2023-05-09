package com.offcn.list.controller;

import com.offcn.common.result.Result;
import com.offcn.list.service.SearchService;
import com.offcn.model.list.Goods;
import com.offcn.model.list.SearchParam;
import com.offcn.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("api/list")
public class ListApiController {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchService searchService;

    /**
     * @return
     */
    @GetMapping("inner/createIndex")
    public Result createIndex() {
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);
        return Result.ok();
    }
    /**
     * 上架商品
     * @param skuId
     * @return
     */
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId) {
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    /**
     * 下架商品
     * @param skuId
     * @return
     */
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId) {
        searchService.lowerGoods(skuId);
        return Result.ok();
    }
    /**
     * 更新商品incrHotScore
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable("skuId") Long skuId) {
        // 调用服务层
        searchService.incrHotScore(skuId);
        return Result.ok();
    }
    /**
     * 搜索商品
     * @param searchParam
     * @return
     * @throws IOException
     */
    @PostMapping
    public Result list(@RequestBody SearchParam searchParam) throws IOException {
        SearchResponseVo response = searchService.search(searchParam);
        return Result.ok(response);
    }

}
