package com.youlexuan.pay.service;

import com.youlexuan.pojo.TbPayLog;

public interface PayLogService {

    /**
     * 根据userId查找支付日志a
     *    作用一：生成二维码时从支付日志中得到支付的ID以及支付的金额
     */
    public TbPayLog searchPayLogByUserId(String userID);
}
