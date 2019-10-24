package com.youlexuan.sellergoods.service;

import com.youlexuan.entity.PageResult;
import com.youlexuan.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    List<TbBrand> findAll();

    //分页查询
    PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    void  insert(TbBrand brand);

    void update(TbBrand brand);

    TbBrand findOne(Long id);

    void delte(Long[] ids);

    public List<Map> selectOption();
}
