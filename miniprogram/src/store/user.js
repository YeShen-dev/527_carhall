import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../api/request.js'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const refreshToken = ref(uni.getStorageSync('refreshToken') || '')

  let parsedInfo = null
  try { const raw = uni.getStorageSync('userInfo'); if (raw) parsedInfo = JSON.parse(raw) } catch { }
  const userInfo = ref(parsedInfo)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  function setAuth(t, user, rt) {
    token.value = t; userInfo.value = user
    uni.setStorageSync('token', t); uni.setStorageSync('userInfo', JSON.stringify(user))
    if (rt) { refreshToken.value = rt; uni.setStorageSync('refreshToken', rt) }
  }

  /** 用户名密码登录 (保留) */
  async function login(username, password, captchaUuid, captchaCode, clientType) {
    const res = await request.post('/auth/login', { username, password, captchaUuid, captchaCode, clientType: clientType || 'miniapp' })
    setAuth(res.data.token, res.data.user, res.data.refreshToken)
    return res.data
  }

  /** 手机号 + 验证码登录 */
  async function loginByPhone(phone, smsCode) {
    const res = await request.post('/auth/loginByPhone', { phone, smsCode })
    setAuth(res.data.token, res.data.user, res.data.refreshToken)
    return res.data
  }

  /** 手机号 + 验证码注册 */
  async function registerByPhone(phone, smsCode, password) {
    const res = await request.post('/auth/registerByPhone', { phone, smsCode, password: password || '' })
    setAuth(res.data.token, res.data.user, res.data.refreshToken)
    return res.data
  }

  /** 发送验证码 */
  function sendSmsCode(phone) {
    return request.post('/auth/sendCode', { phone })
  }

  /** 获取用户信息 */
  async function fetchUserInfo() {
    return new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8833/api/user/info', method: 'GET',
        header: { 'Authorization': 'Bearer ' + token.value },
        success(res) {
          if (res.data.code === 200) { userInfo.value = res.data.data; uni.setStorageSync('userInfo', JSON.stringify(res.data.data)); resolve(res.data.data) }
          else reject(new Error(res.data.message))
        }, fail: reject
      })
    })
  }

  /** 更新个人资料 */
  async function updateProfile(data) {
    return new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8833/api/user/info', method: 'PUT', data,
        header: { 'Authorization': 'Bearer ' + token.value },
        success(res) {
          if (res.data.code === 200) { userInfo.value = { ...userInfo.value, ...data }; uni.setStorageSync('userInfo', JSON.stringify(userInfo.value)); resolve(res.data) }
          else reject(new Error(res.data.message))
        }, fail: reject
      })
    })
  }

  /** 修改密码 */
  async function updatePassword(oldPassword, newPassword) {
    return new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8833/api/user/password', method: 'PUT', data: { oldPassword, newPassword },
        header: { 'Authorization': 'Bearer ' + token.value },
        success(res) { res.data.code === 200 ? resolve(res.data) : reject(new Error(res.data.message)) }, fail: reject
      })
    })
  }

  /** 退出 */
  function logout() {
    token.value = ''; refreshToken.value = ''; userInfo.value = null
    uni.removeStorageSync('token'); uni.removeStorageSync('refreshToken'); uni.removeStorageSync('userInfo')
  }

  return { token, refreshToken, userInfo, isLoggedIn, isAdmin,
    login, loginByPhone, registerByPhone, sendSmsCode, fetchUserInfo, updateProfile, updatePassword, logout }
})
