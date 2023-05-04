package com.offcn.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.item.service.ItemService;
import com.offcn.list.client.ListFeignClient;
import com.offcn.model.product.BaseCategoryView;
import com.offcn.model.product.SkuInfo;
import com.offcn.model.product.SpuSaleAttr;
import com.offcn.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ListFeignClient listFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

        @Override
        public Map<String, Object> getBySkuId (Long skuId){
            Map<String, Object> result = new HashMap<>();

            // 通过skuId 查询skuInfo
            CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
                SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
                // 保存skuInfo
                result.put("skuInfo", skuInfo);
                return skuInfo;
            }, threadPoolExecutor);
            // 销售属性-销售属性值回显并锁定
            //当获取skuInfo的线程执行完毕后，获取返回的结果，在执行获取销售属性值
            CompletableFuture<Void> spuSaleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
                List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
                result.put("spuSaleAttrList", spuSaleAttrList);
            }, threadPoolExecutor);


            //根据spuId 查询map 集合属性
            //当获取skuInfo的线程执行完毕后，获取返回的结果，在执行根据spuId 查询map 集合属性
            CompletableFuture<Void> skuValueIdsMapcompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
                Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
                String valuesSkuJson = JSON.toJSONString(skuValueIdsMap);
                result.put("valuesSkuJson", valuesSkuJson);
            }, threadPoolExecutor);


            //获取商品最新价格
            //当获取skuInfo的线程执行完毕后，获取返回的结果，在执行获取商品最新价格
            CompletableFuture<Void> skuPricecompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
                BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
                result.put("price", skuPrice);
            }, threadPoolExecutor);

            //获取商品分类
            //当获取skuInfo的线程执行完毕后，获取返回的结果，在执行获取商品分类
            CompletableFuture<Void> categoryViewcompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
                BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            //保存商品分类数据
                result.put("categoryView", categoryView);
            }, threadPoolExecutor);

            //更新商品incrHotScore
            CompletableFuture<Void> incrHotScoreCompletableFuture = CompletableFuture.runAsync(() -> {
                listFeignClient.incrHotScore(skuId);
            }, threadPoolExecutor);


            //多线程阻塞，等待全部线程执行完成，在继续执行
            CompletableFuture.allOf(skuInfoCompletableFuture, spuSaleAttrCompletableFuture, skuValueIdsMapcompletableFuture, skuPricecompletableFuture,incrHotScoreCompletableFuture, categoryViewcompletableFuture).join();

            return result;
        }


}
