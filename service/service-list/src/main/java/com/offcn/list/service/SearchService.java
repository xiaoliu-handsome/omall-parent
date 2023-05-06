package com.offcn.list.service;

public interface SearchService {
    /**
     * 上架商品列表
     * @param skuId
     */
    void upperGoods(Long skuId);

    /**
     * 下架商品列表
     * @param skuId
     */
    void lowerGoods(Long skuId);
    /**
     * 更新热点访问次数
     * @param skuId
     */
    void incrHotScore(Long skuId);

}
