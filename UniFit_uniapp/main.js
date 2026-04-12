
// #ifndef VUE3
import Vue from 'vue'
import App from './App'

Vue.config.productionTip = false

// 处理浏览器扩展错误
if (typeof window !== 'undefined') {
  window.addEventListener('error', (event) => {
    if (event.message && event.message.includes('runtime.lastError')) {
      event.preventDefault()
    }
  }, true)
}

App.mpType = 'app'

const app = new Vue({
    ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
import App from './App.vue'

// 处理浏览器扩展错误
if (typeof window !== 'undefined') {
  window.addEventListener('error', (event) => {
    if (event.message && event.message.includes('runtime.lastError')) {
      event.preventDefault()
    }
  }, true)
}

export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif