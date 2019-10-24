package com.youlexuan.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.*;
import com.youlexuan.pojo.*;
import com.youlexuan.pojo.TbGoodsExample.Criteria;
import com.youlexuan.pojogroup.Goods;
import com.youlexuan.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Transactional
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 * 1、商品基本信息
	 * 1、商品描述信息
	 * 1、商品sku信息
	 */
	@Override
	public void add(Goods goods) {

		// 1、商品基本信息
		TbGoods tbGoods = goods.getGoods();
		tbGoods.setAuditStatus("0");
		goodsMapper.insert(tbGoods);

		//2、商品描述信息
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDesc.setGoodsId(tbGoods.getId());
		goodsDescMapper.insertSelective(goodsDesc);

		//3、商品的sku信息
		if("1".equals(tbGoods.getIsEnableSpec())){
			for(TbItem item:goods.getItemList()){
				//标题
				String title= goods.getGoods().getGoodsName();
				Map<String,Object> specMap = JSON.parseObject(item.getSpec());
				for(String key:specMap.keySet()){
					title+=" "+ specMap.get(key);
				}
				item.setTitle(title);
				setItemValus(goods,item);
				itemMapper.insert(item);

			}
		}else {
			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(99999);//库存数量
			item.setSpec("{}");
			setItemValus(goods,item);
			itemMapper.insert(item);

		}

	}

	private void setItemValus(Goods goods, TbItem item) {
		item.setGoodsId(goods.getGoods().getId());//商品SPU编号
		item.setSellerId(goods.getGoods().getSellerId());//商家编号
		item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//修改日期

		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());

		//商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		//图片地址（取spu的第一个图片）
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class) ;
		if(imageList.size()>0){
			item.setImage ( (String)imageList.get(0).get("url"));
		}

	}


	/**
	 * 修改
	 * 1、修改goods
	 * 2、修改goodsDesc
	 * 3、修改itemList sku
	 * 		先删后增
	 */
	@Override
	public void update(Goods goods){

		//1、
		TbGoods tbGoods = goods.getGoods();
		tbGoods.setAuditStatus("0");
		goodsMapper.updateByPrimaryKeySelective(tbGoods);

		//2\修改desc
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDescMapper.updateByPrimaryKeySelective(goodsDesc);

		//3、修改SKU
		TbItemExample exam = new TbItemExample();
		exam.createCriteria().andGoodsIdEqualTo(tbGoods.getId());
		itemMapper.deleteByExample(exam);

		for(TbItem item:goods.getItemList()){
			item.setUpdateTime(new Date());
			item.setStatus("0");
			setItemValus(goods,item);
			itemMapper.insertSelective(item);

		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return 基础的goods,goodsDesc,item
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//基础对象
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//goodsDesc
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//sku列表
		TbItemExample exam = new TbItemExample();
		 exam.createCriteria().andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(exam);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			goodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 *
	 * @param ids
	 * @param status 1通过、2驳回、3删除
	 */
	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);//spu的状态
			TbItemExample exam = new TbItemExample();
			exam.createCriteria().andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(exam);
			for(TbItem item:itemList){
				item.setStatus(status);
				itemMapper.updateByPrimaryKeySelective(item);
			}
			goodsMapper.updateByPrimaryKeySelective(tbGoods);
		}
	}

	/**
	 * 根据goodsId和状态，查出对应的所有的SKU信息
	 * @param ids
	 * @param status
	 * @return
	 */
	@Override
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] ids, String status) {

		TbItemExample exam = new TbItemExample();
		TbItemExample.Criteria criteria = exam.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(ids));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(exam);
	}

}
