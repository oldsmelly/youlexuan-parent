package com.youlexuan.task;

import com.youlexuan.CONSTANT;
import com.youlexuan.mapper.TbSeckillGoodsMapper;
import com.youlexuan.pojo.TbSeckillGoods;
import com.youlexuan.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Scheduled(cron = "* * * * * ?")
    public void refreshSeckillGoodsList(){

        TbSeckillGoodsExample exam = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = exam.createCriteria();
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);
        criteria.andStartTimeLessThanOrEqualTo(new Date());
        criteria.andEndTimeGreaterThanOrEqualTo(new Date());
        List<Long> idList = new ArrayList<>(redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).keys());
        criteria.andIdNotIn(idList);
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(exam);

        System.out.println("start::redis中数据个数："+idList.size());
        for(TbSeckillGoods seckillGoods:seckillGoodsList){
            redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).put(seckillGoods.getId(),seckillGoods);
        }
        System.out.println("end::redis中数据个数："+redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).keys().size());

    }

    @Scheduled(cron="* * * * * ?")
    public void removeSeckillGoodsList(){
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).values();

        /**
         * 过期的商品列表移除
         *  结束时间在当前时间之前
         */
        for(TbSeckillGoods seckillGoods:seckillGoodsList){
            if(seckillGoods.getEndTime().before(new Date())){
                redisTemplate.boundHashOps(CONSTANT.SECKILL_GOODS_LIST_KEY).delete(seckillGoods.getId());
                //持久化到数据库中
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            }
        }
    }

}
