package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.CONSTANT;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.TbSpecificationOptionMapper;
import com.youlexuan.mapper.TbTypeTemplateMapper;
import com.youlexuan.pojo.TbSpecificationOption;
import com.youlexuan.pojo.TbSpecificationOptionExample;
import com.youlexuan.pojo.TbTypeTemplate;
import com.youlexuan.pojo.TbTypeTemplateExample;
import com.youlexuan.pojo.TbTypeTemplateExample.Criteria;
import com.youlexuan.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper spcOptMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);

		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);
			List<TbTypeTemplate> typeTemplateList = findAll();
			for(TbTypeTemplate tt:typeTemplateList){
				List<Map> brandList = JSON.parseArray(tt.getBrandIds(),Map.class);
				redisTemplate.boundHashOps(CONSTANT.BRAND_LIST_KEY).put(tt.getId(),brandList);

				List<Map> specList = findSpecList(tt.getId());
				redisTemplate.boundHashOps(CONSTANT.SPEC_LIST_KEY).put(tt.getId(),specList);

			}
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 *
	 * @param id 模板ID
	 * @return
	 *
	 * 根据模板ID得到模板对象  模板对象中的 specList [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
	 * 期望的返回类型是                              [{"id":27,"text":"网络",options:['3G','4G','5G']},{"id":32,"text":"机身内存"}]
	 */
	@Override
	public List<Map> findSpecList(Long id) {

		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		List<Map> mapList = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
		for(Map map:mapList){//{"id":27,"text":"网络"}
			Long specId  = new Long((Integer) map.get("id"));
			TbSpecificationOptionExample exam = new TbSpecificationOptionExample();
			exam.createCriteria().andSpecIdEqualTo(specId);
			List<TbSpecificationOption> specOptList = spcOptMapper.selectByExample(exam);//['3G','4G','5G']
			map.put("options",specOptList);
		}
		return mapList;
	}
	
}
