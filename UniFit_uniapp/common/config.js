const storedBaseUrl = typeof uni !== 'undefined' && uni.getStorageSync ? uni.getStorageSync('unifit_base_url') : ''
export const BASE_URL = storedBaseUrl || 'http://107.148.176.142:9020/api'
