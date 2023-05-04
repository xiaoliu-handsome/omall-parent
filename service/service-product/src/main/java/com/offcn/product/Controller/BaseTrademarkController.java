package com.offcn.product.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.result.Result;
import com.offcn.model.product.BaseTrademark;
import com.offcn.product.service.BaseTrademarkService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    @ApiOperation(value = "分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page,
                        @PathVariable Long limit) {

        Page<BaseTrademark> pageParam = new Page<>(page, limit);
        IPage<BaseTrademark> pageModel = baseTrademarkService.getPage(pageParam);
        return Result.ok(pageModel);
    }

    @ApiOperation(value = "获取BaseTrademark")
    @GetMapping("get/{id}")
    public Result get(@PathVariable String id) {
        BaseTrademark baseTrademark = baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }

    @ApiOperation(value = "新增BaseTrademark")
    @PostMapping("save")
    public Result save(@RequestBody BaseTrademark banner) {
        baseTrademarkService.save(banner);
        return Result.ok();
    }

    @ApiOperation(value = "修改BaseTrademark")
    @PutMapping("update")
    public Result updateById(@RequestBody BaseTrademark banner) {
        baseTrademarkService.updateById(banner);
        return Result.ok();
    }

    @ApiOperation(value = "删除BaseTrademark")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id) {
        baseTrademarkService.removeById(id);
        return Result.ok();
    }
    /**
     * 查询全部品牌
     * @return
     */
    @GetMapping("getTrademarkList")
    public Result<List<BaseTrademark>> getTrademarkList() {
        List<BaseTrademark> trademarkList = baseTrademarkService.list(null);
        return Result.ok(trademarkList);
    }

}
