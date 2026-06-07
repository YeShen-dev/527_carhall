import request from './request.js'

export function listAdminProducts(params) {
  return request.get('/admin/products', { params })
}

export function addProduct(data) {
  return request.post('/admin/products', data)
}

export function updateProduct(id, data) {
  return request.put(`/admin/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/admin/products/${id}`)
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function listAdminOrders(params) {
  return request.get('/admin/orders', { params })
}

export function getAdminOrderDetail(id) {
  return request.get(`/admin/orders/${id}`)
}

export function updateOrderStatus(id, status) {
  return request.put(`/admin/orders/${id}/status`, { status })
}
