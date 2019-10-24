package com.youlexuan.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.youlexuan.entity.PageResult;
import com.youlexuan.entity.Result;
import com.youlexuan.page.service.ItemPageService;
import com.youlexuan.pojo.TbGoods;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojogroup.Goods;
import com.youlexuan.search.service.ItemSearchService;
import com.youlexuan.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Reference
	private ItemSearchService searchService;

	@Reference
	private ItemPageService pageService;

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination queueSolrAddDestination;
	@Autowired
	private Destination queueSolrDeleDestination;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			TbGoods tbGoods = goods.getGoods();
			tbGoods.setAuditStatus("0");
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			tbGoods.setSellerId(sellerId);
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
//		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
//		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);		
	}

	/**
	 * 审核管理
	 * 		修改数据库 状态改为通过状态
	 * 		审核通过的商品需要在solr中能够搜索到。 数据应该同步到solr的索引库中
	 * 	1、同步solr索引库
	 * 	    1.1、查到审核通过的所有的商品
	 * 	    1.2将这些sku保存到solr索引库中
	 * 	 2、生成静态化的html页面
	 *
	 *
	 * 删除商品
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids,String status){
		try {
			goodsService.updateStatus(ids,status);
						if("1".equals(status)){ // 审核通过
					//1.1、
					List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids,status);
					//1.2、
//					searchService.importItemList(itemList);
					if(itemList!=null&&itemList.size()>0){
						String jsonStr = JSON.toJSONString(itemList);
						jmsTemplate.send(queueSolrAddDestination, new MessageCreator() {
							@Override
							public Message createMessage(Session session) throws JMSException {
								return session.createTextMessage(jsonStr);
							}
						});
					}

					//2
					for(Long goodsId:ids){
						pageService.genItemHtml(goodsId);
					}


				}
			if("3".equals(status)){ //删除商品
				// 根据goodsId 从索引库中删除数据
//				searchService.deleItemListByGoodsIds(ids);
				jmsTemplate.send(queueSolrDeleDestination, new MessageCreator() {
					@Override
					public Message createMessage(Session session) throws JMSException {
						return session.createObjectMessage(ids);
					}
				});
			}
			return new Result(true,"审核成功");
		}catch (Exception e){
			e.printStackTrace();
			return new Result(false,e.toString());
		}

	}

	/**
	 * 测试接口，传递一个goodsID，生成一个html页面
	 */
//	@RequestMapping("/genItemHtml")
//	public boolean genItemHtml(Long goodsId) {
//		return pageService.genItemHtml(goodsId);
//	}
}
