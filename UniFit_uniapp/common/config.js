const storedBaseUrl = typeof uni !== 'undefined' && uni.getStorageSync ? uni.getStorageSync('unifit_base_url') : ''

function resolveDefaultBaseUrl() {
  if (typeof window !== 'undefined' && window.location && /^https?:$/i.test(window.location.protocol)) {
    const host = window.location.hostname || ''
    if (host === 'localhost' || host === '127.0.0.1') {
      return 'http://127.0.0.1:19921/api'
    }
    return `${window.location.origin}/api`
  }
  return 'http://127.0.0.1:19921/api'
}

export const BASE_URL = storedBaseUrl || resolveDefaultBaseUrl()
