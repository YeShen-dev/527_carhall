import request from './request.js'

/** 用户名密码登录 (web 端) */
export function login(username, password, captchaUuid, captchaCode, clientType) {
  const data = { username, password, clientType: clientType || 'web' }
  if (captchaUuid) data.captchaUuid = captchaUuid
  if (captchaCode) data.captchaCode = captchaCode
  return request.post('/auth/login', data)
}

/** 手机号 + 验证码登录 */
export function loginByPhone(phone, smsCode) {
  return request.post('/auth/loginByPhone', { phone, smsCode })
}

/** 手机号 + 验证码注册 */
export function registerByPhone(phone, smsCode, password) {
  return request.post('/auth/registerByPhone', { phone, smsCode, password: password || '' })
}

/** 发送验证码 */
export function sendSmsCode(phone) {
  return request.post('/auth/sendCode', { phone })
}

/** 注册 (原有用户名密码方式) */
export function register(data) {
  return request.post('/auth/register', data)
}
