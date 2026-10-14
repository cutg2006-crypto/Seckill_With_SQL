package com.hzr.seckillwithsql.mapper;

import com.hzr.seckillwithsql.model.entity.OrderData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.type.MappedJdbcTypes;

/**
 * @author Administrator
 * @description 针对表【order_data(订单信息)】的数据库操作Mapper
 * @createDate 2026-10-13 21:16:44
 * @Entity generator.domain.OrderData
 */
@Mapper
public interface OrderDataMapper extends BaseMapper<OrderData> {



}
