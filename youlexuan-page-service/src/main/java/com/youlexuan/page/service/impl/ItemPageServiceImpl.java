package com.youlexuan.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.youlexuan.mapper.TbGoodsDescMapper;
import com.youlexuan.mapper.TbGoodsMapper;
import com.youlexuan.mapper.TbItemCatMapper;
import com.youlexuan.mapper.TbItemMapper;
import com.youlexuan.page.service.ItemPageService;
import com.youlexuan.pojo.TbGoods;
import com.youlexuan.pojo.TbGoodsDesc;
import com.youlexuan.pojo.TbItem;
import com.youlexuan.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 给定一个goodsID，生成goodsID对应的静态html
 *  生成的html页面通过page-web工程显示，因此生成的路径是page-web工程的webapps下
 *
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {


    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;


    @Autowired
    private TbItemMapper itemMapper;


    /**
     * 第一步：创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是 freemarker的版本号。
     * 第二步：设置模板文件所在的路径。
     * 第三步：设置模板文件使用的字符集。一般就是 utf-8.
     * 第四步：加载一个模板，创建一个模板对象。
     * 第五步：创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
     * 第六步：创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。
     * 第七步：调用模板对象的 process 方法输出文件。
     * 第八步：关闭流
     * @param goodsId
     * @return
     */
    @Override
    public boolean genItemHtml(Long goodsId) {
        Writer writer = null;
        try {
            //1、
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //2\
            Template template = configuration.getTemplate("item.ftl");
            //5\
            Map dataModel = new HashMap<>();
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",tbGoods);
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",tbGoodsDesc);
            //3.商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);

            //sku数据以后再加工
            TbItemExample exam = new TbItemExample();
            exam.createCriteria().andGoodsIdEqualTo(goodsId);
            List<TbItem> itemList = itemMapper.selectByExample(exam);
            dataModel.put("itemList",itemList);

            //6
            writer = new FileWriter(pagedir + goodsId + ".html");
            //7
            template.process(dataModel,writer);

            return true;


        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        } finally {

            try {
               if (writer!=null)  writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
