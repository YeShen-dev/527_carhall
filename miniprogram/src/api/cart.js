import request from './request.js'

export function getCart() { return request.get('/cart') }
export function addToCart(productId, quantity) { return request.post('/cart', { productId, quantity }) }
export function updateCartItem(productId, quantity) { return request.put(`/cart/${productId}`, { quantity }) }
export function removeCartItem(productId) { return request.delete(`/cart/${productId}`) }
export function clearCart() { return request.delete('/cart') }
export function placeOrder() { return request.post('/orders') }
