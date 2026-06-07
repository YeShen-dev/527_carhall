package com.autoparts.market.comment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentVO {
    private Long id;
    private Long productId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private Integer rating;
    private List<String> images;
    private Integer likeCount;
    private Integer replyCount;
    private Integer isAnonymous;
    private Integer isAppend;
    private Integer status;
    private Boolean liked;
    private List<ReplyVO> replies;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Data @Builder
    public static class ReplyVO {
        private Long id;
        private Long userId;
        private String username;
        private String avatar;
        private String content;
        private Long parentId;
        private Integer isMerchant;
        private LocalDateTime createTime;
    }
}
