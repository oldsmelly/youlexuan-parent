package com.youlexuan.seckill.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.CONSTANT;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.TbSeckillGoodsMapper;
import com.youlexuan.pojo.TbSeckillGoods;
import com.youlexuan.pojo.TbSeckillGoodsExample;
import com.youlexuan.pojo.TbSeckillGoodsExample.Criteria;
import com.youlexuan.seckill.service.SeckillGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillGoods> findAll() {
		return seckillGoodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillGoods> page=   (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillGoods seckillGoods) {
		seckillGoodsMapper.insert(seckillGoods);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillGoods seckillGoods){
		seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillGoods findOne(Long id){
		return seckillGoodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillGoodsMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillGoodsExample example=new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillGoods!=null){			
						if(seckillGoods.getTitle()!=null && seckillGoods.getTitle().length()>0){
				criteria.andTitleLike("%"+seckillGoods.getTitle()+"%");
			}			if(seckillGoods.getSmallPic()!=null && seckillGoods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+seckillGoods.getSmallPic()+"%");
			}			if(seckillGoods.getSellerId()!=null && seckillGoods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillGoods.getSellerId()+"%");
			}			if(seckillGoods.getStatus()!=null && seckillGoods.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillGoods.getStatus()+"%");
			}			if(seckillGoods.getIntroduction()!=null && seckillGoods.getIntroduction().length()>0){
				criteria.andIntroductionLike("%"+seckillGoods.getIntroduction()+"%");
			}	
		}
		
		Page<TbSeckillGoods> page= (Page<TbSeckillGoods>)seckillGoodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 审核通过了
	 * 当前时间介于秒杀时间的区间内
	 * 库存大于0
	 * @return
	 *
	 * d点击列表中商品链接，需要调整到详情页上
	 * seckillGoodsId为参数，能够定位到商品，因此我们hash  key seckillGoodsID-----seckillGoods
	 */
	@Override
	public List<TbSeckillGoods> findList() {

		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).values();
		if(seckillGoodsList==null||seckillGoodsList.size()==0){
			TbSeckillGoodsExample exam = new TbSeckillGoodsExample();
			Criteria criteria = exam.createCriteria();
			criteria.andStatusEqualTo("1");//通过审核
			//当前时间介于开始时间和结束时间之间
			criteria.andStartTimeLessThanOrEqualTo(new Date());
			criteria.andEndTimeGreaterThanOrEqualTo(new Date());
			//当前库存》0
			criteria.andStockCountGreaterThan(0);
			seckillGoodsList = seckillGoodsMapper.selectByExample(exam);
			for(TbSeckillGoods seckillGoods:seckillGoodsList){
				redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).put(seckillGoods.getId(),seckillGoods);
			}
		}
		return seckillGoodsList;

	}

	@Override
	public TbSeckillGoods findOneFrmRedis(Long id) {
		return (TbSeckillGoods) redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).get(id);
	}

}
