package com.youlexuan.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.youlexuan.CONSTANT;
import com.youlexuan.entity.PageResult;
import com.youlexuan.mapper.TbOrderItemMapper;
import com.youlexuan.mapper.TbOrderMapper;
import com.youlexuan.mapper.TbPayLogMapper;
import com.youlexuan.order.service.OrderService;
import com.youlexuan.pojo.TbOrder;
import com.youlexuan.pojo.TbOrderExample;
import com.youlexuan.pojo.TbOrderExample.Criteria;
import com.youlexuan.pojo.TbOrderItem;
import com.youlexuan.pojo.TbPayLog;
import com.youlexuan.pojogroup.Cart;
import com.youlexuan.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	/**
	 * 支付日志：
	 * 		当订单创建成功，那么入库一条支付日志，并将日志缓存
	 */
	@Autowired
	private TbPayLogMapper payLogMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 * 将cartList中的信息转成order相关信息
	 * 1、cart基本信息入库tb_order表
	 * 2、cart中的购物项信息入到tb_order-item表中
	 *
	 * 部分数据是从前台获取到的，eg:付款方式、收货人地址信息等
	 */
	@Override
	public void add(TbOrder order) {
		//1、获取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CONSTANT.CART_LIST_REDIS_KEY).get(order.getUserId());
		//2、从cartList中取值入库
		List orderIdList = new ArrayList();
		double totalFee = 0.0;
		for(Cart cart:cartList){
			long orderId = idWorker.nextId();
			System.out.println("sellerId:"+cart.getSellerId());
			TbOrder tborder=new TbOrder();//新创建订单对象
			tborder.setOrderId(orderId);//订单ID
			tborder.setUserId(order.getUserId());//用户名
			tborder.setPaymentType(order.getPaymentType());//支付类型
			tborder.setStatus("1");//状态：未付款
			tborder.setCreateTime(order.getCreateTime());//订单创建日期
			tborder.setUpdateTime(order.getUpdateTime());//订单更新日期
			tborder.setReceiverAreaName(order.getReceiverAreaName());//地址
			tborder.setReceiverMobile(order.getReceiverMobile());//手机号
			tborder.setReceiver(order.getReceiver());//收货人
			tborder.setSellerId(cart.getSellerId());//商家ID
			//循环购物车明细
			double money=0;
			for(TbOrderItem orderItem :cart.getOrderItemList()){
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId( orderId  );//订单ID
				orderItem.setSellerId(cart.getSellerId());
				money+=orderItem.getTotalFee().doubleValue();//金额累加
				orderItemMapper.insert(orderItem);
			}
			tborder.setPayment(new BigDecimal(money));

			orderMapper.insert(tborder);
			totalFee  += money;
			orderIdList.add(orderId);

		}

		/**
		 * 生成支付日志相关信息
		 */
		if("1".equals(order.getPaymentType())){
			TbPayLog payLog = new TbPayLog();
			payLog.setTradeState("0");
			payLog.setOrderList(orderIdList.toString().replace("[","").replace("]",""));
			payLog.setTotalFee((long)totalFee*100);
			payLog.setCreateTime(new Date());
			payLog.setOutTradeNo(idWorker.nextId()+"");//要求交易号必须唯一
			payLog.setPayType("1");
			payLog.setUserId(order.getUserId());
			payLogMapper.insert(payLog);
			redisTemplate.boundHashOps(CONSTANT.PAY_LOG_KEY).put(order.getUserId(),payLog);
		}
		//3、将购物清空
		redisTemplate.boundHashOps(CONSTANT.CART_LIST_REDIS_KEY).delete(order.getUserId());
	}



	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long orderId){
		return orderMapper.selectByPrimaryKey(orderId);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] orderIds) {
		for(Long orderId:orderIds){
			orderMapper.deleteByPrimaryKey(orderId);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 1、修改订单状态
	 * 2、补全支付日志数据，修改支付日志
	 * 3、清空支付日志的缓存
	 * @param out_trade_no
	 * @param transaction_id
	 */
	@Override
	public void updateOrderStauts(String out_trade_no, String transaction_id) {

		//2、修改日志
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1");//支付状态
		payLog.setTransactionId(transaction_id);
		payLogMapper.updateByPrimaryKeySelective(payLog);

		//1\修改订单
		String orderList = payLog.getOrderList();
		for(String orderIdStr:orderList.split(",")){
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderIdStr));
			order.setStatus("2");
			order.setUpdateTime(new Date());
			orderMapper.updateByPrimaryKey(order);
		}

		//3\清空支付日志的缓存
		redisTemplate.boundHashOps(CONSTANT.PAY_LOG_KEY).delete(payLog.getUserId());

	}

}
