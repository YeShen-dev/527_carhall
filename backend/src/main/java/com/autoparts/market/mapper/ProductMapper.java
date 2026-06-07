package com.autoparts.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.autoparts.market.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
