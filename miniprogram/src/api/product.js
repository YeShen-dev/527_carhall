import request from './request.js'
export function listProducts(params) { return request.get('/products', params) }
export function getProductDetail(id) { return request.get(`/products/${id}`) }
export function searchProducts(keyword, params) { return request.get('/products/search', { keyword, ...params }) }
