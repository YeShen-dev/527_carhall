package com.autoparts.market.comment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_comment")
public class ProductComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long orderId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private Integer rating;
    private String images;
    private Integer likeCount;
    private Integer replyCount;
    private Integer isAnonymous;
    private Integer isAppend;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
