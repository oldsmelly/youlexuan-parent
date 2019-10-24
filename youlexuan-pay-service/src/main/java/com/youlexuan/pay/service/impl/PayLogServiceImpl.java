package com.youlexuan.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.CONSTANT;
import com.youlexuan.pay.service.PayLogService;
import com.youlexuan.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@Service
public class PayLogServiceImpl implements PayLogService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public TbPayLog searchPayLogByUserId(String userID) {
        return (TbPayLog) redisTemplate.boundHashOps(CONSTANT.PAY_LOG_KEY).get(userID);
    }
}
