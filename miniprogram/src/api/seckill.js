import request from './request.js'
export function listSeckillActivities() { return request.get('/seckill/activities') }
export function executeSeckill(activityId) { return request.post(`/seckill/${activityId}/execute`) }
