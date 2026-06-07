import request from './request.js'

/** 商品评论列表 */
export function getComments(productId, params) {
  return request.get(`/comment/product/${productId}`, params)
}

/** 评论统计 */
export function getCommentStats(productId) {
  return request.get(`/comment/statistics/${productId}`)
}

/** 发表评论 */
export function addComment(data) {
  return request.post('/comment/add', data)
}

/** 点赞/取消 */
export function toggleLike(commentId) {
  return request.post(`/comment/${commentId}/like`)
}

/** 回复评论 */
export function replyComment(data) {
  return request.post('/comment/reply', data)
}

/** 删除评论 */
export function deleteComment(id) {
  return request.delete(`/comment/${id}`)
}
