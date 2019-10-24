package com.youlexuan.pojogroup;

import com.youlexuan.pojo.TbGoods;
import com.youlexuan.pojo.TbGoodsDesc;
import com.youlexuan.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable {

    //商品基本信息
    private TbGoods goods;
    //商品desc信息
    private TbGoodsDesc goodsDesc;
    //商品sku信息
    private List<TbItem> itemList;

    public Goods() {
    }

    public Goods(TbGoods goods, TbGoodsDesc goodsDesc, List<TbItem> itemList) {
        this.goods = goods;
        this.goodsDesc = goodsDesc;
        this.itemList = itemList;
    }

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }
}
