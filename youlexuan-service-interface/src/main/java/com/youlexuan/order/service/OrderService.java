package com.youlexuan.order.service;
import java.util.List;
import com.youlexuan.pojo.TbOrder;

import com.youlexuan.entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface OrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbOrder order);
	
	
	/**
	 * 修改
	 */
	public void update(TbOrder order);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbOrder findOne(Long orderId);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] orderIds);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbOrder order, int pageNum, int pageSize);

	/**
	 * 当支付成功以后
	 * 1、修改订单状态
	 * 2、修改支付日志的状态，并将支付日志的redis清空
	 */
	public  void updateOrderStauts(String out_trade_no,String transaction_id);
	
}
