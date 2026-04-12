<template>
  <view class="uf-page">
    <!-- 顶部信息卡片 -->
    <view class="header-card">
      <view class="header-top">
        <view class="header-left">
          <view class="header-title">{{ user.userName || '用户' }}</view>
          <view class="header-subtitle">{{ roleLabel }} · {{ todayLabel }}</view>
        </view>
        <view class="header-greeting">{{ greetingText }}</view>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-grid">
      <view class="stat-box clickable-stat" v-if="!isAdminRole" @click="go('/pages/checkin/checkin')">
        <view class="stat-number">{{ streak }}</view>
        <view class="stat-text">连续打卡</view>
      </view>
      <view class="stat-box clickable-stat" v-if="!isAdminRole" @click="openBadgeSummary">
        <view class="stat-number">{{ badges.length }}</view>
        <view class="stat-text">我的徽章</view>
      </view>
      <view class="stat-box" v-if="isAdminRole">
        <view class="stat-number">{{ dashboard.studentCount }}</view>
        <view class="stat-text">学生总数</view>
      </view>
      <view class="stat-box" v-if="isAdminRole">
        <view class="stat-number">{{ dashboard.checkinLast7Days }}</view>
        <view class="stat-text">近7天打卡</view>
      </view>
    </view>

    <!-- 快捷操作 -->
    <view class="section">
      <view class="section-title">快捷操作</view>
      <view class="action-grid" v-if="!isAdminRole">
        <view class="action-btn" @click="go('/pages/mine/mine')">
          <image class="action-icon" src="/static/icon_uni/wode.png" mode="aspectFit" />
          <text class="action-text">我的</text>
        </view>
        <view class="action-btn" @click="go('/pages/checkin/checkin')">
          <image class="action-icon" src="/static/icon_uni/wancheng.png" mode="aspectFit" />
          <text class="action-text">打卡</text>
        </view>
        <view class="action-btn" @click="go('/pages/test/test')">
          <image class="action-icon" src="/static/icon_uni/shezhi.png" mode="aspectFit" />
          <text class="action-text">体测</text>
        </view>
        <view class="action-btn" @click="go('/pages/exercise/list')">
          <image class="action-icon" src="/static/icon_uni/fenlei.png" mode="aspectFit" />
          <text class="action-text">动作库</text>
        </view>
        <view class="action-btn" @click="go('/pages/exercise/my')">
          <image class="action-icon" src="/static/icon_uni/zan.png" mode="aspectFit" />
          <text class="action-text">我的互动</text>
        </view>
        <view class="action-btn" @click="go('/pages/plan/history')">
          <image class="action-icon" src="/static/icon_uni/rili.png" mode="aspectFit" />
          <text class="action-text">历史计划</text>
        </view>
        <view class="action-btn" @click="go('/pages/message/message')">
          <image class="action-icon" src="/static/icon_uni/xiaoxi.png" mode="aspectFit" />
          <text class="action-text">留言</text>
        </view>
      </view>
      <view class="action-grid" v-else>
        <view class="action-btn" @click="go('/pages/mine/mine')">
          <image class="action-icon" src="/static/icon_uni/wode.png" mode="aspectFit" />
          <text class="action-text">我的</text>
        </view>
        <view class="action-btn" @click="go('/pages/admin/dashboard')">
          <image class="action-icon" src="/static/icon_uni/shezhi.png" mode="aspectFit" />
          <text class="action-text">看板</text>
        </view>
        <view class="action-btn" @click="go('/pages/admin/students')">
          <image class="action-icon" src="/static/icon_uni/wancheng.png" mode="aspectFit" />
          <text class="action-text">审核</text>
        </view>
        <view class="action-btn" @click="go('/pages/admin/messages')">
          <image class="action-icon" src="/static/icon_uni/xiaoxi.png" mode="aspectFit" />
          <text class="action-text">回复</text>
        </view>
      </view>
    </view>

    <!-- 计划状态 -->
    <view class="section" v-if="!isAdminRole">
      <view class="section-title">当前计划</view>
      <view v-if="currentPlan.planId" class="plan-box clickable" @click="go(`/pages/plan/current?planId=${currentPlan.planId}`)">
        <view class="plan-row">
          <text class="plan-label">计划ID</text>
          <text class="plan-value">#{{ currentPlan.planId }}</text>
        </view>
        <view class="plan-row">
          <text class="plan-label">项目</text>
          <text class="plan-value">{{ testItemLabel(currentPlan.testItemCode) }}</text>
        </view>
        <view class="plan-row">
          <text class="plan-label">等级</text>
          <text class="plan-value">{{ scoreLevelLabel(currentPlan.scoreLevel) }}</text>
        </view>
        <view class="plan-row">
          <text class="plan-label">频率</text>
          <text class="plan-value">每周 {{ currentPlan.daysPerWeek || 0 }} 天</text>
        </view>
        <view class="empty-hint" style="margin-top: 8rpx;">点击查看计划详情与关联课程</view>
      </view>
      <view v-else-if="needStudentVerify" class="empty-box clickable" @click="go('/pages/mine/student-verify')">
        <text class="empty-text">认证状态：{{ verifyStatusText }}</text>
        <text class="empty-hint">点击前往认证后可使用计划功能</text>
      </view>
      <view v-else class="empty-box">
        <text class="empty-text">暂无计划</text>
        <text class="empty-hint">请到计划页生成</text>
      </view>
    </view>

    <!-- 待处理事项 -->
    <view class="section" v-if="isAdminRole">
      <view class="section-title">待处理</view>
      <view class="pending-box">
        <view class="pending-row">
          <text class="pending-label">待审核学生</text>
          <text class="pending-count">{{ pendingStudents }}</text>
        </view>
        <view class="pending-row">
          <text class="pending-label">待回复留言</text>
          <text class="pending-count">{{ pendingMessages }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'

const TAB_PAGES = [
  '/pages/index/index',
  '/pages/profile/profile',
  '/pages/plan/plan',
  '/pages/mine/mine'
]

export default {
  data() {
    return {
      user: {},
      badges: [],
      streak: 0,
      currentPlan: {},
      testItemNameMap: {},
      studentProfile: {},
      needStudentVerify: false,
      dashboard: {
        studentCount: 0,
        checkinLast7Days: 0
      },
      pendingStudents: 0,
      pendingMessages: 0
    }
  },
  computed: {
    isAdminRole() {
      return (this.user.userRole || 'student') === 'admin'
    },
    roleLabel() {
      return this.isAdminRole ? '管理员' : '学生'
    },
    greetingText() {
      const hour = new Date().getHours()
      if (hour < 12) return '上午好'
      if (hour < 18) return '下午好'
      return '晚上好'
    },
    focusText() {
      if (this.isAdminRole) return '关注待审核学生和留言'
      return '坚持训练，保持热情'
    },
    todayLabel() {
      const d = new Date()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${m}-${day}`
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
  },
  methods: {
    go(url) {
      if (TAB_PAGES.includes(url)) {
        uni.switchTab({ url })
        return
      }
      uni.navigateTo({ url })
    },
    async loadCurrentUser() {
      const latestUser = await request({ url: '/user/get/login', showError: false })
      if (!latestUser || !latestUser.id) {
        uni.redirectTo({ url: '/pages/login/login' })
        return
      }
      const localUser = uni.getStorageSync('user') || {}
      this.user = { ...localUser, ...latestUser, token: localUser.token }
      setUser(this.user)

      if (this.isAdminRole) {
        await this.loadAdminDashboard()
      } else {
        await this.loadStudentDashboard()
      }
    },
    async loadStudentDashboard() {
      const [badgeRes, streakRes, profileRes, testItemsRes] = await Promise.allSettled([
        request({ url: '/badge/my', showError: false }),
        request({ url: '/checkin/streak', showError: false }),
        request({ url: '/student/profile/my', showError: false }),
        request({ url: '/test/items', showError: false })
      ])
      const badgeData = badgeRes.status === 'fulfilled' ? badgeRes.value : null
      const streakData = streakRes.status === 'fulfilled' ? streakRes.value : null
      const profile = profileRes.status === 'fulfilled' ? profileRes.value : null
      const testItems = testItemsRes.status === 'fulfilled' ? (testItemsRes.value || []) : []

      this.badges = (badgeData && badgeData.badges) || []
      this.streak = streakData || 0
      this.studentProfile = profile || {}
      const map = {}
      testItems.forEach(item => {
        map[item.itemCode] = item.itemName
      })
      this.testItemNameMap = map
      this.needStudentVerify = !profile || profile.verificationStatus !== 'approved'

      if (this.needStudentVerify) {
        this.currentPlan = {}
        return
      }
      this.currentPlan = await request({ url: '/plan/current', showError: false }).catch(() => null) || {}
    },
    async loadAdminDashboard() {
      const [dashboard, students, messages] = await Promise.all([
        request({ url: '/admin/dashboard', showError: false }),
        request({ url: '/student/verify/pending', showError: false }),
        request({ url: '/message/pending', showError: false })
      ])
      this.dashboard = dashboard || { studentCount: 0, checkinLast7Days: 0 }
      this.pendingStudents = (students || []).length
      this.pendingMessages = (messages || []).length
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
    openBadgeSummary() {
      const names = (this.badges || []).map(item => item.badgeName).filter(Boolean)
      const content = names.length
        ? `已获得 ${this.badges.length} 枚徽章：${names.join('、')}`
        : '当前还没有获得徽章，先去坚持打卡吧。'
      uni.showModal({
        title: '我的徽章',
        content,
        showCancel: false,
        confirmText: '知道了'
      })
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.uf-page {
  min-height: 100vh;
  background: #f5f5f7;
  padding: 0;
}

// 顶部信息卡片
.header-card {
  background: #ffffff;
  padding: 24rpx;
  border-bottom: 1rpx solid $border-color;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
}

.header-subtitle {
  font-size: 22rpx;
  color: $text-secondary;
  margin-top: 6rpx;
}

.header-greeting {
  font-size: 26rpx;
  color: $primary-color;
  font-weight: 600;
}

// 统计数据
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
  padding: 16rpx;
  background: #ffffff;
  margin-bottom: 16rpx;
}

.stat-box {
  background: #f9fafb;
  border-radius: $radius-md;
  padding: 16rpx;
  text-align: center;
  border: 1rpx solid $border-color;
}

.clickable-stat {
  transition: all 0.2s ease;

  &:active {
    background: #f0f0f0;
    transform: scale(0.98);
  }
}

.stat-number {
  font-size: 32rpx;
  font-weight: 700;
  color: $primary-color;
  line-height: 1;
}

.stat-text {
  font-size: 22rpx;
  color: $text-secondary;
  margin-top: 8rpx;
}

// 分组
.section {
  background: #ffffff;
  margin: 0 16rpx 16rpx;
  border-radius: $radius-md;
  padding: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 16rpx;
  display: block;
}

// 快捷操作网格
.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx;
  border-radius: $radius-md;
  background: #f9fafb;
  border: 1rpx solid $border-color;
  transition: all 0.2s ease;

  &:active {
    background: #f0f0f0;
    transform: scale(0.95);
  }
}

.action-icon {
  width: 40rpx;
  height: 40rpx;
}

.action-text {
  font-size: 20rpx;
  color: $text-primary;
  font-weight: 500;
  text-align: center;
}

// 计划卡片
.plan-box {
  background: #f9fafb;
  border-radius: $radius-md;
  padding: 16rpx;
  border: 1rpx solid $border-color;
}

.plan-box.clickable {
  transition: all 0.2s ease;

  &:active {
    background: #f0f0f0;
  }
}

.plan-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 0;
  font-size: 24rpx;

  &:not(:last-child) {
    border-bottom: 1rpx solid $border-color;
    padding-bottom: 12rpx;
    margin-bottom: 12rpx;
  }
}

.plan-label {
  color: $text-secondary;
  font-weight: 500;
}

.plan-value {
  color: $text-primary;
  font-weight: 600;
}

// 空状态
.empty-box {
  background: #f9fafb;
  border-radius: $radius-md;
  padding: 24rpx;
  text-align: center;
  border: 1rpx solid $border-color;

  &.clickable {
    cursor: pointer;
    transition: all 0.2s ease;

    &:active {
      background: #f0f0f0;
    }
  }
}

.empty-text {
  display: block;
  font-size: 24rpx;
  color: $text-primary;
  font-weight: 500;
  margin-bottom: 6rpx;
}

.empty-hint {
  display: block;
  font-size: 22rpx;
  color: $text-secondary;
}

// 待处理
.pending-box {
  background: #f9fafb;
  border-radius: $radius-md;
  padding: 0;
  border: 1rpx solid $border-color;
  overflow: hidden;
}

.pending-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14rpx 16rpx;
  border-bottom: 1rpx solid $border-color;

  &:last-child {
    border-bottom: 0;
  }
}

.pending-label {
  font-size: 24rpx;
  color: $text-secondary;
}

.pending-count {
  font-size: 28rpx;
  font-weight: 700;
  color: $primary-color;
}
</style>
