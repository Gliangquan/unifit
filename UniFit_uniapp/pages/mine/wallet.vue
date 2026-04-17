<template>
  <view class="page-container">
    <!-- Header -->
    <view class="header">
      <view class="header-top">
        <text class="header-back" @click="goBack">←</text>
        <text class="header-title">余额钱包</text>
        <view style="width: 40rpx;"></view>
      </view>
    </view>

    <!-- Balance Card -->
    <view class="balance-card">
      <view class="balance-row">
        <view class="balance-info">
          <view class="balance-label">当前余额</view>
          <view class="balance-amount">¥{{ balanceText }}</view>
        </view>
        <view class="balance-status">
          <view class="status-item">
            <text class="status-label">计划权限</text>
            <text class="status-value" :class="planUnlocked ? 'status-success' : 'status-warning'">
              {{ planUnlocked ? '已解锁' : '未解锁' }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- Recharge Section -->
    <view class="section-container">
      <view class="section-header">快速充值</view>
      <view class="quick-recharge">
        <view 
          v-for="amount in quickAmounts" 
          :key="amount"
          class="recharge-btn"
          @click="selectAmount(amount)"
          :class="{ active: selectedAmount === amount }"
        >
          <text class="btn-label">¥{{ amount }}</text>
        </view>
      </view>
    </view>

    <!-- Custom Amount Section -->
    <view class="section-container">
      <view class="section-header">自定义金额</view>
      <view class="custom-amount">
        <view class="input-group">
          <text class="currency-symbol">¥</text>
          <input 
            v-model="customAmount"
            type="number"
            placeholder="请输入充值金额"
            class="amount-input"
            @input="onCustomAmountChange"
          />
        </view>
        <view class="amount-tips">
          <text>最低充值金额：¥10</text>
          <text>最高充值金额：¥10000</text>
        </view>
      </view>
    </view>

    <!-- Payment Methods Section -->
    <view class="section-container">
      <view class="section-header">支付方式</view>
      <view class="payment-methods">
        <view 
          v-for="method in paymentMethods"
          :key="method.id"
          class="payment-item"
          @click="selectPaymentMethod(method.id)"
          :class="{ active: selectedPaymentMethod === method.id }"
        >
          <view class="payment-icon">
            <text class="icon-text">{{ method.icon }}</text>
          </view>
          <view class="payment-info">
            <view class="payment-name">{{ method.name }}</view>
            <view class="payment-desc">{{ method.desc }}</view>
          </view>
          <view class="payment-radio">
            <view class="radio" :class="{ checked: selectedPaymentMethod === method.id }"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- Recharge Button -->
    <view class="button-container">
      <button 
        class="recharge-submit-btn"
        :disabled="!canRecharge"
        @click="handleRecharge"
      >
        {{ rechargeButtonText }}
      </button>
    </view>

    <!-- Transaction History -->
    <view class="section-container" style="margin-bottom: 40rpx;">
      <view class="section-header">充值记录</view>
      <view v-if="transactions.length > 0" class="transaction-list">
        <view v-for="(tx, index) in transactions" :key="index" class="transaction-item">
          <view class="tx-info">
            <view class="tx-type">{{ tx.type }}</view>
            <view class="tx-time">{{ tx.time }}</view>
          </view>
          <view class="tx-amount" :class="{ 'tx-income': tx.amount > 0 }">
            {{ tx.amount > 0 ? '+' : '' }}¥{{ Math.abs(tx.amount).toFixed(2) }}
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">暂无充值记录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin } from '@/common/auth'

export default {
  data() {
    return {
      balance: 0,
      planUnlocked: 0,
      selectedAmount: null,
      customAmount: '',
      selectedPaymentMethod: 'wechat',
      quickAmounts: [50, 100, 200, 500],
      paymentMethods: [
        { id: 'wechat', name: '微信支付', desc: '推荐使用', icon: '💳' },
        { id: 'alipay', name: '支付宝', desc: '快速安全', icon: '🏦' },
        { id: 'card', name: '银行卡', desc: '储蓄卡/信用卡', icon: '🎫' }
      ],
      transactions: []
    }
  },
  computed: {
    balanceText() {
      return Number(this.balance || 0).toFixed(2)
    },
    rechargeAmount() {
      return this.selectedAmount || Number(this.customAmount) || 0
    },
    canRecharge() {
      const amount = this.rechargeAmount
      return amount >= 10 && amount <= 10000
    },
    rechargeButtonText() {
      if (!this.canRecharge) {
        return '请输入10-10000元'
      }
      return `确认充值 ¥${this.rechargeAmount.toFixed(2)}`
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadUserBalance()
  },
  methods: {
    getTransactionKey() {
      const user = uni.getStorageSync('user') || {}
      return `wallet_transactions_${user.id || 'guest'}`
    },
    saveTransaction(record) {
      if (!record) return
      const key = this.getTransactionKey()
      const list = uni.getStorageSync(key) || []
      list.unshift(record)
      uni.setStorageSync(key, list.slice(0, 50))
      this.transactions = uni.getStorageSync(key) || []
    },
    formatTime(value) {
      const d = new Date(value)
      const y = d.getFullYear()
      const m = `${d.getMonth() + 1}`.padStart(2, '0')
      const day = `${d.getDate()}`.padStart(2, '0')
      const hh = `${d.getHours()}`.padStart(2, '0')
      const mm = `${d.getMinutes()}`.padStart(2, '0')
      return `${y}-${m}-${day} ${hh}:${mm}`
    },
    async loadUserBalance() {
      const latest = await request({ url: '/user/get/login', showError: false }).catch(() => null)
      const localUser = uni.getStorageSync('user') || {}
      const user = { ...localUser, ...(latest || {}), token: localUser.token }
      uni.setStorageSync('user', user)
      this.balance = Number(user.balance || 0)
      this.planUnlocked = Number(user.planUnlocked || 0)
      this.transactions = uni.getStorageSync(this.getTransactionKey()) || []
    },
    syncUserStore(latestUser) {
      const localUser = uni.getStorageSync('user') || {}
      const merged = { ...localUser, ...(latestUser || {}), token: localUser.token }
      uni.setStorageSync('user', merged)
      this.balance = Number(merged.balance || 0)
      this.planUnlocked = Number(merged.planUnlocked || 0)
      return merged
    },
    selectAmount(amount) {
      this.selectedAmount = amount
      this.customAmount = ''
    },
    onCustomAmountChange() {
      this.selectedAmount = null
    },
    selectPaymentMethod(methodId) {
      this.selectedPaymentMethod = methodId
    },
    async handleRecharge() {
      if (!this.canRecharge) {
        uni.showToast({ title: '请输入有效金额', icon: 'none' })
        return
      }
      const amount = Number(this.rechargeAmount)
      const method = this.paymentMethods.find(m => m.id === this.selectedPaymentMethod)
      uni.showLoading({ title: '处理中...' })
      try {
        const latestUser = await request({
          url: '/user/balance/recharge',
          method: 'POST',
          data: { amount }
        })
        this.syncUserStore(latestUser)
        this.saveTransaction({
          type: `充值-${(method && method.name) || '钱包'}`,
          time: this.formatTime(Date.now()),
          amount
        })
        this.selectedAmount = null
        this.customAmount = ''
        uni.hideLoading()
        uni.showToast({ title: '充值成功', icon: 'success' })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ title: (error && error.message) || '充值失败，请重试', icon: 'none' })
      }
    },
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/common.scss";

.page-container {
  min-height: 100vh;
  background: $bg-page;
  padding: 0 0 env(safe-area-inset-bottom);
}

.header {
  background: #ffffff;
  border-bottom: 1rpx solid $border-color;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 20rpx;
  height: 88rpx;
}

.header-back {
  font-size: 40rpx;
  color: $text-primary;
  width: 40rpx;
  text-align: center;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
  text-align: center;
}

.balance-card {
  margin: 16rpx 16rpx;
  padding: 24rpx;
  background: #ffffff;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
}

.balance-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24rpx;
}

.balance-info {
  flex: 1;
}

.balance-label {
  font-size: 24rpx;
  color: $text-secondary;
  margin-bottom: 12rpx;
}

.balance-amount {
  font-size: 48rpx;
  font-weight: 700;
  color: $primary-color;
}

.balance-status {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.status-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.status-label {
  font-size: 20rpx;
  color: $text-secondary;
  margin-bottom: 4rpx;
}

.status-value {
  font-size: 24rpx;
  font-weight: 600;
}

.status-success { color: #16a34a; }
.status-warning { color: #ca8a04; }

.section-container {
  margin: 16rpx 16rpx;
  background: #ffffff;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
}

.section-header {
  padding: 20rpx 20rpx 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
}

.quick-recharge {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
  padding: 12rpx 20rpx 20rpx;
}

.recharge-btn {
  padding: 20rpx;
  border: 2rpx solid $border-color;
  border-radius: $radius-md;
  text-align: center;
  transition: all 0.3s ease;
  background: #ffffff;
}

.recharge-btn.active {
  border-color: $primary-color;
  background: rgba(255, 107, 53, 0.1);
}

.btn-label {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
}

.recharge-btn.active .btn-label {
  color: $primary-color;
}

.custom-amount {
  padding: 12rpx 20rpx 20rpx;
}

.input-group {
  display: flex;
  align-items: center;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  padding: 0 16rpx;
  background: #f9fafb;
  margin-bottom: 12rpx;
}

.currency-symbol {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
  margin-right: 8rpx;
}

.amount-input {
  flex: 1;
  height: 80rpx;
  font-size: 28rpx;
  color: $text-primary;
  background: transparent;
  border: none;
  outline: none;
}

.amount-tips {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  font-size: 20rpx;
  color: $text-secondary;
}

.payment-methods {
  padding: 12rpx 20rpx 20rpx;
}

.payment-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  margin-bottom: 12rpx;
  transition: all 0.3s ease;
  background: #ffffff;
}

.payment-item.active {
  border-color: $primary-color;
  background: rgba(255, 107, 53, 0.05);
}

.payment-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: $radius-md;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  font-size: 32rpx;
}

.payment-info {
  flex: 1;
}

.payment-name {
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 4rpx;
}

.payment-desc {
  font-size: 20rpx;
  color: $text-secondary;
}

.payment-radio {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.radio {
  width: 20rpx;
  height: 20rpx;
  border: 2rpx solid $border-color;
  border-radius: 50%;
  background: #ffffff;
  transition: all 0.3s ease;
}

.payment-item.active .radio {
  border-color: $primary-color;
  background: $primary-color;
  box-shadow: inset 0 0 0 4rpx #ffffff;
}

.button-container {
  padding: 20rpx 16rpx;
  background: #ffffff;
  border-top: 1rpx solid $border-color;
}

.recharge-submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: $radius-md;
  background: $primary-color;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
  transition: all 0.3s ease;
}

.recharge-submit-btn:disabled {
  background: #d1d5db;
  color: #9ca3af;
}

.transaction-list {
  padding: 12rpx 20rpx 20rpx;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid $border-color;
}

.transaction-item:last-child {
  border-bottom: none;
}

.tx-info {
  flex: 1;
}

.tx-type {
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 4rpx;
}

.tx-time {
  font-size: 20rpx;
  color: $text-secondary;
}

.tx-amount {
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
}

.tx-income {
  color: #16a34a;
}

.empty-state {
  padding: 40rpx 20rpx;
  text-align: center;
}

.empty-text {
  font-size: 24rpx;
  color: $text-secondary;
}
</style>
