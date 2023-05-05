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

}
