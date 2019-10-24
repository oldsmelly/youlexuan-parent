package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.TbBrandMapper;
import com.youlexuan.pojo.TbBrand;
import com.youlexuan.pojo.TbBrandExample;
import com.youlexuan.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper brandMapper;

    @Override
    public List<TbBrand> findAll() {
        TbBrandExample exam = new TbBrandExample();
        return brandMapper.selectByExample(exam);
    }

    @Override
    public PageResult findPage(TbBrand brand,int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        TbBrandExample exam = new TbBrandExample();
        //条件查询
        if(brand!=null){
            if(!StringUtils.isEmpty(brand.getName())){
                exam.createCriteria().andNameLike("%"+brand.getName()+"%");
            }
        }

        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(exam);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void insert(TbBrand brand) {
        brandMapper.insertSelective(brand);
    }

    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKeySelective(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    @Override
    public void delte(Long[] ids) {
        //TODO 偷懒
        for(Long id:ids){
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public List<Map> selectOption() {
        return brandMapper.selectOptionList();
    }
}
