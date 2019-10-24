package com.youlexuan.mapper;

import com.youlexuan.pojo.TbAddress;
import com.youlexuan.pojo.TbAddressExample;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface TbAddressMapper {
	Map map = new HashMap();
    int countByExample(TbAddressExample example);

    int deleteByExample(TbAddressExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TbAddress record);

    int insertSelective(TbAddress record);

    List<TbAddress> selectByExample(TbAddressExample example);

    TbAddress selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TbAddress record, @Param("example") TbAddressExample example);

    int updateByExample(@Param("record") TbAddress record, @Param("example") TbAddressExample example);

    int updateByPrimaryKeySelective(TbAddress record);

    int updateByPrimaryKey(TbAddress record);
}