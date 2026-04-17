<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="row-between">
        <view class="uf-section-title" style="margin: 0;">购买记录</view>
        <text class="hint">共 {{ records.length }} 条</text>
      </view>

      <view v-if="records.length === 0" class="empty">
        暂无购买记录
      </view>

      <view v-else>
        <view class="record-card" v-for="item in records" :key="item.orderNo">
          <view class="row-between line">
            <text class="label">订单号</text>
            <text class="value mono">{{ item.orderNo }}</text>
          </view>
          <view class="row-between line">
            <text class="label">订单类型</text>
            <text class="value">{{ item.typeText || '课程解锁' }}</text>
          </view>
          <view class="row-between line">
            <text class="label">关联计划</text>
            <text class="value">{{ item.planName || '-' }}</text>
          </view>
          <view class="row-between line">
            <text class="label">订单金额</text>
            <text class="value">¥{{ Number(item.amount || 0).toFixed(2) }}</text>
          </view>
          <view class="row-between line">
            <text class="label">订单状态</text>
            <text :class="['uf-pill', item.status === 'paid' ? 'status-paid' : 'status-pending']">
              {{ item.status === 'paid' ? '已支付' : '待支付' }}
            </text>
          </view>
          <view class="row-between line">
            <text class="label">下单时间</text>
            <text class="value">{{ formatDateTime(item.createdAt) }}</text>
          </view>
          <view class="row-between line" v-if="item.paidAt">
            <text class="label">支付时间</text>
            <text class="value">{{ formatDateTime(item.paidAt) }}</text>
          </view>
          <button
            v-if="item.status !== 'paid' && item.planId"
            class="uf-btn-secondary mini-btn"
            @click="goPay(item)"
          >
            前往支付
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ensureLogin } from '@/common/auth'

export default {
  data() {
    return {
      user: {},
      records: []
    }
  },
  onShow() {
    if (!ensureLogin()) return
    this.loadRecords()
  },
  methods: {
    getOrderListKey() {
      const localUser = uni.getStorageSync('user') || {}
      const uid = localUser.id || 'guest'
      return `purchase_orders_${uid}`
    },
    loadRecords() {
      this.user = uni.getStorageSync('user') || {}
      const list = uni.getStorageSync(this.getOrderListKey()) || []
      const userId = this.user.id || this.resolveLatestUserId()
      const walletKey = `wallet_transactions_${userId || 'guest'}`
      const walletRows = uni.getStorageSync(walletKey) || []
      const rechargeRecords = (Array.isArray(walletRows) ? walletRows : []).map((item, index) => ({
        orderNo: `RECHARGE_${index}_${item.time || item.createdAt || index}`,
        typeText: '钱包充值',
        planName: item.type || '余额充值',
        amount: item.amount || 0,
        status: 'paid',
        createdAt: item.time || item.createdAt,
        paidAt: item.time || item.createdAt
      }))
      this.records = (Array.isArray(list) ? list : []).concat(rechargeRecords).slice().sort((a, b) => Number(new Date(b.createdAt || 0).getTime() || 0) - Number(new Date(a.createdAt || 0).getTime() || 0))
    },
    resolveLatestUserId() {
      const localUser = uni.getStorageSync('user') || {}
      return localUser.id || 'guest'
    },
    formatDateTime(v) {
      if (!v) return '--'
      const d = new Date(v)
      if (Number.isNaN(d.getTime())) return '--'
      const y = d.getFullYear()
      const m = `${d.getMonth() + 1}`.padStart(2, '0')
      const day = `${d.getDate()}`.padStart(2, '0')
      const hh = `${d.getHours()}`.padStart(2, '0')
      const mm = `${d.getMinutes()}`.padStart(2, '0')
      return `${y}-${m}-${day} ${hh}:${mm}`
    },
    goPay(item) {
      if (!item || !item.planId) return
      uni.navigateTo({ url: `/pages/plan/current?planId=${item.planId}` })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/common.scss";

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hint {
  font-size: 22rpx;
  color: $text-secondary;
}

.record-card {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: #fff;
  margin-bottom: 12rpx;
  padding: 12rpx 14rpx;
}

.line {
  padding: 8rpx 0;
  border-bottom: 1px solid $border-color;
}

.line:last-child {
  border-bottom: none;
}

.label {
  font-size: 22rpx;
  color: $text-secondary;
}

.value {
  font-size: 22rpx;
  color: $text-primary;
  font-weight: 600;
}

.mono {
  font-family: Menlo, Monaco, Consolas, monospace;
  font-weight: 500;
  font-size: 20rpx;
}

.status-paid {
  background: #ecfdf3;
  color: #15803d;
}

.status-pending {
  background: #fff7ed;
  color: #c2410c;
}

.mini-btn {
  margin-top: 10rpx;
  font-size: 22rpx;
  padding: 8rpx 16rpx;
}

.empty {
  font-size: 24rpx;
  color: $text-secondary;
  padding: 20rpx 0;
  text-align: center;
}
</style>
