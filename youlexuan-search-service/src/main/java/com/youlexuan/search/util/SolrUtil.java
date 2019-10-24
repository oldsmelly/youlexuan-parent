package com.youlexuan.search.util;

import com.alibaba.fastjson.JSON;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importData(){
        TbItemExample exam = new TbItemExample();
        exam.createCriteria().andStatusEqualTo("1");//所有审核通过的sku
        List<TbItem> itemList = itemMapper.selectByExample(exam);
        for (TbItem item:itemList){
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
            System.out.println(item.getTitle());
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("从mysql中导入数据到solr索引库结束....");
    }

    public static void main(String[] args) {
        //去spring配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil  solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importData();

    }

}
