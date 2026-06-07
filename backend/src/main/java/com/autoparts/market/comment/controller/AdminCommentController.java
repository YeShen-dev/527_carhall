package com.autoparts.market.comment.controller;

import com.autoparts.market.comment.service.CommentService;
import com.autoparts.market.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理后台-评论管理", description = "审核/删除/列表")
@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    private final CommentService commentService;

    @Operation(summary = "评论审核")
    @PostMapping("/review")
    public ApiResponse<Void> review(@RequestBody Map<String,Integer> body) {
        commentService.review(body.get("id").longValue(), body.get("status"));
        return ApiResponse.success("审核完成", null);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id, null, true);
        return ApiResponse.success("已删除", null);
    }

    @Operation(summary = "评论列表")
    @GetMapping("/list")
    public ApiResponse<Map<String,Object>> list(
            @RequestParam(required=false) Integer status,
            @RequestParam(required=false) String keyword,
            @RequestParam(defaultValue="0") Integer page,
            @RequestParam(defaultValue="10") Integer size) {
        return ApiResponse.success(commentService.adminList(status, keyword, page, size));
    }
}
