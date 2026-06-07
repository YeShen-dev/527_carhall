import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../api/request.js'

function isTokenExpired(t) {
  if (!t) return true
  try {
    const payload = JSON.parse(atob(t.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch { return true }
}

export const useUserStore = defineStore('user', () => {
  const storedToken = localStorage.getItem('token')
  const storedRefresh = localStorage.getItem('refreshToken')
  const storedInfo = localStorage.getItem('userInfo')

  const validToken = !isTokenExpired(storedToken) ? storedToken : ''
  const validRefresh = !isTokenExpired(storedRefresh) ? storedRefresh : ''

  if (!validToken && storedToken) {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  const token = ref(validToken)
  const refreshToken = ref(validRefresh)
  const userInfo = ref(validToken ? JSON.parse(storedInfo || 'null') : null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  function setAuth(t, user, rt) {
    token.value = t
    userInfo.value = user
    localStorage.setItem('token', t)
    localStorage.setItem('userInfo', JSON.stringify(user))
    if (rt) {
      refreshToken.value = rt
      localStorage.setItem('refreshToken', rt)
    }
  }

  async function login(username, password, sessionId, captchaUuid, captchaCode) {
    const res = await request.post('/auth/login', { username, password, sessionId, captchaUuid, captchaCode })
    setAuth(res.data.token, res.data.user, res.data.refreshToken)
    return res.data
  }

  async function register(data) {
    const res = await request.post('/auth/register', data)
    setAuth(res.data.token, res.data.user, res.data.refreshToken)
    return res.data
  }

  /** 手机号 + 验证码登录 */
  async function loginByPhone(phone, smsCode) {
    const res = await request.post('/auth/loginByPhone', { phone, smsCode })
    setAuth(res.data.token, res.data.user, res.data.refreshToken)
    return res.data
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
  }

  async function fetchUserInfo() {
    const res = await request.get('/user/info')
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res.data
  }

  async function updateProfile(data) {
    await request.put('/user/info', data)
    return await fetchUserInfo()
  }

  async function updatePassword(oldPassword, newPassword) {
    await request.put('/user/password', { oldPassword, newPassword })
  }

  return { token, refreshToken, userInfo, isLoggedIn, isAdmin, login, loginByPhone, register, logout, fetchUserInfo, updateProfile, updatePassword }
})
