package com.youlexuan.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.youlexuan.entity.PageResult;
import com.youlexuan.entity.Result;
import com.youlexuan.pojo.TbBrand;
import com.youlexuan.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {


    @Reference
    private BrandService brandService;


    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return  brandService.findAll();
    }

    /**
     * easyUI:前台框架，应用场景互联网项目的后台管理系统
     *          分页组件datagrid.,前台传后台的参数名 page,rows
     *          后台传给前台的数据json,key，total,rows
     * @param pageNum
     * @param pageSize
     * @return
     *
     * angularjs 前台分页步骤
     * 1、引入分页插件 js\css
     * 2、定义app时加载插件
     * 3、在html页面中分页的 dom 加载
     */
    @RequestMapping("/findPage")
    public PageResult findPage(
            @RequestParam("page") int pageNum, @RequestParam("rows") int pageSize){
        return brandService.findPage(null,pageNum,pageSize);
    }

    @RequestMapping("/search")
    public PageResult search(
            @RequestBody TbBrand brand,
            @RequestParam("page") int pageNum, @RequestParam("rows") int pageSize){
        return brandService.findPage(brand,pageNum,pageSize);
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.insert(brand);
            return new Result(true,"添加品牌成功");
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, e.toString());
         }
    }

        @RequestMapping("/update")
        public Result update(@RequestBody TbBrand brand){
            try {
                brandService.update(brand);
                return new Result(true,"添加品牌成功");
            }catch (Exception e) {
                e.printStackTrace();
                return new Result(false, e.toString());
            }
    }

    @RequestMapping("/findOne")
    public TbBrand findOne(long id) {
        return brandService.findOne(id);
    }

    @RequestMapping("/delet")
    public Result delet(Long[] ids) {

        try {
            brandService.delte(ids);
            return new Result(true,"删除成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,e.toString());
        }
    }

    /**
     * 查询所有的品牌，做为select2的选项数据
     */

    @RequestMapping("/findBrandList")
    public List<Map> findBrandList(){
        return brandService.selectOption();
    }
}
