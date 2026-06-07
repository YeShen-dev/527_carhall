package com.autoparts.market.comment.mapper;

import com.autoparts.market.comment.entity.ProductComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductCommentMapper extends BaseMapper<ProductComment> {

    @Select("SELECT COUNT(*) as total, AVG(rating) as avg, " +
            "SUM(CASE WHEN rating >=4 THEN 1 ELSE 0 END) as good, " +
            "SUM(CASE WHEN rating =3 THEN 1 ELSE 0 END) as middle, " +
            "SUM(CASE WHEN rating <=2 THEN 1 ELSE 0 END) as bad " +
            "FROM product_comment WHERE product_id=#{productId} AND status=1")
    Map<String,Object> statistics(@Param("productId") Long productId);

    @Select("SELECT DISTINCT product_id FROM order_item oi " +
            "JOIN order_master om ON oi.order_id=om.id " +
            "WHERE om.user_id=#{userId} AND om.status='COMPLETED'")
    List<Long> findPurchasedProductIds(@Param("userId") Long userId);
}
