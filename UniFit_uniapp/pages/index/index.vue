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
        <view class="stat-number">{{ unlockedBadgeCount }}</view>
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
      <view class="action-grid">
        <view
          class="action-btn"
          v-for="item in quickActions"
          :key="item.text"
          @click="go(item.url)"
        >
          <view class="action-icon-badge" :style="{ background: item.bg }">
            <text class="action-icon-emoji">{{ item.icon }}</text>
          </view>
          <text class="action-text">{{ item.text }}</text>
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
          <text class="plan-label">体能基础</text>
          <text class="plan-value">{{ fitnessLevelLabel(currentPlan.fitnessLevel) }}</text>
        </view>
        <view class="plan-row">
          <text class="plan-label">器械条件</text>
          <text class="plan-value">{{ equipmentTypeLabel(currentPlan.equipmentType) }}</text>
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

    <view v-if="badgePopupVisible" class="badge-mask" @click="closeBadgeSummary">
      <view class="badge-popup" @click.stop>
        <view class="badge-popup-header">
          <view>
            <view class="badge-popup-title">我的徽章墙</view>
            <view class="badge-popup-subtitle">灰色未解锁，高亮为已解锁</view>
          </view>
          <text class="badge-popup-close" @click="closeBadgeSummary">×</text>
        </view>

        <view v-if="badgeWall.length" class="badge-grid">
          <view
            v-for="item in badgeWall"
            :key="item.id"
            :class="['badge-item', item.unlocked ? 'badge-item-unlocked' : 'badge-item-locked', selectedBadge && selectedBadge.id === item.id ? 'badge-item-active' : '']"
            @click="selectBadge(item)"
          >
            <view class="badge-icon-wrap">
              <text class="badge-icon-text">{{ badgeGlyph(item) }}</text>
            </view>
            <text class="badge-name">{{ item.badgeName }}</text>
            <text class="badge-state">{{ item.unlocked ? '已解锁' : '未解锁' }}</text>
          </view>
        </view>
        <view v-else class="empty-box" style="margin: 0;">当前还没有徽章定义。</view>

        <view v-if="selectedBadge" class="badge-detail-card">
          <view class="badge-detail-top">
            <view :class="['badge-detail-icon', selectedBadge.unlocked ? 'badge-detail-icon-unlocked' : 'badge-detail-icon-locked']">
              <text class="badge-detail-icon-text">{{ badgeGlyph(selectedBadge) }}</text>
            </view>
            <view class="badge-detail-main">
              <view class="badge-detail-name">{{ selectedBadge.badgeName }}</view>
              <view class="badge-detail-desc">{{ badgeConditionText(selectedBadge) }}</view>
              <view class="badge-detail-time" v-if="selectedBadge.achievedDate">解锁时间：{{ formatBadgeDate(selectedBadge.achievedDate) }}</view>
              <view class="badge-detail-time" v-else>达成后将自动点亮该徽章</view>
            </view>
          </view>
          <view class="badge-share-row">
            <button class="badge-share-btn badge-share-btn-primary" @click="shareBadge('moments')">分享到朋友圈</button>
            <button class="badge-share-btn" @click="shareBadge('feed')">分享到动态</button>
          </view>
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
      badgeWall: [],
      streak: 0,
      currentPlan: {},
      testItemNameMap: {},
      studentProfile: {},
      needStudentVerify: false,
      badgePopupVisible: false,
      selectedBadge: null,
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
    },
    unlockedBadgeCount() {
      return (this.badgeWall || []).filter(item => item.unlocked).length
    },
    quickActions() {
      return this.isAdminRole
        ? [
            { text: '我的', url: '/pages/mine/mine', icon: '👤', bg: 'linear-gradient(135deg, #fff7ed 0%, #fed7aa 100%)' },
            { text: '看板', url: '/pages/admin/dashboard', icon: '📊', bg: 'linear-gradient(135deg, #eff6ff 0%, #bfdbfe 100%)' },
            { text: '审核', url: '/pages/admin/students', icon: '✅', bg: 'linear-gradient(135deg, #ecfdf5 0%, #bbf7d0 100%)' },
            { text: '回复', url: '/pages/admin/messages', icon: '💬', bg: 'linear-gradient(135deg, #f5f3ff 0%, #ddd6fe 100%)' }
          ]
        : [
            { text: '我的', url: '/pages/mine/mine', icon: '👤', bg: 'linear-gradient(135deg, #fff7ed 0%, #fed7aa 100%)' },
            { text: '打卡', url: '/pages/checkin/checkin', icon: '✅', bg: 'linear-gradient(135deg, #ecfdf5 0%, #bbf7d0 100%)' },
            { text: '体测', url: '/pages/test/test', icon: '📈', bg: 'linear-gradient(135deg, #eff6ff 0%, #bfdbfe 100%)' },
            { text: '动作库', url: '/pages/exercise/list', icon: '🏋️', bg: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 100%)' },
            { text: '我的互动', url: '/pages/exercise/my', icon: '👍', bg: 'linear-gradient(135deg, #fae8ff 0%, #e9d5ff 100%)' },
            { text: '历史计划', url: '/pages/plan/history', icon: '📅', bg: 'linear-gradient(135deg, #ecfeff 0%, #a5f3fc 100%)' },
            { text: '留言', url: '/pages/message/message', icon: '💬', bg: 'linear-gradient(135deg, #f3f4f6 0%, #d1d5db 100%)' }
          ]
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
      this.badgeWall = (badgeData && badgeData.badgeWall) || []
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
    badgeGlyph(item) {
      const row = item || {}
      const conditionType = row.conditionType || ''
      const code = String(row.badgeCode || '').toUpperCase()
      if (conditionType === 'streak_days' || code.includes('STR')) return '🔥'
      if (conditionType === 'checkin_count' || code.includes('CHK')) return '🏅'
      if (code.includes('VIP') || code.includes('STAR')) return '⭐'
      return '🎖️'
    },
    badgeConditionText(item) {
      if (!item) return '持续训练即可解锁更多徽章'
      const value = Number(item.conditionValue || 0)
      if (item.conditionType === 'streak_days') {
        return `连续打卡 ${value || 0} 天可解锁`
      }
      if (item.conditionType === 'checkin_count') {
        return `累计完成 ${value || 0} 次打卡可解锁`
      }
      return '完成对应训练目标后自动解锁'
    },
    formatBadgeDate(value) {
      if (!value) return '--'
      const d = new Date(value)
      if (Number.isNaN(d.getTime())) return '--'
      const y = d.getFullYear()
      const m = `${d.getMonth() + 1}`.padStart(2, '0')
      const day = `${d.getDate()}`.padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    openBadgeSummary() {
      this.badgePopupVisible = true
      const firstUnlocked = (this.badgeWall || []).find(item => item.unlocked)
      this.selectedBadge = firstUnlocked || (this.badgeWall || [])[0] || null
    },
    closeBadgeSummary() {
      this.badgePopupVisible = false
    },
    selectBadge(item) {
      this.selectedBadge = item || null
    },
    buildBadgeShareText(scene) {
      const badge = this.selectedBadge || {}
      const sceneText = scene === 'moments' ? '朋友圈' : '动态'
      const statusText = badge.unlocked ? '我已解锁' : '我正在冲刺'
      return `${statusText}「${badge.badgeName || '训练徽章'}」！${this.badgeConditionText(badge)}，快来 UniFit 和我一起坚持训练吧～（${sceneText}分享）`
    },
    shareBadge(scene) {
      if (!this.selectedBadge) return
      const shareText = this.buildBadgeShareText(scene)
      const shareUrl = typeof window !== 'undefined' && window.location ? window.location.href : ''
      if (typeof navigator !== 'undefined' && navigator.share) {
        navigator.share({
          title: `${this.selectedBadge.badgeName || '训练徽章'} - UniFit`,
          text: shareText,
          url: shareUrl
        }).then(() => {
          uni.showToast({ title: '分享成功', icon: 'success' })
        }).catch(() => {
          uni.setClipboardData({
            data: `${shareText}${shareUrl ? `\n${shareUrl}` : ''}`,
            success: () => uni.showToast({ title: '已复制分享文案', icon: 'success' })
          })
        })
        return
      }
      uni.setClipboardData({
        data: `${shareText}${shareUrl ? `\n${shareUrl}` : ''}`,
        success: () => uni.showToast({ title: '已复制分享文案', icon: 'success' })
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

.action-icon-badge {
  width: 68rpx;
  height: 68rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 1rpx 0 rgba(255, 255, 255, 0.7);
}

.action-icon-emoji {
  font-size: 34rpx;
  line-height: 1;
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

.badge-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.48);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 999;
}

.badge-popup {
  width: 100%;
  max-height: 84vh;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 24rpx;
  box-sizing: border-box;
  overflow-y: auto;
}

.badge-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.badge-popup-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-primary;
}

.badge-popup-subtitle {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.badge-popup-close {
  width: 48rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background: #f3f4f6;
  text-align: center;
  line-height: 48rpx;
  font-size: 32rpx;
  color: $text-secondary;
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.badge-item {
  border-radius: 20rpx;
  padding: 18rpx 12rpx;
  text-align: center;
  border: 2rpx solid transparent;
}

.badge-item-locked {
  background: linear-gradient(180deg, #f3f4f6 0%, #e5e7eb 100%);
  color: #9ca3af;
}

.badge-item-unlocked {
  background: linear-gradient(180deg, #fff7ed 0%, #fdba74 100%);
  color: #9a3412;
}

.badge-item-active {
  border-color: $primary-color;
  box-shadow: 0 8rpx 24rpx rgba(249, 115, 22, 0.16);
}

.badge-icon-wrap {
  width: 84rpx;
  height: 84rpx;
  margin: 0 auto 10rpx;
  border-radius: 42rpx;
  background: rgba(255, 255, 255, 0.72);
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-icon-text {
  font-size: 42rpx;
  line-height: 1;
}

.badge-name {
  display: block;
  font-size: 22rpx;
  font-weight: 600;
}

.badge-state {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
}

.badge-detail-card {
  margin-top: 22rpx;
  border-radius: 22rpx;
  background: #fff7ed;
  border: 1rpx solid #fed7aa;
  padding: 18rpx;
}

.badge-detail-top {
  display: flex;
  gap: 16rpx;
  align-items: center;
}

.badge-detail-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.badge-detail-icon-unlocked {
  background: linear-gradient(180deg, #fb923c 0%, #f97316 100%);
}

.badge-detail-icon-locked {
  background: linear-gradient(180deg, #d1d5db 0%, #9ca3af 100%);
}

.badge-detail-icon-text {
  font-size: 48rpx;
}

.badge-detail-main {
  flex: 1;
}

.badge-detail-name {
  font-size: 28rpx;
  font-weight: 700;
  color: $text-primary;
}

.badge-detail-desc,
.badge-detail-time {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-secondary;
  line-height: 1.5;
}

.badge-share-row {
  display: flex;
  gap: 12rpx;
  margin-top: 18rpx;
}

.badge-share-btn {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background: #fff;
  color: $primary-color;
  border: 1rpx solid #fdba74;
  font-size: 24rpx;
}

.badge-share-btn-primary {
  background: $primary-color;
  color: #fff;
}
</style>
