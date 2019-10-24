package com.youlexuan.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.CONSTANT;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.StringUtils;

import java.util.*;

@Service(timeout = 100000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 1、根据关键字高亮查询
     * 2、得到分类分组
     * 3、根据选择的不同的分类信息，得到对应的品牌列表和规格列表
     * @param searchMap
     *          key=keywords
     * @return  map
     *          key=rows
     */
    @Override
    public Map search(Map searchMap) {
        Map resultMap = new HashMap();
        //1、
        Map map = searchList(searchMap);
        resultMap.putAll(map);
        //2、
        List<String> categoryList =  searchCategoryList(searchMap);
        resultMap.put("categoryList",categoryList);

        //3、
        String category = (String) searchMap.get("category");
        category =  StringUtils.isEmpty(category)? categoryList.get(0):category;
        Map brandAndSpecMap = searchBrandAndSpecList(category);
        resultMap.putAll(brandAndSpecMap);

        return resultMap;
    }



    /**
     * 3\根据分类得到对应的品牌和规格
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category) {

        Map brandAndSpecMap = new HashMap(3);
        Long typeId = (Long) redisTemplate.boundHashOps(CONSTANT.ITEMCAT_LIST_KEY).get(category);
        if(typeId!=null){
            List brandList = (List) redisTemplate.boundHashOps(CONSTANT.BRAND_LIST_KEY).get(typeId);
            brandAndSpecMap.put("brandList",brandList);

            List specList = (List) redisTemplate.boundHashOps(CONSTANT.SPEC_LIST_KEY).get(typeId);
            brandAndSpecMap.put("specList",specList);
        }

        return brandAndSpecMap;

    }

    /**
     * 根据关键字查询的结果集，再根据分类分组。得到所有的分类名
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap) {
        List<String> categoryList = new ArrayList<>();
        Query query = new SimpleQuery();
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        GroupOptions groupOpt = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOpt);
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        List<GroupEntry<TbItem>> content = groupResult.getGroupEntries().getContent();
        for(GroupEntry<TbItem> groupEntry:content){
            categoryList.add(groupEntry.getGroupValue());
        }
        return  categoryList;
    }

    /**
     * 1、高亮显示
     *   1.1 关键字高亮查询
     *   1.2 分类过滤查询   category
     *   1.3 品牌过滤查询   brand
     *   1.4 规格过滤查询   网络 机身内存等..
     *   1.5 根据价格区间过滤..
     *   1.6 加工分页条件
     *   1.7 排序
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        /*
        * 处理关键字中的空格
        * 多关键字处理
        *  solr会把多关键字分词，分词以后每个关键字进行查询，然后取并集
        */
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));


        Map map =  new HashMap();
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //1.1 加工高亮的条件
        HighlightOptions ho = new HighlightOptions().addField("item_title");
        ho.setSimplePrefix("<font style='color:red'>");
        ho.setSimplePostfix("</font>");
        query.setHighlightOptions(ho);
        //加工查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 分类过滤
        if(!"".equals(searchMap.get("category"))){
            Criteria filetCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filetCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4
        Map<String,String> specMap = (Map) searchMap.get("spec");
        for(String key:specMap.keySet()){
            Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.5
        if(!"".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");
            if(!prices[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if(!prices[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }

        //1.6 加工分页条件
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if(pageNo==null) pageNo=1; //默认第一页
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if(pageSize==null) pageSize=20; //默认每页10条

        query.setOffset((pageNo-1)*pageSize);// 计算偏移量
        query.setRows(pageSize);

        //1.7 sortValue 排序的规则， sortField 排序的域
        String sortValue = (String) searchMap.get("sortValue");
        String sortField = (String)searchMap.get("sortField");
        if(sortValue!=null&!"".equals(sortValue)){
            switch (sortValue){
                case "DESC":
                    Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                    query.addSort(sort);
                    break;
                case "ASC":
                    Sort sort1 = new Sort(Sort.Direction.ASC,"item_"+sortField);
                    query.addSort(sort1);
                    break;
                default:
                    break;
            }

        }


        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        System.out.println(page);

        //加工高亮结果
        for(HighlightEntry<TbItem> h: page.getHighlighted()){
            TbItem entity = h.getEntity();
            if(h.getHighlights().size()>0&&h.getHighlights().get(0).getSnipplets().size()>0){
                entity.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        List<TbItem> itemList = page.getContent();
        map.put("rows",itemList);                 //分页后的列表
        map.put("total",page.getTotalElements());//总记录数
        map.put("totalPage",page.getTotalPages());//总页数
        return map;
    }



    @Override
    public void importItemList(List<TbItem> itemList) {
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleItemListByGoodsIds(Long[] goodsIds) {
        SolrDataQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(Arrays.asList(goodsIds));
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
