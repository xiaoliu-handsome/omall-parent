package com.offcn.list.service;

import com.offcn.model.list.SearchParam;
import com.offcn.model.list.SearchResponseVo;

import java.io.IOException;

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
    /**
     * 搜索列表
     * @param searchParam
     * @return
     * @throws IOException
     */
    SearchResponseVo search(SearchParam searchParam) throws IOException;

}
