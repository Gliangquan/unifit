<template>
  <view class="page-container">
    <!-- User Profile Card -->
    <view class="user-profile-card">
      <view class="avatar-container">
        <image
          v-if="displayAvatar"
          :src="displayAvatar"
          class="avatar-img"
          mode="aspectFill"
        />
        <uni-icons v-else type="person-filled" size="50" color="#c1c9d2"></uni-icons>
      </view>
      <view class="user-info">
        <view class="user-name">
          {{ user.userName || '未设置昵称' }}
          <text class="role-tag" :class="isAdminRole ? 'role-admin' : 'role-student'">{{ roleText }}</text>
        </view>
        <view class="user-account">账号: {{ user.userAccount || '-' }}</view>
      </view>
    </view>

    <!-- Account Settings Section -->
    <view class="section-container">
      <view class="section-header">账号设置</view>
      <uni-list class="custom-list" :border="false">
        <uni-list-item title="编辑资料" showArrow clickable @click="goEditProfile">
          <template v-slot:header>
            <uni-icons type="person" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
          </template>
        </uni-list-item>
      </uni-list>
    </view>

    <!-- Student Services Section -->
    <template v-if="!isAdminRole">
      <view class="section-container">
        <view class="section-header">学生服务</view>
        <uni-list class="custom-list" :border="false">
          <uni-list-item title="学生身份认证" showArrow clickable @click="goStudentVerify">
            <template v-slot:header>
              <uni-icons type="vip" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
            <template v-slot:footer>
              <text class="status-text" :class="getVerifyStatusClass()">{{ verifyStatusText }}</text>
            </template>
          </uni-list-item>
          
          <uni-list-item title="健康档案" showArrow clickable @click="goHealthProfile">
            <template v-slot:header>
              <uni-icons type="heart" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
            <template v-slot:footer>
              <text class="status-text" :class="getHealthStatusClass()">{{ healthStatusText }}</text>
            </template>
          </uni-list-item>
          
          <uni-list-item title="BMI健康状态" showArrow clickable @click="goHealthProfile">
            <template v-slot:header>
              <uni-icons type="medal" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
            <template v-slot:footer>
              <text class="status-text" :class="getBmiStatusClass()">{{ bmiStatusText }}</text>
            </template>
          </uni-list-item>
          
          <uni-list-item title="余额钱包" showArrow clickable @click="goWallet">
            <template v-slot:header>
              <uni-icons type="wallet" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
            <template v-slot:footer>
              <text class="wallet-balance-text">¥{{ balanceText }}</text>
            </template>
          </uni-list-item>

          <uni-list-item title="购买记录" showArrow clickable @click="goOrderRecords">
            <template v-slot:header>
              <uni-icons type="list" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
            <template v-slot:footer>
              <text class="status-text status-normal">{{ purchaseRecordCount }} 条</text>
            </template>
          </uni-list-item>
        </uni-list>
      </view>
    </template>

    <template v-else>
      <view class="section-container">
        <view class="section-header">系统说明</view>
        <view style="padding: 20rpx;">
          <uni-notice-bar text="管理员账号无需学生认证和健康档案维护" show-icon class="admin-notice" />
        </view>
      </view>
    </template>
    
    <view class="section-container" style="margin-top: 24rpx; margin-bottom: 24rpx; background: transparent; border: none; box-shadow: none;">
      <button class="uf-btn-secondary" style="color: #ef4444; border-color: #fecaca; background: #fff;" @click="handleLogout">退出登录</button>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser, getUser } from '@/common/auth'
import { resolveAssetUrl } from '@/common/asset'

