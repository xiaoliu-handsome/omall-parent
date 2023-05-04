package com.offcn.item.client;

import com.offcn.common.result.Result;
import org.springframework.stereotype.Component;

@Component
public class ItemDegradeFeignClient implements ItemFeignClient {


    @Override
    public Result getItem(Long skuId) {
        return Result.fail();
    }
}

