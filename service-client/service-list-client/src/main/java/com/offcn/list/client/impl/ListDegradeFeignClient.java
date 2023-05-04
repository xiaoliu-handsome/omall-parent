package com.offcn.list.client.impl;

import com.offcn.common.result.Result;
import com.offcn.list.client.ListFeignClient;
import com.offcn.model.list.SearchParam;
import org.springframework.stereotype.Component;

@Component
public class ListDegradeFeignClient implements ListFeignClient {


    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public Result list(SearchParam listParam) {
        return null;
    }

    @Override
    public Result upperGoods(Long skuId) {
        return null;
    }

    @Override
    public Result lowerGoods(Long skuId) {
        return null;
    }

}
