package com.youlexuan.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.youlexuan.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Service(timeout = 50000000)
public class AlipayServiceImpl implements AliPayService {


    @Autowired
    private  AlipayClient alipayClient;

    /**
     * 调用支付宝的预下单接口，返回支付的连接，根据连接生成二维码
     * @param out_trade_no 订单号
     * @param total_amount 金额
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_amount) {
        Map map = new HashMap();
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setBizContent("{" +
                "\"out_trade_no\":"+out_trade_no+"," +
                "\"total_amount\":"+total_amount+"," +
                "\"subject\":\"商品测试购买标题001\"," +
                "\"qr_code_timeout_express\":\"90m\"" +
                "  }");
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
            //从相应对象读取相应结果
            String code = response.getCode();
            System.out.println("响应码:"+code);
            //全部的响应结果
            String body = response.getBody();
            System.out.println("返回结果:"+body);

            if(code.equals("10000")){
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_amount",total_amount);
                System.out.println("qrcode:"+response.getQrCode());
                System.out.println("out_trade_no:"+response.getOutTradeNo());
                System.out.println("total_amount:"+total_amount);
            }else{
                System.out.println("预下单接口调用失败:"+body);
            }



        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String,String> map=new HashMap<String, String>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{" +
                "    \"out_trade_no\":\""+out_trade_no+"\"," +
                "    \"trade_no\":\"\"}"); //设置业务参数

        //发出请求
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            String code=response.getCode();
            System.out.println("返回值1:"+response.getBody());
            if(code.equals("10000")){
                //System.out.println("返回值2:"+response.getBody());
                map.put("out_trade_no", out_trade_no);
                map.put("trade_no", response.getTradeNo());
                map.put("tradestatus", response.getTradeStatus());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;


    }

    @Override
    public Map closePay(String out_trade_no) {
        Map<String,String> map=new HashMap<String, String>();
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizContent("{" +
                "\"trade_no\":"+out_trade_no+"," +
                "  }");
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
            String code=response.getCode();

            if(code.equals("10000")){

                System.out.println("返回值:"+response.getBody());
                map.put("code", code);
                map.put("out_trade_no", out_trade_no);
                return map;
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return map;
    }
}
