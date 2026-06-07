import request from './request.js'
import { getSessionId } from '../utils/session.js'

function sessionUrl() {
  return `/cart/temp/${getSessionId()}`
}

export function listCartTemp() {
  return request.get(sessionUrl())
}

export function addToCartTemp(productId, quantity = 1) {
  return request.post(sessionUrl(), { productId, quantity })
}

export function updateCartItemTemp(productId, quantity) {
  return request.put(`${sessionUrl()}/${productId}`, { quantity })
}

export function removeCartItemTemp(productId) {
  return request.delete(`${sessionUrl()}/${productId}`)
}

export function clearCartTemp() {
  return request.delete(sessionUrl())
}
