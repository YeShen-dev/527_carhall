package com.autoparts.market.comment.controller;

import com.autoparts.market.comment.dto.CommentDTO;
import com.autoparts.market.comment.dto.CommentVO;
import com.autoparts.market.comment.service.CommentService;
import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.entity.User;
import com.autoparts.market.mapper.UserMapper;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "商品评论", description = "评论发表/查询/点赞/回复")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserMapper userMapper;

    @Operation(summary = "发表评论/追评")
    @PostMapping("/add")
    public ApiResponse<CommentVO> add(@Valid @RequestBody CommentDTO dto) {
        Long uid = SecurityUtils.getCurrentUserId();
        User u = userMapper.selectById(uid);
        return ApiResponse.success(commentService.addComment(uid, u.getUsername(), u.getAvatar(), dto));
    }

    @Operation(summary = "商品评论列表（分页+筛选）")
    @GetMapping("/product/{productId}")
    public ApiResponse<Map<String,Object>> list(@PathVariable Long productId,
            @RequestParam(required=false) Integer rating,
            @RequestParam(defaultValue="0") Integer page,
            @RequestParam(defaultValue="10") Integer size) {
        Long uid = SecurityUtils.getCurrentUserIdOrNull();
        return ApiResponse.success(commentService.productComments(productId, rating, page, size, uid));
    }

    @Operation(summary = "评论详情")
    @GetMapping("/{id}")
    public ApiResponse<CommentVO> detail(@PathVariable Long id) {
        Long uid = SecurityUtils.getCurrentUserIdOrNull();
        return ApiResponse.success(commentService.getDetail(id, uid));
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{id}/like")
    public ApiResponse<Map<String,Object>> like(@PathVariable Long id) {
        return ApiResponse.success(commentService.toggleLike(id, SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply")
    public ApiResponse<CommentVO.ReplyVO> reply(@RequestBody Map<String,String> body) {
        Long uid = SecurityUtils.getCurrentUserId();
        User u = userMapper.selectById(uid);
        return ApiResponse.success(commentService.addReply(
                Long.valueOf(body.get("commentId")), uid, u.getUsername(), u.getAvatar(),
                body.get("content"), body.containsKey("parentId")?Long.valueOf(body.get("parentId")):null, false));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id, SecurityUtils.getCurrentUserId(), false);
        return ApiResponse.success("已删除", null);
    }

    @Operation(summary = "评论统计")
    @GetMapping("/statistics/{productId}")
    public ApiResponse<Map<String,Object>> statistics(@PathVariable Long productId) {
        return ApiResponse.success(commentService.statistics(productId));
    }

    @Operation(summary = "我的评论")
    @GetMapping("/my")
    public ApiResponse<Map<String,Object>> my(@RequestParam(required=false) Integer type,
            @RequestParam(defaultValue="0") Integer page, @RequestParam(defaultValue="10") Integer size) {
        return ApiResponse.success(commentService.myComments(SecurityUtils.getCurrentUserId(), type, page, size));
    }

    @Operation(summary = "待评论商品")
    @GetMapping("/pending")
    public ApiResponse<?> pending() {
        return ApiResponse.success(commentService.pendingComments(SecurityUtils.getCurrentUserId()));
    }
}