export default {
  data() {
    return {
      user: {},
      studentProfile: {},
      healthProfile: {},
      balance: 0,
      planUnlocked: 0,
      rechargeAmount: '',
      purchaseRecordCount: 0
    }
  },
  computed: {
    isAdminRole() {
      return (this.user.userRole || 'student') === 'admin'
    },
    roleText() {
      return this.isAdminRole ? '管理员' : '学生'
    },
    displayAvatar() {
      return resolveAssetUrl(this.user.userAvatar)
    },
    verifyStatusText() {
      const map = {
        pending: '待审核',
        approved: '已通过',
        rejected: '已拒绝'
      }
      return map[this.studentProfile.verificationStatus] || '未提交'
    },
    healthStatusText() {
      const ready = this.healthProfile.height && this.healthProfile.weight && this.healthProfile.age && this.healthProfile.gender
      return ready ? '已填写' : '待填写'
    },
    bmiStatusText() {
      if (this.healthProfile.bmiValue) {
        return `${this.healthProfile.bmiValue} (${this.healthProfile.bmiStatus || '待评估'})`
      }
      return '待评估'
    },
    balanceText() {
      return Number(this.balance || 0).toFixed(2)
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    if (!this.isAdminRole) {
      await Promise.all([this.loadStudentProfile(), this.loadHealthProfile()])
      this.loadPurchaseRecordCount()
    }
  },
  methods: {
    getPurchaseOrderListKey() {
      const uid = this.user.id || (uni.getStorageSync('user') || {}).id || 'guest'
      return `purchase_orders_${uid}`
    },
    loadPurchaseRecordCount() {
      const list = uni.getStorageSync(this.getPurchaseOrderListKey()) || []
      this.purchaseRecordCount = Array.isArray(list) ? list.length : 0
    },
    async loadCurrentUser() {
      const latest = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = { ...localUser, ...latest, token: localUser.token }
      this.balance = Number(this.user.balance || 0)
      this.planUnlocked = Number(this.user.planUnlocked || 0)
      setUser(this.user)
      uni.setStorageSync('user', this.user)
    },
    async loadStudentProfile() {
      this.studentProfile = await request({ url: '/student/profile/my', showError: false }) || {}
    },
    async loadHealthProfile() {
      this.healthProfile = await request({ url: '/health/profile/my', showError: false }) || {}
    },
    goEditProfile() {
      uni.navigateTo({ url: '/pages/mine/edit-profile' })
    },
    getVerifyStatusClass() {
      const status = this.studentProfile.verificationStatus
      if (status === 'approved') return 'status-success'
      if (status === 'pending') return 'status-warning'
      if (status === 'rejected') return 'status-danger'
      return 'status-normal'
    },
    getHealthStatusClass() {
      return this.healthStatusText === '已填写' ? 'status-success' : 'status-warning'
    },
    getBmiStatusClass() {
      const status = this.healthProfile.bmiStatus
      if (status === 'normal') return 'status-success'
      if (status === 'underweight' || status === 'overweight') return 'status-warning'
      if (status === 'obese') return 'status-danger'
      return 'status-normal'
    },
    async applyRecharge(amount) {
      const rechargeValue = Number(amount)
      if (!Number.isFinite(rechargeValue) || rechargeValue <= 0) {
        uni.showToast({ title: '请输入正确金额', icon: 'none' })
        return
      }
      const data = await request({
        url: '/user/balance/recharge',
        method: 'POST',
        data: { amount: rechargeValue }
      })
      this.balance = Number(data.balance || 0)
      this.planUnlocked = Number(data.planUnlocked || 0)
      const localUser = uni.getStorageSync('user') || {}
      const mergedUser = { ...localUser, ...data, token: localUser.token }
      this.user = mergedUser
      setUser(mergedUser)
      uni.setStorageSync('user', mergedUser)
      uni.showToast({ title: '充值成功', icon: 'success' })
    },
    quickRecharge(amount) {
      this.applyRecharge(amount)
    },
    submitRecharge() {
      const amount = Number(this.rechargeAmount)
      if (!this.rechargeAmount || amount <= 0) {
        uni.showToast({ title: '请输入有效金额', icon: 'none' })
        return
      }
      this.applyRecharge(amount)
      this.rechargeAmount = ''
    },
    goStudentVerify() {
      uni.navigateTo({ url: '/pages/mine/student-verify' })
    },
    goHealthProfile() {
      uni.navigateTo({ url: '/pages/mine/health-profile' })
    },
    goWallet() {
      uni.navigateTo({ url: '/pages/mine/wallet' })
    },
    goOrderRecords() {
      uni.navigateTo({ url: '/pages/mine/order-records' })
    },
    async handleLogout() {
      uni.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: async (res) => {
          if (res.confirm) {
            await request({ url: '/user/logout', method: 'POST', showError: false }).catch(() => null)
            const currentUser = getUser() || {}
            if (currentUser.id) {
              const purchaseOrders = uni.getStorageSync(`purchase_orders_${currentUser.id}`)
              const walletTransactions = uni.getStorageSync(`wallet_transactions_${currentUser.id}`)
              const courseUnlockKeys = []
              const storageInfo = uni.getStorageInfoSync()
              ;(storageInfo.keys || []).forEach(key => {
                if (key.indexOf(`course_unlock_${currentUser.id}_`) === 0) {
                  courseUnlockKeys.push({ key, value: uni.getStorageSync(key) })
                }
              })
              uni.clearStorageSync()
              if (purchaseOrders) {
                uni.setStorageSync(`purchase_orders_${currentUser.id}`, purchaseOrders)
              }
              if (walletTransactions) {
                uni.setStorageSync(`wallet_transactions_${currentUser.id}`, walletTransactions)
              }
              courseUnlockKeys.forEach(item => uni.setStorageSync(item.key, item.value))
            } else {
              uni.removeStorageSync('user')
            }
            uni.reLaunch({ url: '/pages/login/login' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/common.scss";

.page-container {
  min-height: 100vh;
  background: $bg-page;
  padding: 16rpx 0 env(safe-area-inset-bottom);
}

.user-profile-card {
  margin: 0 16rpx 16rpx;
  padding: 20rpx;
  background: #ffffff;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
  display: flex;
  align-items: center;
}

.avatar-container {
  width: 96rpx;
  height: 96rpx;
  border-radius: 48rpx;
  background: #f3f4f6;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 20rpx;
  overflow: hidden;
}

.avatar-img {
  width: 96rpx;
  height: 96rpx;
  border-radius: 48rpx;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8rpx;
  display: flex;
  align-items: center;
}

.role-tag {
  margin-left: 12rpx;
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: $radius-sm;
  font-weight: 500;
}

.role-admin {
  background-color: #f1f5f9;
  color: #475569;
}

.role-student {
  background-color: #fff7ed;
  color: $primary-color;
}

.user-account {
  font-size: 24rpx;
  color: $text-secondary;
}

.section-container {
  margin: 0 16rpx 16rpx;
  background-color: #ffffff;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
}

.section-header {
  padding: 20rpx 20rpx 10rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
}

.custom-list {
  background-color: transparent;
}

:deep(.uni-list-item__container) {
  padding: 22rpx 20rpx !important;
}

:deep(.uni-list-item__content-title) {
  font-size: 27rpx !important;
  color: $text-primary !important;
}

:deep(.uni-list-item__extra-text) {
  font-size: 24rpx !important;
  color: $text-muted !important;
}

.wallet-card {
  padding: 12rpx 20rpx 20rpx;
}

.wallet-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14rpx;
}

.wallet-label {
  font-size: 24rpx;
  color: $text-secondary;
}

.wallet-balance {
  font-size: 36rpx;
  font-weight: 700;
  color: $text-primary;
}

.wallet-balance-text {
  font-size: 28rpx;
  font-weight: 600;
  color: $primary-color;
}

.wallet-status {
  font-size: 24rpx;
  font-weight: 600;
}

.wallet-btn-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10rpx;
  margin: 16rpx 0;
}

.wallet-mini-btn {
  height: 68rpx;
  line-height: 68rpx;
  border-radius: $radius-md;
  border: 1rpx solid $border-color;
  background: #ffffff;
  color: $text-primary;
  font-size: 24rpx;
  font-weight: 600;
}

.wallet-submit {
  margin-top: 14rpx;
}

.status-text {
  font-size: 24rpx;
}

.status-success { color: #16a34a; }
.status-warning { color: #ca8a04; }
.status-danger { color: #dc2626; }
.status-normal { color: $text-muted; }

.admin-notice {
  margin: 0 20rpx;
  border-radius: $radius-md;
}
</style>
