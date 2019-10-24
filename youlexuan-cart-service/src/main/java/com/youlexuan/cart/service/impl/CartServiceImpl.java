package com.youlexuan.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.CONSTANT;
import com.youlexuan.cart.service.CartService;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbOrderItem;
import com.youlexuan.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *

     *  有购物车列表
     *       商品对应的商家的购物车没有，购物车里的购物项也没有
     *           新建购物车pojo
     *           新建购物项的poj ,将购物项set到购物车中
     *           将购物车放到购物车列表中
     *       商品对应的商家的购物车也有了
     *          购物项没有
     *              新增购物项，购物项set到购物车中
     *           购物项有
     *              修改购物项，重新set
     *
     *
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {


        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        String sellerId = item.getSellerId();
        Cart cart = searchCartBySellerId(cartList,sellerId);

        if(cart == null){ //没有该商家的购物车
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(item,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //将cart放入到cartList
            cartList.add(cart);

        }else{ //购物车列表中有 商家有对应的购物车
            TbOrderItem orderItem = searOrderItemByItemId(cart.getOrderItemList(),itemId);
            if(orderItem==null){ //购物车中没有商品对应的购物项
                orderItem = createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else{//购物车中有商品对应的购物项
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));

                //当num变为了0，说明是购物项删除了
                if(orderItem.getNum()==0){
                    cart.getOrderItemList().remove(orderItem);
                }
                if(cart.getOrderItemList().size()<=0){
                    cartList.remove(cart);
                }
            }


        }

        return cartList;
    }



    /**
     * 根据skuID得到orderItem
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for(TbOrderItem orderItem:orderItemList){
            if(orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if(num<=0){
            throw new RuntimeException("数量非法");
        }

        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;

    }

    /**
     * 根据sellerID查找对应的cart
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for(Cart cart :cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return  null;
    }

    /**
     * 如果用户登录了，那么将购物车中的数据存入到Redis里，取的时候从redis中取
     *
     * @param name
     * @return
     */

    @Override
    public List<Cart> findCartListFromRedis(String name) {
        System.out.println("从redis中获取购物车List....");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CONSTANT.CART_LIST_REDIS_KEY).get(name);
        if(cartList == null){
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String name, List<Cart> cartList) {
        redisTemplate.boundHashOps(CONSTANT.CART_LIST_REDIS_KEY).put(name,cartList);
        System.out.println("将购物车列表放入到redis中");
    }


    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2){
        for(Cart cart:cartList2){
            for (TbOrderItem orderItem:cart.getOrderItemList()){
                cartList1 = addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return  cartList1;
    }
}
