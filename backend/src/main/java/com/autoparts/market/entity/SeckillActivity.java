package com.autoparts.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "秒杀活动")
@Data
@TableName("seckill_activity")
public class SeckillActivity {

    @Schema(description = "活动ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "秒杀库存")
    private Integer stock;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private BigDecimal productPrice;

    @TableField(exist = false)
    private String productImage;
}
