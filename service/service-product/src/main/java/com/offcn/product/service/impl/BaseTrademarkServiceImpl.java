package com.offcn.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.model.product.BaseTrademark;
import com.offcn.product.mapper.BaseTrademarkMapper;
import com.offcn.product.service.BaseTrademarkService;
import org.springframework.stereotype.Service;

@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {
    @Override
    public IPage<BaseTrademark> getPage(Page<BaseTrademark> pageParam) {
        QueryWrapper<BaseTrademark> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("id");
        return this.baseMapper.selectPage(pageParam,queryWrapper);
    }

}
