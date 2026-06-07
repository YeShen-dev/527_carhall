package com.autoparts.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "商品")
@Data
@TableName("product")
public class Product {

    @Schema(description = "商品ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品分类（发动机/刹车系统/滤清器等）")
    private String category;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "商品图片URL")
    private String imageUrl;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "规格参数")
    private String spec;

    @Schema(description = "制造商")
    private String manufacturer;

    @Schema(description = "状态：ON(上架) / OFF(下架)")
    private String status;

    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
