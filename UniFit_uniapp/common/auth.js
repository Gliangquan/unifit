export function getUser() {
  return uni.getStorageSync('user') || {}
}

export function setUser(user) {
  uni.setStorageSync('user', user || {})
}

export function getRole() {
  return getUser().userRole || 'student'
}

export function isAdmin() {
  return getRole() === 'admin'
}

export function ensureLogin() {
  const user = getUser()
  if (!user || !user.token) {
    uni.redirectTo({ url: '/pages/login/login' })
    return false
  }
  return true
}

export function ensureAdmin() {
  if (!ensureLogin()) return false
  if (!isAdmin()) {
    uni.showToast({ title: '仅管理员可访问', icon: 'none' })
    uni.switchTab({ url: '/pages/index/index' })
    return false
  }
  return true
}
