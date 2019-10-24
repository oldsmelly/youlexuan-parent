package com.youlexuan.search.listener;

import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class SolrDeleMessageListenner implements MessageListener {


    @Autowired
    private ItemSearchService searchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMsg = (ObjectMessage)message;
        try {
           Long[] goodsIds = (Long[]) objectMsg.getObject();
           searchService.deleItemListByGoodsIds(goodsIds);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
