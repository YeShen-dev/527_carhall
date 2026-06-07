import request from './request.js'

export function placeOrder() {
  return request.post('/orders')
}

export function listOrders(params) {
  return request.get('/orders', { params })
}

export function getOrderDetail(id) {
  return request.get(`/orders/${id}`)
}
