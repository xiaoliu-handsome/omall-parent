package com.offcn.product.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.offcn.common.result.Result;
import com.offcn.model.product.BaseSaleAttr;
import com.offcn.model.product.SpuInfo;
import com.offcn.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // @ResponseBody + @Controller
@RequestMapping("admin/product")
public class SpuManageController {
    @Autowired
    private ManageService manageService;

    @GetMapping("{page}/{size}")
    public Result getSpuInfoPage(@PathVariable Long page, @PathVariable Long size, SpuInfo spuInfo){
        // 创建一个Page 对象
        Page<SpuInfo> spuInfoPage = new Page<>(page,size);
        // 获取数据
        IPage<SpuInfo> spuInfoPageList = manageService.getSpuInfoPage(spuInfoPage, spuInfo);
        // 将获取到的数据返回即可！
        return Result.ok(spuInfoPageList);
    }
    // 销售属性http://api.omall.com/admin/product/baseSaleAttrList
    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){
        // 查询所有的销售属性集合
        List<BaseSaleAttr> baseSaleAttrList = manageService.getBaseSaleAttrList();

        return Result.ok(baseSaleAttrList);
    }
    /**
     * 保存spu
     * @param spuInfo
     * @return
     */
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        // 调用服务层的保存方法
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }


}
