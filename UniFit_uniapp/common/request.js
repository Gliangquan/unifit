import { BASE_URL } from './config'

function forceLogoutToLogin() {
  uni.removeStorageSync('user')
  const pages = getCurrentPages ? getCurrentPages() : []
  const current = pages.length ? pages[pages.length - 1] : null
  if (current && current.route === 'pages/login/login') {
    return
  }
  uni.reLaunch({ url: '/pages/login/login' })
}

function shouldForceLogout(body) {
  if (!body) return false
  if (body.code === 40100) return true
  const msg = body.message || ''
  return msg.includes('账号已被禁用') || msg.includes('账号已被封禁')
}

export function request({ url, method = 'GET', data = {}, showError = true }) {
  const user = uni.getStorageSync('user') || {}
  const header = {
    'Content-Type': 'application/json'
  }
  if (user.token) {
    header.Authorization = `Bearer ${user.token}`
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header,
      success: (res) => {
        const body = res.data || {}
        if (body.code === 0) {
          resolve(body.data)
          return
        }
        if (shouldForceLogout(body)) {
          forceLogoutToLogin()
        }
        if (showError) {
          uni.showToast({ title: body.message || '请求失败', icon: 'none' })
        }
        reject(body)
      },
      fail: (err) => {
        if (showError) {
          uni.showToast({ title: '网络异常', icon: 'none' })
        }
        reject(err)
      }
    })
  })
}
