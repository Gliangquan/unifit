<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="row-between">
        <view class="uf-section-title" style="margin: 0;">计划详情</view>
        <button class="uf-btn-secondary mini-btn" @click="goHistory">历史计划</button>
      </view>

      <view v-if="needStudentVerify">
        <view class="empty">当前学生认证状态：{{ verifyStatusText }}，认证通过后可查看计划。</view>
        <button class="uf-btn-primary" @click="goVerify">去完成认证</button>
      </view>

      <view v-else-if="!plan.planId" class="empty">
        暂无可展示的计划，请先到计划页生成。
      </view>

      <view v-else>
        <view class="summary-card">
          <view class="row-between line">
            <text class="label">计划编号</text>
            <text class="value">#{{ plan.planId }}</text>
          </view>
          <view class="row-between line">
            <text class="label">计划状态</text>
            <text :class="['uf-pill', statusClass(plan.status)]">{{ statusText(plan.status) }}</text>
          </view>
          <view class="row-between line">
            <text class="label">体测项目</text>
            <text class="value">{{ testItemLabel(plan.testItemCode) }}</text>
          </view>
          <view class="row-between line">
            <text class="label">计划分档</text>
            <text class="value">{{ scoreLevelLabel(plan.scoreLevel) }}</text>
          </view>
          <view class="row-between line">
            <text class="label">体能基础</text>
            <text class="value">{{ fitnessLevelLabel(plan.fitnessLevel) }}</text>
          </view>
          <view class="row-between line">
            <text class="label">器械条件</text>
            <text class="value">{{ equipmentTypeLabel(plan.equipmentType) }}</text>
          </view>
          <view class="row-between line">
            <text class="label">每周频率</text>
            <text class="value">每周 {{ plan.daysPerWeek || 0 }} 天</text>
          </view>
          <view class="row-between line">
            <text class="label">计划周期</text>
            <text class="value">{{ formatDate(plan.startDate) }} ~ {{ formatDate(plan.endDate) }}</text>
          </view>
          <view class="line">
            <view class="row-between">
              <text class="label">完成进度</text>
              <text class="value">{{ completedCount }}/{{ totalCount }}</text>
            </view>
            <view class="progress-track">
              <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
            </view>
          </view>
        </view>

        <view class="unlock-card">
          <view class="row-between line">
            <text class="label">计划权限</text>
            <text :class="['uf-pill', courseUnlock.unlocked ? 'status-completed' : 'status-archived']">
              {{ courseUnlock.unlocked ? '已开通' : '未开通' }}
            </text>
          </view>
          <view class="row-between line">
            <text class="label">开通方式</text>
            <text class="value">{{ unlockOrderDisplayText() }}</text>
          </view>
          <view class="row-between line">
            <text class="label">开通费用</text>
            <text class="value">¥{{ Number(courseUnlock.amount || 0).toFixed(2) }}</text>
          </view>
          <view class="row-between line" v-if="courseUnlock.paidAt">
            <text class="label">开通时间</text>
            <text class="value">{{ formatDateTime(courseUnlock.paidAt) }}</text>
          </view>
          <button
            v-if="!courseUnlock.unlocked"
            class="uf-btn-primary mini-btn"
            @click="goPurchasePlan"
          >
            去开通计划
          </button>
        </view>

        <view class="section-title">今日训练项</view>
        <view v-if="todayPlanItems.length" class="unlock-card">
          <view class="row-between line">
            <text class="label">今日进度</text>
            <text class="value">{{ completedTodayCount }}/{{ todayPlanItems.length }}</text>
          </view>
          <view v-if="pendingTodayItems.length">
            <view
              class="course-row"
              v-for="item in pendingTodayItems"
              :key="`pending_${item.id}`"
            >
              <view class="course-main">
                <view class="course-title">{{ item.exerciseName || ('动作#' + (item.exerciseId || '-')) }}</view>
                <view class="course-meta">{{ item.setsCount || 0 }}组 · {{ item.repsCount || 0 }}次 · {{ item.durationMinutes || 0 }}分钟</view>
                <view class="course-note" v-if="item.intensityNote">{{ item.intensityNote }}</view>
              </view>
              <view class="course-actions">
                <button
                  v-if="item.exerciseId"
                  class="uf-btn-secondary mini-btn"
                  @click="onCourseAction(item)"
                >
                  {{ isCourseLocked(item.courseIndex) ? '去开通计划' : '查看动作' }}
                </button>
                <button
                  v-if="canConfirmDone(item)"
                  class="uf-btn-primary mini-btn"
                  @click="markItemDone(item)"
                >确认完成</button>
                <text v-else class="uf-hint">{{ confirmDoneHint(item) }}</text>
              </view>
            </view>
          </view>
          <view v-else class="empty">今日训练项已全部完成。</view>
        </view>
        <view v-else class="empty">今天不是训练日，或当前计划暂无待训练动作。</view>

        <view v-if="doneTodayItems.length">
          <view class="section-title">今日已完成</view>
          <view class="group-card">
            <view class="course-row" v-for="item in doneTodayItems" :key="`done_${item.id}`">
              <view class="course-main">
                <view class="course-title">{{ item.exerciseName || ('动作#' + (item.exerciseId || '-')) }}</view>
                <view class="course-meta">{{ item.setsCount || 0 }}组 · {{ item.repsCount || 0 }}次 · {{ item.durationMinutes || 0 }}分钟</view>
                <view class="course-note" v-if="item.intensityNote">{{ item.intensityNote }}</view>
              </view>
              <text class="done-badge">已完成</text>
            </view>
          </view>
        </view>

        <view class="section-title">其余计划课程</view>
        <view v-if="otherPlanGroups.length" class="group-card overflow-visible">
          <view class="fold-header" @click="toggleOtherCourses">
            <view>
              <view class="course-title">课程总览</view>
              <view class="course-meta">剩余 {{ otherPlanGroups.length }} 个训练日，默认折叠展示</view>
            </view>
            <text class="fold-trigger">{{ otherCoursesExpanded ? '收起' : '展开' }}</text>
          </view>
          <view v-if="otherCoursesExpanded">
            <view class="group-card nested-group-card" v-for="group in otherPlanGroups" :key="group.key">
              <view class="group-title">第 {{ group.weekNo }} 周 · 第 {{ group.dayNo }} 训练日</view>
              <view class="course-row" v-for="item in group.rows" :key="`${group.key}_${item.courseIndex}`">
                <view class="course-main">
                  <view class="course-title">{{ item.exerciseName || ('动作#' + (item.exerciseId || '-')) }}</view>
                  <view class="course-meta">
                    {{ item.setsCount || 0 }}组 · {{ item.repsCount || 0 }}次 · {{ item.durationMinutes || 0 }}分钟
                  </view>
                  <view class="course-note" v-if="item.intensityNote">{{ item.intensityNote }}</view>
                  <view class="course-lock" v-if="isCourseLocked(item.courseIndex)">
                    当前账号未开通训练计划，开通后可查看动作
                  </view>
                </view>
                <view class="course-actions">
                  <text v-if="Number(item.completed) === 1" class="done-badge">已完成</text>
                  <button
                    v-else-if="item.exerciseId"
                    class="uf-btn-secondary mini-btn"
                    @click="onCourseAction(item)"
                  >
                    {{ isCourseLocked(item.courseIndex) ? '去开通计划' : '查看动作' }}
                  </button>
                </view>
              </view>
            </view>
          </view>
        </view>
        <view v-else class="empty">该计划暂无其他课程内容。</view>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'

