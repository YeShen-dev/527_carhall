import request from './request.js'

export function listSeckillActivities() {
  return request.get('/seckill/activities')
}

export function executeSeckill(activityId) {
  return request.post(`/seckill/${activityId}/execute`)
}

export function listAdminSeckill(page = 0, size = 10) {
  return request.get('/admin/seckill', { params: { page, size } })
}

export function createSeckill(data) {
  return request.post('/admin/seckill', data)
}

export function updateSeckill(id, data) {
  return request.put(`/admin/seckill/${id}`, data)
}

export function deleteSeckill(id) {
  return request.delete(`/admin/seckill/${id}`)
}
