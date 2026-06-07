import request from './request.js'
export function listOrders(params) { return request.get('/orders', params) }
export function getOrderDetail(id) { return request.get(`/orders/${id}`) }
export function confirmReceipt(id) { return request.post(`/orders/${id}/confirm`) }
export function cancelOrder(id) { return request.post(`/orders/${id}/cancel`) }
export function createRefund(orderId, reason, amount) { return request.post('/payment/refund', { orderId, reason, refundAmount: amount }) }
export function placeOrder() { return request.post('/orders') }