export default {
  data() {
    return {
      user: {},
      planUnlocked: false,
      studentProfile: {},
      needStudentVerify: false,
      verifyStatusText: '未提交',
      testItemMap: {},
      plan: {},
      planIdParam: null,
      courseUnlock: {
        unlocked: false,
        status: 'none',
        amount: 19.9,
        paidAt: null
      },
      otherCoursesExpanded: false
    }
  },
  computed: {
    totalCount() {
      return (this.plan.items || []).length
    },
    completedCount() {
      return (this.plan.items || []).filter(item => Number(item.completed) === 1).length
    },
    progressPercent() {
      if (!this.totalCount) return 0
      return Math.round((this.completedCount / this.totalCount) * 100)
    },
    groupedCourses() {
      const rows = this.plan.items || []
      const indexedRows = rows.map((item, index) => ({
        ...item,
        courseIndex: index
      }))
      const map = {}
      indexedRows.forEach(item => {
        const weekNo = Number(item.weekNo || 0)
        const dayNo = Number(item.dayNo || 0)
        const key = `${weekNo}-${dayNo}`
        if (!map[key]) {
          map[key] = { key, weekNo, dayNo, rows: [] }
        }
        map[key].rows.push(item)
      })
      return Object.values(map).sort((a, b) => {
        if (a.weekNo !== b.weekNo) return a.weekNo - b.weekNo
        return a.dayNo - b.dayNo
      })
    },
    todayTrainingDayNo() {
      const day = new Date().getDay()
      return day === 0 ? 7 : day
    },
    currentWeekNo() {
      if (!this.plan.startDate) return 1
      const start = new Date(this.plan.startDate)
      const now = new Date()
      const diffDays = Math.floor((now.getTime() - start.getTime()) / (24 * 3600 * 1000))
      if (Number.isNaN(diffDays) || diffDays < 0) return 1
      return Math.min(4, Math.max(1, Math.floor(diffDays / 7) + 1))
    },
    todayPlanItems() {
      const rows = this.plan.items || []
      const exactRows = rows.filter(item => Number(item.weekNo) === this.currentWeekNo && Number(item.dayNo) === this.todayTrainingDayNo)
      if (exactRows.length) {
        return exactRows
      }
      return rows.filter(item => Number(item.dayNo) === this.todayTrainingDayNo)
    },
    pendingTodayItems() {
      return this.todayPlanItems.filter(item => Number(item.completed) !== 1)
    },
    doneTodayItems() {
      return this.todayPlanItems.filter(item => Number(item.completed) === 1)
    },
    otherPlanGroups() {
      const todayIds = new Set(this.todayPlanItems.map(item => item.id))
      return this.groupedCourses
        .map(group => ({
          ...group,
          rows: group.rows.filter(item => !todayIds.has(item.id))
        }))
        .filter(group => group.rows.length)
    },
    completedTodayCount() {
      return this.doneTodayItems.length
    }
  },
  onLoad(options) {
    const id = Number((options && options.planId) || 0)
    this.planIdParam = id > 0 ? id : null
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    const verified = await this.loadStudentVerifyStatus()
    if (!verified) {
      this.plan = {}
      return
    }
    await this.loadTestItems()
    await this.loadPlanDetail()
    this.loadCourseUnlockState()
  },
  methods: {
    async loadCurrentUser() {
      const latestUser = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = { ...localUser, ...latestUser, token: localUser.token }
      this.planUnlocked = Number(this.user.planUnlocked || 0) === 1
      setUser(this.user)
    },
    async loadStudentVerifyStatus() {
      const profile = await request({ url: '/student/profile/my', showError: false }).catch(() => null)
      this.studentProfile = profile || {}
      this.needStudentVerify = !profile || profile.verificationStatus !== 'approved'
      const map = {
        pending: '待审核',
        approved: '已通过',
        rejected: '已拒绝'
      }
      this.verifyStatusText = map[this.studentProfile.verificationStatus] || '未提交'
      return !this.needStudentVerify
    },
    async loadTestItems() {
      const rows = await request({ url: '/test/items', showError: false }).catch(() => []) || []
      const map = {}
      rows.forEach(item => {
        map[item.itemCode] = item.itemName
      })
      this.testItemMap = map
    },
    async loadPlanDetail() {
      if (this.planIdParam) {
        const list = await request({ url: '/plan/list', showError: false }).catch(() => []) || []
        const found = list.find(row => Number(row.planId) === Number(this.planIdParam))
        this.plan = found || {}
        if (!found) {
          const current = await request({ url: '/plan/current', showError: false }).catch(() => null) || {}
          this.plan = current
        }
        return
      }
      this.plan = await request({ url: '/plan/current', showError: false }).catch(() => null) || {}
    },
    loadCourseUnlockState() {
      const unlocked = Number(this.user.planUnlocked || 0) === 1
      const unlockTime = this.user.planUnlockTime || null
      this.courseUnlock = {
        unlocked,
        status: unlocked ? 'paid' : 'none',
        amount: 19.9,
        paidAt: unlockTime
      }
    },
    unlockOrderDisplayText() {
      return this.courseUnlock.unlocked ? '后台统一开通' : '需先购买方案'
    },
    isCourseLocked(courseIndex) {
      return !this.courseUnlock.unlocked
    },
    goPurchasePlan() {
      uni.showToast({ title: '请返回计划页先开通方案', icon: 'none' })
      setTimeout(() => {
        this.go('/pages/plan/plan')
      }, 300)
    },
    async onCourseAction(item) {
      if (!item || !item.exerciseId) {
        return
      }
      if (this.isCourseLocked(item.courseIndex)) {
        await this.goPurchasePlan()
        return
      }
      this.goExercise(item.exerciseId)
    },
    testItemLabel(code) {
      if (!code) return '-'
      return this.testItemMap[code] || '未知项目'
    },
    scoreLevelLabel(level) {
      const map = {
        beginner: '基础档',
        intermediate: '提升档',
        advanced: '强化档'
      }
      return map[level] || '未知分档'
    },
    fitnessLevelLabel(level) {
      const map = {
        newbie: '入门',
        beginner: '入门',
        basic: '基础',
        intermediate: '基础',
        advanced: '进阶'
      }
      return map[level] || level || '-'
    },
    equipmentTypeLabel(type) {
      const map = {
        bodyweight: '无器械',
        dorm: '宿舍器械',
        dorm_equipment: '宿舍器械',
        track: '跑道',
        gym: '健身房',
        mixed: '综合'
      }
      return map[type] || type || '-'
    },
    formatDate(v) {
      if (!v) return '--'
      return String(v).slice(0, 10)
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
    statusText(status) {
      if (status === 'completed') return '已完成'
      if (status === 'archived') return '已归档'
      return '进行中'
    },
    statusClass(status) {
      if (status === 'completed') return 'status-completed'
      if (status === 'archived') return 'status-archived'
      return 'status-active'
    },
    canConfirmDone(item) {
      return !!(item && item.id && this.planUnlocked && !this.isCourseLocked(item.courseIndex) && this.isTodayPlanItem(item) && Number(item.completed) !== 1)
    },
    isTodayPlanItem(item) {
      return !!item && this.todayPlanItems.some(row => row.id === item.id)
    },
    isToday(value) {
      if (!value) return false
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return false
      const now = new Date()
      return date.getFullYear() === now.getFullYear()
        && date.getMonth() === now.getMonth()
        && date.getDate() === now.getDate()
    },
    confirmDoneHint(item) {
      if (!this.planUnlocked) return '需先开通计划'
      if (this.isCourseLocked(item.courseIndex)) return '未开通不可完成'
      if (!this.isTodayPlanItem(item)) return '请在今日训练项中完成'
      if (Number(item.completed) === 1) return '已完成'
      return ''
    },
    toggleOtherCourses() {
      this.otherCoursesExpanded = !this.otherCoursesExpanded
    },
    async markItemDone(item) {
      if (!this.canConfirmDone(item) || Number(item.completed) === 1) {
        return
      }
      await request({
        url: `/plan/item/done?planItemId=${item.id}`,
        method: 'POST'
      })
      uni.showToast({ title: '已记录完成', icon: 'success' })
      await this.loadPlanDetail()
      this.loadCourseUnlockState()
    },
    goExercise(id) {
      uni.navigateTo({ url: `/pages/exercise/detail?id=${id}` })
    },
    goHistory() {
      uni.navigateTo({ url: '/pages/plan/history' })
    },
    goVerify() {
      uni.navigateTo({ url: '/pages/mine/student-verify' })
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

.summary-card {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: #f9fafb;
  padding: 14rpx;
  margin-bottom: 16rpx;
}

.unlock-card {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: #f8fafc;
  padding: 14rpx;
  margin-bottom: 16rpx;
}

.line {
  padding: 8rpx 0;
  border-bottom: 1px solid $border-color;
}

.line:last-child {
  border-bottom: none;
}

.label {
  color: $text-secondary;
  font-size: 23rpx;
}

.value {
  color: $text-primary;
  font-size: 23rpx;
  font-weight: 600;
}

.section-title {
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
  margin: 10rpx 0 12rpx;
}

.progress-track {
  margin-top: 8rpx;
  height: 8rpx;
  background: #e2e8f0;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: $primary-color;
}

.group-card {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  background: #fff;
  margin-bottom: 12rpx;
  overflow: hidden;
}

.overflow-visible {
  overflow: visible;
}

.nested-group-card {
  margin: 12rpx 14rpx 14rpx;
}

.group-title {
  padding: 12rpx 14rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: $text-primary;
  background: #f8fafc;
  border-bottom: 1px solid $border-color;
}

.fold-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 18rpx;
}

.fold-trigger {
  font-size: 24rpx;
  color: $primary-color;
  font-weight: 600;
}

.course-row {
  padding: 12rpx 14rpx;
  display: flex;
  gap: 12rpx;
  align-items: center;
  border-bottom: 1px solid $border-color;
}

.course-row:last-child {
  border-bottom: none;
}

.course-main {
  flex: 1;
}

.course-actions {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  align-items: flex-end;
}

.course-title {
  font-size: 24rpx;
  color: $text-primary;
  font-weight: 600;
}

.course-meta {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.course-note {
  margin-top: 4rpx;
  font-size: 22rpx;
  color: $text-muted;
}

.course-lock {
  margin-top: 6rpx;
  font-size: 21rpx;
  color: #b45309;
}

.done-badge {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #ecfdf3;
  color: #15803d;
  font-size: 22rpx;
  font-weight: 600;
}

.mini-btn {
  padding: 8rpx 16rpx;
  font-size: 22rpx;
}

.status-active {
  background: #eef2ff;
  color: #4f46e5;
}

.status-completed {
  background: #ecfdf3;
  color: #15803d;
}

.status-archived {
  background: #f3f4f6;
  color: #6b7280;
}

.empty {
  color: $text-secondary;
  font-size: 24rpx;
  padding: 12rpx 0;
}

.uf-hint {
  font-size: 22rpx;
  color: #9ca3af;
}
</style>
