import request from './request.js'

export function getCaptcha() {
  return request.get('/captcha/generate')
}
