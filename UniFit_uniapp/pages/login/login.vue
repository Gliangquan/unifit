<template>
  <view class="page-container flex-center">
    <view class="card" style="width: 100%; max-width: 600rpx;">
      <view class="page-header">
        <text class="title">UniFit</text>
        <text class="subtitle">{{ showRegister ? '创建账号' : '登录' }}</text>
      </view>

      <view v-if="!showRegister" class="form-wrapper">
        <view class="form">
          <view class="input-item">
            <input v-model="form.userAccount" class="input-field" placeholder="账号" />
          </view>
          <view class="input-item">
            <input v-model="form.userPassword" class="input-field" type="password" placeholder="密码" />
          </view>

          <view class="btn-wrapper">
            <button class="btn-primary" :disabled="loading" @tap="handleLogin">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </view>
        </view>

        <view class="links">
          <text @tap="showRegister = true">创建账号</text>
          <text @tap="forgetPassword">忘记密码</text>
        </view>
      </view>

      <view v-else class="form-wrapper">
        <view class="form">
          <view class="input-item">
            <input v-model="registerForm.userAccount" class="input-field" placeholder="账号" />
          </view>
          <view class="input-item">
            <input v-model="registerForm.userName" class="input-field" placeholder="昵称" />
          </view>
          <view class="input-item">
            <input v-model="registerForm.userPhone" class="input-field" type="number" placeholder="手机号" />
          </view>
          <view class="input-item">
            <input v-model="registerForm.userPassword" class="input-field" type="password" placeholder="密码" />
          </view>
          <view class="input-item">
            <input v-model="registerForm.confirmPassword" class="input-field" type="password" placeholder="确认密码" />
          </view>

          <view class="btn-wrapper">
            <button class="btn-primary" :disabled="registering" @tap="handleRegister">
              {{ registering ? '注册中...' : '注册' }}
            </button>
          </view>
        </view>

        <view class="links">
          <text @tap="showRegister = false">返回登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'

export default {
  data() {
    return {
      showRegister: false,
      form: {
        userAccount: '',
        userPassword: ''
      },
      registerForm: {
        userAccount: '',
        userName: '',
        userPhone: '',
        userPassword: '',
        confirmPassword: ''
      },
      loading: false,
      registering: false
    }
  },
  onLoad() {
    const user = uni.getStorageSync('user')
    if (user && user.id) {
      this.goHome()
    }
  },
  methods: {
    async handleLogin() {
      if (this.loading) return

      const { userAccount, userPassword } = this.form
      if (!userAccount) {
        return uni.showToast({ title: '请输入账号', icon: 'none' })
      }
      if (!userPassword) {
        return uni.showToast({ title: '请输入密码', icon: 'none' })
      }

      this.loading = true
      try {
        const data = await request({
          url: '/user/login',
          method: 'POST',
          data: {
            loginType: 'account',
            userAccount,
            userPassword
          }
        })
        uni.setStorageSync('user', data)
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          this.goHome()
        }, 800)
      } catch (error) {
        uni.showToast({ title: error.message || '登录失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      if (this.registering) return
      const { userAccount, userName, userPhone, userPassword, confirmPassword } = this.registerForm
      if (!userAccount || !userName || !userPhone || !userPassword || !confirmPassword) {
        return uni.showToast({ title: '请填写完整信息', icon: 'none' })
      }
      if (userPassword !== confirmPassword) {
        return uni.showToast({ title: '两次密码不一致', icon: 'none' })
      }

      this.registering = true
      try {
        await request({
          url: '/user/register',
          method: 'POST',
          data: {
            userAccount,
            userName,
            userPhone,
            userPassword,
            checkPassword: confirmPassword
          }
        })
        uni.showToast({ title: '注册成功，请登录', icon: 'success' })
        this.showRegister = false
        this.form.userAccount = userAccount
        this.form.userPassword = ''
      } catch (error) {
        uni.showToast({ title: error.message || '注册失败', icon: 'none' })
      } finally {
        this.registering = false
      }
    },
    forgetPassword() {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    },
    goHome() {
      uni.switchTab({
        url: '/pages/index/index',
        fail: () => {
          uni.reLaunch({ url: '/pages/index/index' })
        }
      })
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.form-wrapper {
  margin-top: 24rpx;
}

.input-field {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  font-size: 28rpx;
  color: $text-primary;
  box-sizing: border-box;
  background: #fff;
  transition: all 0.3s ease;

  &:focus {
    border-color: $primary-color;
    background: #fff;
  }

  &::placeholder {
    color: $text-muted;
  }
}
</style>
