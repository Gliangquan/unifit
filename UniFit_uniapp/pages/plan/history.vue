<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="row-between">
        <view class="uf-section-title" style="margin: 0;">历史计划</view>
        <text class="hint">{{ plans.length }} 条</text>
      </view>

      <view v-if="needStudentVerify">
        <view class="empty">当前学生认证状态：{{ verifyStatusText }}，认证通过后可查看历史计划。</view>
        <button class="uf-btn-primary" @click="goVerify">去完成认证</button>
      </view>
      <view v-else-if="plans.length">
        <view class="plan-row" v-for="plan in plans" :key="plan.planId || plan.id" @click="goDetail(plan)">
          <view class="row-between" style="margin-bottom: 8rpx;">
            <view class="plan-title">计划 #{{ plan.planId || plan.id }}</view>
            <view :class="['uf-pill', statusClass(plan.status)]">{{ statusText(plan.status) }}</view>
          </view>
          <view class="plan-sub">项目：{{ testItemLabel(plan.testItemCode) }} · 等级：{{ scoreLevelLabel(plan.scoreLevel) }}</view>
          <view class="plan-sub">基础：{{ fitnessLevelLabel(plan.fitnessLevel) }} · 器械：{{ equipmentTypeLabel(plan.equipmentType) }}</view>
          <view class="plan-sub">周期：{{ formatDate(plan.startDate) }} ~ {{ formatDate(plan.endDate) }}</view>
          <view class="plan-sub">频率：每周 {{ plan.daysPerWeek || 0 }} 天 · 完成度：{{ progress(plan) }}</view>
          <view class="detail-tip">点击查看计划详情与关联课程</view>
        </view>
      </view>
      <view v-else class="empty">暂无历史计划。</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'

export default {
  data() {
    return {
      plans: [],
      user: {},
      studentProfile: {},
      needStudentVerify: false,
      testItemNameMap: {}
    }
  },
  computed: {
    isAdminRole() {
      return (this.user.userRole || 'student') === 'admin'
    },
    verifyStatusText() {
      const map = {
        pending: '待审核',
        approved: '已通过',
        rejected: '已拒绝'
      }
      return map[this.studentProfile.verificationStatus] || '未提交'
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    await this.loadTestItems()
    if (!this.isAdminRole) {
      const verified = await this.loadStudentVerifyStatus()
      if (!verified) {
        this.plans = []
        return
      }
    }
    await this.loadPlans()
  },
  methods: {
    async loadCurrentUser() {
      const latestUser = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = { ...localUser, ...latestUser, token: localUser.token }
      setUser(this.user)
    },
    async loadStudentVerifyStatus() {
      const profile = await request({ url: '/student/profile/my', showError: false }).catch(() => null)
      this.studentProfile = profile || {}
      this.needStudentVerify = !profile || profile.verificationStatus !== 'approved'
      return !this.needStudentVerify
    },
    async loadPlans() {
      const data = await request({ url: '/plan/list', showError: false }).catch(() => null) || []
      this.plans = data.slice().sort((a, b) => {
        const ta = new Date(a.startDate || 0).getTime()
        const tb = new Date(b.startDate || 0).getTime()
        return tb - ta
      })
    },
    async loadTestItems() {
      const rows = await request({ url: '/test/items', showError: false }).catch(() => []) || []
      const map = {}
      rows.forEach(item => {
        map[item.itemCode] = item.itemName
      })
      this.testItemNameMap = map
    },
    goVerify() {
      uni.navigateTo({ url: '/pages/mine/student-verify' })
    },
    goDetail(plan) {
      const id = Number((plan && (plan.planId || plan.id)) || 0)
      if (!id) return
      uni.navigateTo({ url: `/pages/plan/current?planId=${id}` })
    },
    progress(plan) {
      const items = plan.items || []
      if (!items.length) return '0/0'
      const done = items.filter(item => item.completed).length
      return `${done}/${items.length}`
    },
    formatDate(v) {
      if (!v) return '--'
      return String(v).slice(0, 10)
    },
    testItemLabel(code) {
      if (!code) return '-'
      return this.testItemNameMap[code] || '未知项目'
    },
    scoreLevelLabel(level) {
      const map = {
        beginner: '初级',
        intermediate: '中级',
        advanced: '高级'
      }
      return map[level] || '未知等级'
    },
    fitnessLevelLabel(level) {
      const map = {
        newbie: '初级',
        beginner: '初级',
        basic: '中级',
        intermediate: '中级',
        advanced: '高级'
      }
      return map[level] || level || '-'
    },
    equipmentTypeLabel(type) {
      const map = {
        bodyweight: '无器械',
        track: '跑道',
        gym: '健身房'
      }
      return map[type] || type || '-'
    },
    statusText(status) {
      const map = {
        active: '进行中',
        completed: '已完成',
        archived: '已结束'
      }
      return map[status] || '未知状态'
    },
    statusClass(status) {
      if (status === 'completed') return 'status-completed'
      if (status === 'archived') return 'status-archived'
      return 'status-active'
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.hint {
  color: $text-secondary;
  font-size: 22rpx;
}

.plan-row {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: 14rpx;
  margin-bottom: 10rpx;
  background: #fff;
}

.detail-tip {
  margin-top: 8rpx;
  color: $text-muted;
  font-size: 21rpx;
}

.plan-title {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 600;
}

.plan-sub {
  margin-top: 6rpx;
  color: $text-secondary;
  font-size: 22rpx;
}

.status-active {
  background: #e8f4ff;
  color: #2563eb;
}

.status-completed {
  background: #e8f8ee;
  color: #15803d;
}

.status-archived {
  background: #f3f4f6;
  color: #6b7280;
}

.empty {
  color: $text-secondary;
  font-size: 24rpx;
}
</style>
