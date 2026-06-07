package com.autoparts.market.comment.service;

import com.autoparts.market.comment.dto.CommentDTO;
import com.autoparts.market.comment.dto.CommentVO;
import com.autoparts.market.comment.entity.*;
import com.autoparts.market.comment.mapper.ProductCommentMapper;
import com.autoparts.market.entity.*;
import com.autoparts.market.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final ProductCommentMapper commentMapper;
    private final com.autoparts.market.comment.mapper.ProductCommentReplyMapper replyMapper;
    private final com.autoparts.market.comment.mapper.ProductCommentLikeMapper likeMapper;
    private final OrderMasterMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 发表评论 / 追评 */
    @Transactional
    public CommentVO addComment(Long userId, String username, String avatar, CommentDTO dto) {
        // 校验订单已完成
        OrderMaster order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !order.getUserId().equals(userId))
            throw new RuntimeException("订单不存在");
        if (!"COMPLETED".equals(order.getStatus()))
            throw new RuntimeException("订单未完成，无法评论");

        // 追评需已有首次评论
        if (dto.getIsAppend() != null && dto.getIsAppend() == 1) {
            long cnt = commentMapper.selectCount(new LambdaQueryWrapper<ProductComment>()
                    .eq(ProductComment::getOrderId, dto.getOrderId())
                    .eq(ProductComment::getProductId, dto.getProductId())
                    .eq(ProductComment::getUserId, userId)
                    .eq(ProductComment::getIsAppend, 0));
            if (cnt == 0) throw new RuntimeException("请先发表首次评论再来追评");
        } else {
            // 首次评论不可重复
            long cnt = commentMapper.selectCount(new LambdaQueryWrapper<ProductComment>()
                    .eq(ProductComment::getOrderId, dto.getOrderId())
                    .eq(ProductComment::getProductId, dto.getProductId())
                    .eq(ProductComment::getUserId, userId)
                    .eq(ProductComment::getIsAppend, 0));
            if (cnt > 0) throw new RuntimeException("您已评论过该订单商品，可前往追评");
        }

        ProductComment comment = new ProductComment();
        comment.setProductId(dto.getProductId());
        comment.setOrderId(dto.getOrderId());
        comment.setUserId(userId);
        comment.setUsername(dto.getIsAnonymous() != null && dto.getIsAnonymous() == 1 ? "匿名用户" : username);
        comment.setAvatar(avatar);
        comment.setContent(dto.getContent());
        comment.setRating(dto.getRating());
        comment.setImages(dto.getImages());
        comment.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : 0);
        comment.setIsAppend(dto.getIsAppend() != null ? dto.getIsAppend() : 0);
        comment.setStatus(0); // 待审核
        commentMapper.insert(comment);
        return toVO(comment, null);
    }

    /** 查询商品评论（已通过，支持分页+筛选） */
    public Map<String,Object> productComments(Long productId, Integer rating, Integer page, Integer size, Long userId) {
        LambdaQueryWrapper<ProductComment> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductComment::getProductId, productId)
          .eq(ProductComment::getStatus, 1);
        if (rating != null && rating > 0) qw.eq(ProductComment::getRating, rating);
        qw.orderByDesc(ProductComment::getCreateTime);

        Page<ProductComment> mpPage = commentMapper.selectPage(new Page<>(page+1, size), qw);
        List<CommentVO> vos = mpPage.getRecords().stream()
                .map(c -> toVO(c, userId))
                .collect(Collectors.toList());

        Map<String,Object> result = new HashMap<>();
        result.put("list", vos);
        result.put("total", mpPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /** 评论详情 */
    public CommentVO getDetail(Long id, Long userId) {
        ProductComment c = commentMapper.selectById(id);
        if (c == null) throw new RuntimeException("评论不存在");
        return toVO(c, userId);
    }

    /** 点赞/取消点赞 (toggle) */
    @Transactional
    public Map<String,Object> toggleLike(Long commentId, Long userId) {
        ProductCommentLike exist = likeMapper.selectOne(new LambdaQueryWrapper<ProductCommentLike>()
                .eq(ProductCommentLike::getCommentId, commentId)
                .eq(ProductCommentLike::getUserId, userId));

        if (exist != null) {
            likeMapper.deleteById(exist.getId());
            commentMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ProductComment>()
                    .eq(ProductComment::getId, commentId)
                    .setSql("like_count = like_count - 1"));
            return Map.of("liked", false);
        }
        ProductCommentLike like = new ProductCommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        likeMapper.insert(like);
        commentMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ProductComment>()
                .eq(ProductComment::getId, commentId)
                .setSql("like_count = like_count + 1"));
        return Map.of("liked", true);
    }

    /** 回复评论 */
    @Transactional
    public CommentVO.ReplyVO addReply(Long commentId, Long userId, String username, String avatar, String content, Long parentId, boolean isMerchant) {
        ProductCommentReply reply = new ProductCommentReply();
        reply.setCommentId(commentId);
        reply.setUserId(userId);
        reply.setUsername(username);
        reply.setAvatar(avatar);
        reply.setContent(content);
        reply.setParentId(parentId != null ? parentId : 0L);
        reply.setIsMerchant(isMerchant ? 1 : 0);
        replyMapper.insert(reply);
        commentMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ProductComment>()
                .eq(ProductComment::getId, commentId)
                .setSql("reply_count = reply_count + 1"));
        return CommentVO.ReplyVO.builder()
                .id(reply.getId()).userId(userId).username(username)
                .avatar(avatar).content(content).parentId(reply.getParentId())
                .isMerchant(isMerchant ? 1 : 0).createTime(reply.getCreateTime()).build();
    }

    /** 删除评论（逻辑删除，仅作者可删） */
    @Transactional
    public void deleteComment(Long id, Long userId, boolean isAdmin) {
        ProductComment c = commentMapper.selectById(id);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!isAdmin && !c.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        c.setStatus(2);
        commentMapper.updateById(c);
    }

    /** 审核评论 */
    public void review(Long id, Integer status) {
        ProductComment c = commentMapper.selectById(id);
        if (c == null) throw new RuntimeException("评论不存在");
        c.setStatus(status);
        commentMapper.updateById(c);
    }

    /** 评论统计 */
    public Map<String,Object> statistics(Long productId) {
        Map<String,Object> stats = commentMapper.statistics(productId);
        if (stats == null || stats.get("total") == null) {
            return Map.of("totalCount",0,"goodCount",0,"middleCount",0,"badCount",0,"goodRate","0%","avgRating",0.0);
        }
        long total = ((Number)stats.getOrDefault("total",0)).longValue();
        long good = ((Number)stats.getOrDefault("good",0)).longValue();
        long middle = ((Number)stats.getOrDefault("middle",0)).longValue();
        long bad = ((Number)stats.getOrDefault("bad",0)).longValue();
        double avg = stats.get("avg") != null ? ((Number)stats.get("avg")).doubleValue() : 0;
        return Map.of("totalCount",total,"goodCount",good,"middleCount",middle,"badCount",bad,
                "goodRate",total>0?Math.round(good*100.0/total)+"%":"0%","avgRating",Math.round(avg*10)/10.0);
    }

    /** 用户评论列表 */
    public Map<String,Object> myComments(Long userId, Integer type, Integer page, Integer size) {
        LambdaQueryWrapper<ProductComment> qw = new LambdaQueryWrapper<>();
        qw.eq(ProductComment::getUserId, userId);
        if (type != null && type == 1) qw.eq(ProductComment::getIsAppend, 1); // 追评
        else if (type != null && type == 0) qw.eq(ProductComment::getIsAppend, 0); // 首次
        qw.orderByDesc(ProductComment::getCreateTime);

        Page<ProductComment> mpPage = commentMapper.selectPage(new Page<>(page+1, size), qw);
        List<CommentVO> vos = mpPage.getRecords().stream().map(c -> toVO(c, userId)).collect(Collectors.toList());
        return Map.of("list",vos,"total",mpPage.getTotal());
    }

    /** 查询待评论商品（已购买已完成但未评论） */
    public List<Map<String,Object>> pendingComments(Long userId) {
        List<Long> productIds = commentMapper.findPurchasedProductIds(userId);
        if (productIds.isEmpty()) return List.of();
        List<OrderMaster> orders = orderMapper.selectList(new LambdaQueryWrapper<OrderMaster>()
                .eq(OrderMaster::getUserId, userId).eq(OrderMaster::getStatus, "COMPLETED")
                .orderByDesc(OrderMaster::getId));
        List<Map<String,Object>> result = new ArrayList<>();
        for (OrderMaster o : orders) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            for (OrderItem item : items) {
                long cnt = commentMapper.selectCount(new LambdaQueryWrapper<ProductComment>()
                        .eq(ProductComment::getOrderId, o.getId())
                        .eq(ProductComment::getProductId, item.getProductId())
                        .eq(ProductComment::getUserId, userId)
                        .eq(ProductComment::getIsAppend, 0));
                if (cnt == 0) {
                    result.add(Map.of("orderId",o.getId(),"productId",item.getProductId(),
                            "productName",item.getProductName(),"productImage",item.getProductImage()!=null?item.getProductImage():""));
                }
            }
        }
        return result;
    }

    /** 管理端分页列表 */
    public Map<String,Object> adminList(Integer status, String keyword, Integer page, Integer size) {
        LambdaQueryWrapper<ProductComment> qw = new LambdaQueryWrapper<>();
        if (status != null) qw.eq(ProductComment::getStatus, status);
        if (keyword != null && !keyword.isEmpty())
            qw.like(ProductComment::getUsername, keyword);
        qw.orderByDesc(ProductComment::getCreateTime);
        Page<ProductComment> mpPage = commentMapper.selectPage(new Page<>(page+1, size), qw);
        return Map.of("list", mpPage.getRecords(), "total", mpPage.getTotal());
    }

    // ---- 转换 ----
    private CommentVO toVO(ProductComment c, Long currentUserId) {
        List<String> imageList = parseImages(c.getImages());
        List<ProductCommentReply> replies = replyMapper.selectList(
                new LambdaQueryWrapper<ProductCommentReply>()
                        .eq(ProductCommentReply::getCommentId, c.getId())
                        .orderByAsc(ProductCommentReply::getCreateTime));
        List<CommentVO.ReplyVO> replyVOS = replies.stream().map(r -> CommentVO.ReplyVO.builder()
                .id(r.getId()).userId(r.getUserId()).username(r.getUsername())
                .avatar(r.getAvatar()).content(r.getContent()).parentId(r.getParentId())
                .isMerchant(r.getIsMerchant()).createTime(r.getCreateTime()).build()).collect(Collectors.toList());

        boolean liked = currentUserId != null && likeMapper.selectCount(
                new LambdaQueryWrapper<ProductCommentLike>()
                        .eq(ProductCommentLike::getCommentId, c.getId())
                        .eq(ProductCommentLike::getUserId, currentUserId)) > 0;

        return CommentVO.builder()
                .id(c.getId()).productId(c.getProductId()).userId(c.getUserId())
                .username(c.getIsAnonymous()==1?"匿名用户":c.getUsername())
                .avatar(c.getAvatar()).content(c.getContent()).rating(c.getRating())
                .images(imageList).likeCount(c.getLikeCount()).replyCount(c.getReplyCount())
                .isAnonymous(c.getIsAnonymous()).isAppend(c.getIsAppend()).status(c.getStatus())
                .liked(liked).replies(replyVOS).createTime(c.getCreateTime()).updateTime(c.getUpdateTime()).build();
    }

    @SuppressWarnings("unchecked")
    private List<String> parseImages(String images) {
        if (images == null || images.isEmpty()) return List.of();
        try { return objectMapper.readValue(images, List.class); }
        catch (Exception e) {
            if (images.startsWith("[")) return List.of();
            return List.of(images);
        }
    }
}
