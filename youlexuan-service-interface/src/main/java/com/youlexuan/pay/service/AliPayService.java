package com.youlexuan.pay.service;

import java.util.Map;

public interface AliPayService {

    /**
     * 生成支付宝支付二维码
     * @param out_trade_no 订单号
     * @param total_amount 金额
     * @return
     */
    public Map createNative(String out_trade_no, String total_amount);

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    public Map closePay(String out_trade_no);
}
