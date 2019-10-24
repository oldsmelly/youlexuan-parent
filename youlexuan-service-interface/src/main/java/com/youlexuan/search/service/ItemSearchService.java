package com.youlexuan.search.service;

import com.youlexuan.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    public Map search(Map searchMap);

    void importItemList(List<TbItem> itemList);

    void deleItemListByGoodsIds(Long[] goodsIds);
}
