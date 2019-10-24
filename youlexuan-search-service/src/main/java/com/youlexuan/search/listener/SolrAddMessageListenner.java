package com.youlexuan.search.listener;

import com.alibaba.fastjson.JSON;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

public class SolrAddMessageListenner implements MessageListener {


    @Autowired
    private ItemSearchService searchService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage)message;
        try {
            String jsonStr = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(jsonStr, TbItem.class);
            for(TbItem item:itemList){
               Map spceMap =  JSON.parseObject(item.getSpec());
               item.setSpecMap(spceMap);
            }
            searchService.importItemList(itemList);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
