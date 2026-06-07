package com.autoparts.market.comment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_comment_reply")
public class ProductCommentReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long commentId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private Long parentId;
    private Integer isMerchant;
    private LocalDateTime createTime;
}
