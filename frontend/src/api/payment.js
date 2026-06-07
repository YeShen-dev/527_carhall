import request from './request.js'

/** 创建支付（返回支付信息含二维码/跳转URL） */
export function createPayment(orderId, payMethod) {
  return request.post('/payment/create', { orderId, payMethod })
}

/** 查询支付状态（用于前端轮询） */
export function queryPaymentStatus(orderId) {
  return request.get(`/payment/status/${orderId}`)
}

/** 关闭未支付订单 */
export function closePayment(orderId) {
  return request.post(`/payment/close/${orderId}`)
}

/** 发起退款 */
export function createRefund(orderId, reason, refundAmount) {
  return request.post('/payment/refund', { orderId, reason, refundAmount })
}

/** 查询退款状态 */
export function queryRefundStatus(refundNo) {
  return request.get(`/payment/refund/status/${refundNo}`)
}
