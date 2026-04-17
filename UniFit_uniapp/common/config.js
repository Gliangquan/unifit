const storedBaseUrl = typeof uni !== 'undefined' && uni.getStorageSync ? uni.getStorageSync('unifit_base_url') : ''

function resolveDefaultBaseUrl() {
  if (typeof window !== 'undefined' && window.location && /^https?:$/i.test(window.location.protocol)) {
    return `${window.location.origin}/api`
  }
  return 'http://127.0.0.1:9020/api'
}

export const BASE_URL = storedBaseUrl || resolveDefaultBaseUrl()
