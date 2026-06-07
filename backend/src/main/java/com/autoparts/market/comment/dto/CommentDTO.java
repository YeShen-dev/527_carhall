package com.autoparts.market.comment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CommentDTO {
    @NotNull private Long productId;
    @NotNull private Long orderId;
    @NotBlank @Size(min=10,max=500) private String content;
    @Min(1) @Max(5) private Integer rating;
    private String images;     // JSON array string
    private Integer isAnonymous;
    private Integer isAppend;  // 0=首次 1=追评
}
