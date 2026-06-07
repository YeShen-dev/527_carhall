import request from './request.js'
export function createPayment(orderId, payMethod) { return request.post('/payment/create', { orderId, payMethod }) }
export function queryPaymentStatus(orderId) { return request.get(`/payment/status/${orderId}`) }
export function closePayment(orderId) { return request.post(`/payment/close/${orderId}`) }
export function createRefund(orderId, reason, amount) { return request.post('/payment/refund', { orderId, reason, refundAmount: amount }) }
export function queryRefundStatus(refundNo) { return request.get(`/payment/refund/status/${refundNo}`) }
