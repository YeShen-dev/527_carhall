// API base URL — use your backend address
const BASE_URL = 'http://localhost:8833/api'

let isRefreshing = false
let refreshQueue = []

function onRefreshed(token) {
  refreshQueue.forEach(cb => cb(token))
  refreshQueue = []
}

function request(options) {
  const { url, method = 'GET', data = {}, header = {}, skipAuth = false } = options

  return new Promise((resolve, reject) => {
    const token = !skipAuth ? uni.getStorageSync('token') : null
    const headers = { 'Content-Type': 'application/json', ...header }
    if (token) headers['Authorization'] = 'Bearer ' + token

    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: headers,
      timeout: 15000,
      success(res) {
        const { statusCode, data: body } = res
        if (statusCode === 200 && body.code === 200) {
          resolve(body)
        } else if (statusCode === 401 && !skipAuth) {
          // Token refresh
          const refreshToken = uni.getStorageSync('refreshToken')
          if (refreshToken && !options._retry) {
            if (isRefreshing) {
              refreshQueue.push(newToken => {
                headers['Authorization'] = 'Bearer ' + newToken
                uni.request({ url: BASE_URL + url, method, data, header: headers, success(r) { resolve(r.data) }, fail: reject })
              })
              return
            }
            options._retry = true
            isRefreshing = true
            uni.request({
              url: BASE_URL + '/auth/refresh',
              method: 'POST',
              data: { refreshToken },
              success(r) {
                const nt = r.data?.data?.token
                const nrt = r.data?.data?.refreshToken
                if (nt) uni.setStorageSync('token', nt)
                if (nrt) uni.setStorageSync('refreshToken', nrt)
                onRefreshed(nt)
                headers['Authorization'] = 'Bearer ' + nt
                uni.request({ url: BASE_URL + url, method, data, header: headers, success(rr) { resolve(rr.data) }, fail: reject })
              },
              fail() {
                uni.removeStorageSync('token')
                uni.removeStorageSync('refreshToken')
                uni.reLaunch({ url: '/pages/login/login' })
                reject(new Error('登录已过期'))
              },
              complete() { isRefreshing = false }
            })
            return
          }
          uni.removeStorageSync('token')
          uni.removeStorageSync('refreshToken')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录'))
        } else {
          uni.showToast({ title: body?.message || '请求失败', icon: 'none' })
          reject(new Error(body?.message || 'request error'))
        }
      },
      fail(err) {
        uni.showToast({ title: '网络错误，请重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

;['get','post','put','delete'].forEach(m => {
  request[m] = (url, data, opts) => request({ url, method: m.toUpperCase(), data, ...opts })
})

export default request
