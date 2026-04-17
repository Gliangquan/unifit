<template>
  <view class="page-container">
    <!-- 管理员视图 -->
    <template v-if="isAdminRole">
      <view class="section-container">
        <view class="section-header">训练资源管理</view>
        <uni-list :border="false" class="custom-list">
          <uni-list-item title="计划模板管理" showArrow clickable @click="go('/pages/admin/templates')">
            <template v-slot:header>
              <uni-icons type="list" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
          </uni-list-item>
          <uni-list-item title="动作库管理" showArrow clickable @click="go('/pages/admin/exercises')">
            <template v-slot:header>
              <uni-icons type="compose" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
          </uni-list-item>
          <uni-list-item title="体测标准管理" showArrow clickable @click="go('/pages/admin/standards')">
            <template v-slot:header>
              <uni-icons type="bars" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
          </uni-list-item>
          <uni-list-item title="留言回复" showArrow clickable @click="go('/pages/admin/messages')">
            <template v-slot:header>
              <uni-icons type="chat" size="20" color="#64748b" style="margin-right: 20rpx;"></uni-icons>
            </template>
          </uni-list-item>
        </uni-list>
      </view>

      <view class="section-container">
        <view class="section-header">资源统计</view>
        <view class="stat-grid">
          <view class="stat-card">
            <view class="stat-label">模板数</view>
            <view class="stat-value">{{ adminOverview.templateTotal }}</view>
          </view>
          <view class="stat-card">
            <view class="stat-label">动作数</view>
            <view class="stat-value">{{ adminOverview.exerciseTotal }}</view>
          </view>
          <view class="stat-card">
            <view class="stat-label">标准条目</view>
            <view class="stat-value">{{ adminOverview.standardTotal }}</view>
          </view>
          <view class="stat-card">
            <view class="stat-label">待回复留言</view>
            <view class="stat-value">{{ adminOverview.pendingMessages }}</view>
          </view>
        </view>
      </view>
    </template>

    <!-- 学生视图 -->
    <template v-else>
      <!-- 未认证提示 -->
      <view v-if="needStudentVerify" class="section-container">
        <view class="section-header">需要完成学生认证</view>
        <view class="verify-card">
          <view class="verify-status">
            <uni-icons type="info" size="24" color="#f59e0b"></uni-icons>
            <text class="status-text">当前状态：{{ verifyStatusText }}</text>
          </view>
          <button class="btn-primary" @click="go('/pages/mine/student-verify')">前往认证</button>
        </view>
      </view>

      <!-- 进行中的计划显示 -->
      <view v-if="!showGenerateForm && currentPlan" class="section-container">
        <view class="section-header">当前训练计划</view>
        <view class="plan-card">
          <view class="plan-header">
            <view class="plan-title">{{ currentPlan.testItemName }}</view>
            <view class="plan-status">进行中</view>
          </view>
          <view class="plan-info">
            <view class="info-row">
              <text class="info-label">目标成绩</text>
              <text class="info-value">{{ currentPlan.targetScore }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">当前成绩</text>
              <text class="info-value">{{ currentPlan.currentScore }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">训练周期</text>
              <text class="info-value">{{ currentPlan.daysPerWeek }}天/周</text>
            </view>
            <view class="info-row">
              <text class="info-label">体能基础</text>
              <text class="info-value">{{ currentPlan.fitnessLevelLabel }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">器械条件</text>
              <text class="info-value">{{ currentPlan.equipmentTypeLabel }}</text>
            </view>
          </view>
          <view class="plan-actions">
            <button class="btn-secondary" @click="viewPlanDetails">查看详情</button>
            <button class="btn-primary" @click="showGenerateForm = true">重新生成计划</button>
          </view>
        </view>
      </view>

      <!-- 计划生成表单 -->
      <view v-else-if="showGenerateForm" class="section-container">
        <view class="section-header">生成训练计划</view>

        <!-- 训练项目选择 -->
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="bars" size="18" color="#64748b"></uni-icons>
            <text>训练项目</text>
          </view>
          <picker class="form-picker" :range="testItemOptions" range-key="itemName" @change="onTestItemChange">
            <view class="picker-value">{{ selectedTestItem || '请选择项目' }}</view>
          </picker>
        </view>

        <!-- 当前成绩 -->
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="medal" size="18" color="#64748b"></uni-icons>
            <text>当前成绩</text>
          </view>
          <input class="form-input" v-model="form.currentScore" type="digit" :placeholder="currentScorePlaceholder" />
          <view class="field-hint" v-if="currentTestItemUnit">单位：{{ currentTestItemUnit }}</view>
        </view>

        <!-- 体能基础 + 器械条件 -->
        <view class="form-row">
          <view class="form-item form-item-half">
            <view class="form-label">
              <uni-icons type="heart" size="18" color="#64748b"></uni-icons>
              <text>体能基础</text>
            </view>
            <picker class="form-picker" :range="fitnessLevelOptions" range-key="label" @change="onFitnessChange">
              <view class="picker-value">{{ form.fitnessLevelLabel || '请选择' }}</view>
            </picker>
          </view>

          <view class="form-item form-item-half">
            <view class="form-label">
              <uni-icons type="settings" size="18" color="#64748b"></uni-icons>
              <text>器械条件</text>
            </view>
            <picker class="form-picker" :range="equipmentTypeOptions" range-key="label" @change="onEquipmentChange">
              <view class="picker-value">{{ form.equipmentTypeLabel || '请选择' }}</view>
            </picker>
          </view>
        </view>

        <!-- 每周训练天数 -->
        <view class="form-item">
          <view class="form-label">
            <uni-icons type="calendar" size="18" color="#64748b"></uni-icons>
            <text>每周训练天数</text>
          </view>
          <input class="form-input" v-model="form.daysPerWeek" type="number" placeholder="请输入天数（1-7）" />
        </view>

        <button class="btn-primary btn-full" @click="generate">生成训练计划</button>
      </view>
    </template>
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
      adminOverview: {
        templateTotal: 0,
        exerciseTotal: 0,
        standardTotal: 0,
        pendingMessages: 0
      },
      testItemOptions: [],
      fitnessLevelOptions: [
        { value: 'newbie', label: '初级' },
        { value: 'basic', label: '中级' },
        { value: 'advanced', label: '高级' }
      ],
      equipmentTypeOptions: [
        { value: 'bodyweight', label: '无器械' },
        { value: 'track', label: '跑道' },
        { value: 'gym', label: '健身房' }
      ],
      form: {
        testItemCode: '',
        currentScore: '',
        fitnessLevel: 'newbie',
        fitnessLevelLabel: '初级',
        equipmentType: 'bodyweight',
        equipmentTypeLabel: '无器械',
        daysPerWeek: '3',
        bmiValue: null
      },
      selectedTestItem: '',
      studentProfile: {},
      needStudentVerify: false,
      showGenerateForm: false,
      currentPlan: null
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
    },
    currentTestItem() {
      return this.testItemOptions.find(item => item.itemCode === this.form.testItemCode) || null
    },
    currentTestItemUnit() {
      return (this.currentTestItem && this.currentTestItem.scoreUnit) || ''
    },
    currentScorePlaceholder() {
      if (!this.currentTestItemUnit) return '请输入当前成绩'
      return `请输入当前成绩（${this.currentTestItemUnit}）`
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    if (this.isAdminRole) {
      await this.loadAdminOverview()
      return
    }
    const verified = await this.loadStudentVerifyStatus()
    await Promise.allSettled([this.loadTestItems(), this.loadHealthBmi(), this.loadCurrentPlan()])
    if (!verified) return
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
      const latestUser = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = { ...localUser, ...latestUser, token: localUser.token }
      setUser(this.user)
    },
    async loadAdminOverview() {
      const [templatePage, exercisePage, standardPage, messageRows] = await Promise.all([
        request({ url: '/admin/template/list?current=1&pageSize=1', showError: false }),
        request({ url: '/exercise/list?current=1&pageSize=1', showError: false }),
        request({
          url: '/admin/standard/list/page',
          method: 'POST',
          data: { current: 1, pageSize: 1 },
          showError: false
        }),
        request({ url: '/message/pending', showError: false })
      ])
      this.adminOverview = {
        templateTotal: Number((templatePage && templatePage.total) || 0),
        exerciseTotal: Number((exercisePage && exercisePage.total) || 0),
        standardTotal: Number((standardPage && standardPage.total) || 0),
        pendingMessages: (messageRows || []).length
      }
    },
    async loadTestItems() {
      this.testItemOptions = await request({ url: '/test/items', showError: false }) || []
      if (!this.form.testItemCode && this.testItemOptions.length) {
        this.form.testItemCode = this.testItemOptions[0].itemCode
        this.selectedTestItem = this.testItemOptions[0].itemName
      }
      if (!this.form.currentScore && this.currentTestItemUnit) {
        this.form.currentScore = ''
      }
    },
    onTestItemChange(e) {
      const row = this.testItemOptions[Number(e.detail.value)]
      if (row) {
        this.form.testItemCode = row.itemCode
        this.selectedTestItem = row.itemName
        this.form.currentScore = ''
      }
    },
    onFitnessChange(e) {
      const row = this.fitnessLevelOptions[Number(e.detail.value)]
      if (row) {
        this.form.fitnessLevel = row.value
        this.form.fitnessLevelLabel = row.label
      }
    },
    onEquipmentChange(e) {
      const row = this.equipmentTypeOptions[Number(e.detail.value)]
      if (row) {
        this.form.equipmentType = row.value
        this.form.equipmentTypeLabel = row.label
      }
    },
    async loadHealthBmi() {
      const profile = await request({ url: '/health/profile/my', showError: false }).catch(() => null)
      if (!profile) {
        return
      }
      if (profile.bmiValue !== undefined && profile.bmiValue !== null && profile.bmiValue !== '') {
        this.form.bmiValue = Number(profile.bmiValue)
        return
      }
      const h = Number(profile.height || 0)
      const w = Number(profile.weight || 0)
      if (h > 0 && w > 0) {
        const bmi = w / ((h / 100) * (h / 100))
        this.form.bmiValue = Number(bmi.toFixed(2))
      }
    },
    async loadStudentVerifyStatus() {
      const profile = await request({ url: '/student/profile/my', showError: false }).catch(() => null)
      this.studentProfile = profile || {}
      this.needStudentVerify = !profile || profile.verificationStatus !== 'approved'
      return !this.needStudentVerify
    },
    ensureStudentVerifiedAction() {
      if (!this.needStudentVerify) {
        return false
      }
      uni.showModal({
        title: '需要先完成认证',
        content: `当前认证状态：${this.verifyStatusText}，通过后才可生成训练计划。`,
        confirmText: '去认证',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({ url: '/pages/mine/student-verify' })
          }
        }
      })
      return true
    },
    getGeneratePayload() {
      if (!this.form.testItemCode) {
        uni.showToast({ title: '请选择训练项目', icon: 'none' })
        return null
      }
      const currentScore = Number(this.form.currentScore)
      if (!Number.isFinite(currentScore) || currentScore < 0) {
        uni.showToast({ title: '请输入有效成绩', icon: 'none' })
        return null
      }
      const daysPerWeek = Number(this.form.daysPerWeek)
      if (!Number.isInteger(daysPerWeek) || daysPerWeek < 1 || daysPerWeek > 7) {
        uni.showToast({ title: '训练天数需在1-7之间', icon: 'none' })
        return null
      }
      let bmiValue = null
      if (this.form.bmiValue !== null && this.form.bmiValue !== '') {
        const parsed = Number(this.form.bmiValue)
        bmiValue = Number.isFinite(parsed) ? parsed : null
      }
      return {
        testItemCode: this.form.testItemCode,
        currentScore,
        fitnessLevel: this.form.fitnessLevel,
        equipmentType: this.form.equipmentType,
        daysPerWeek,
        bmiValue
      }
    },
    async generate() {
      if (this.ensureStudentVerifiedAction()) {
        return
      }
      const payload = this.getGeneratePayload()
      if (!payload) {
        return
      }
      try {
        let generatedPlan = await request({
          url: '/plan/generate',
          method: 'POST',
          showError: false,
          data: {
            testItemCode: payload.testItemCode,
            currentScore: Number(payload.currentScore),
            fitnessLevel: payload.fitnessLevel,
            equipmentType: payload.equipmentType,
            daysPerWeek: Number(payload.daysPerWeek),
            bmiValue: payload.bmiValue === null || payload.bmiValue === '' ? null : Number(payload.bmiValue)
          }
        })
        uni.showToast({ title: '计划已生成', icon: 'success' })
        setTimeout(() => {
          this.go(`/pages/plan/current?planId=${generatedPlan && generatedPlan.planId ? generatedPlan.planId : ''}`)
        }, 300)
      } catch (e) {
        const message = (e && e.message) || ''
        if (message.includes('购买') || message.includes('解锁')) {
          try {
            const purchaseResult = await request({ url: '/plan/purchase', method: 'POST', showError: false })
            const localUser = uni.getStorageSync('user') || {}
            uni.setStorageSync('user', {
              ...localUser,
              balance: purchaseResult && purchaseResult.balance !== undefined ? purchaseResult.balance : localUser.balance,
              planUnlocked: purchaseResult && purchaseResult.planUnlocked !== undefined ? purchaseResult.planUnlocked : localUser.planUnlocked
            })
            const generatedPlan = await request({
              url: '/plan/generate',
              method: 'POST',
              data: {
                testItemCode: payload.testItemCode,
                currentScore: Number(payload.currentScore),
                fitnessLevel: payload.fitnessLevel,
                equipmentType: payload.equipmentType,
                daysPerWeek: Number(payload.daysPerWeek),
                bmiValue: payload.bmiValue === null || payload.bmiValue === '' ? null : Number(payload.bmiValue)
              }
            })
            uni.showToast({ title: '计划已生成', icon: 'success' })
            setTimeout(() => {
              this.go(`/pages/plan/current?planId=${generatedPlan && generatedPlan.planId ? generatedPlan.planId : ''}`)
            }, 300)
          } catch (purchaseError) {
            uni.showToast({ title: (purchaseError && purchaseError.message) || '生成失败', icon: 'none' })
          }
          return
        }
        uni.showToast({ title: message || '生成失败', icon: 'none' })
      }
    },
    async loadCurrentPlan() {
      try {
        const plan = await request({ url: '/plan/current', showError: false })
        if (plan) {
          this.currentPlan = {
            ...plan,
            testItemName: this.testItemLabel(plan.testItemCode),
            targetScore: this.targetScoreText(plan.scoreLevel),
            currentScore: this.currentScoreText(plan),
            fitnessLevelLabel: this.fitnessLevelText(plan.fitnessLevel),
            equipmentTypeLabel: this.equipmentTypeText(plan.equipmentType)
          }
          this.showGenerateForm = false
        } else {
          this.currentPlan = null
          this.showGenerateForm = true
        }
      } catch (error) {
        this.currentPlan = null
        this.showGenerateForm = true
      }
    },
    viewPlanDetails() {
      if (this.currentPlan && this.currentPlan.planId) {
        this.go(`/pages/plan/current?planId=${this.currentPlan.planId}`)
      }
    },
    fitnessLevelText(value) {
      const map = {
        newbie: '初级',
        beginner: '初级',
        basic: '中级',
        intermediate: '中级',
        advanced: '高级'
      }
      return map[value] || value || '-'
    },
    equipmentTypeText(value) {
      const map = {
        bodyweight: '无器械',
        track: '跑道',
        gym: '健身房'
      }
      return map[value] || value || '-'
    },
    targetScoreText(scoreLevel) {
      const map = {
        beginner: '基础达标',
        intermediate: '稳定提升',
        advanced: '强化突破'
      }
      return map[scoreLevel] || '个性化目标'
    },
    currentScoreText(plan) {
      if (!plan) return '-'
      const unit = (this.testItemOptions.find(item => item.itemCode === plan.testItemCode) || {}).scoreUnit || ''
      const snapshot = plan.snapshot || {}
      const score = snapshot.currentScore
      if (score === undefined || score === null || score === '') {
        return unit ? `-- ${unit}` : '--'
      }
      return unit ? `${score} ${unit}` : String(score)
    }
  }
}
</script>

<style lang="scss" scoped>
@import "@/styles/common.scss";

.page-container {
  min-height: 100vh;
  background: $bg-page;
  padding: 16rpx 0 80rpx;
}

.section-container {
  margin: 0 16rpx 16rpx;
  background: #ffffff;
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
  background: transparent;
}

:deep(.uni-list-item__container) {
  padding: 22rpx 20rpx !important;
}

:deep(.uni-list-item__content-title) {
  font-size: 27rpx !important;
  color: $text-primary !important;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10rpx;
  padding: 12rpx 20rpx 20rpx;
}

.stat-card {
  background: #f9fafb;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  padding: 18rpx;
  text-align: center;
}

.stat-label {
  font-size: 22rpx;
  color: $text-secondary;
  margin-bottom: 10rpx;
}

.stat-value {
  font-size: 34rpx;
  font-weight: 700;
  color: $text-primary;
}

.verify-card {
  padding: 20rpx;
}

.verify-status {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  padding: 14rpx;
  background: #f8fafc;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
}

.status-text {
  font-size: 24rpx;
  color: $text-secondary;
  margin-left: 10rpx;
}

.form-item {
  padding: 16rpx 20rpx;
  border-bottom: 1rpx solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.form-item-half {
  padding: 16rpx 10rpx;
  flex: 1;
}

.form-row {
  display: flex;
  border-bottom: 1rpx solid $border-color;

  .form-item {
    border-bottom: none;
    border-right: 1rpx solid $border-color;

    &:last-child {
      border-right: none;
    }
  }
}

.form-label {
  display: flex;
  align-items: center;
  font-size: 24rpx;
  color: $text-secondary;
  font-weight: 500;
  margin-bottom: 10rpx;
}

.form-picker,
.form-input {
  width: 100%;
  height: 76rpx;
  background: #ffffff;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  padding: 0 16rpx;
  font-size: 26rpx;
  color: $text-primary;
  box-sizing: border-box;
}

.picker-value {
  height: 76rpx;
  line-height: 76rpx;
  color: $text-primary;
  font-size: 26rpx;
}

.field-hint {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.workflow-tip {
  margin: 16rpx 20rpx;
  padding: 16rpx;
  background: #f8fafc;
  border: 1rpx dashed #d7dee8;
  border-radius: $radius-md;
}

.tip-line {
  font-size: 23rpx;
  color: $text-secondary;
  line-height: 1.7;
}

.draft-card,
.order-card {
  margin: 16rpx 20rpx;
  padding: 16rpx;
  background: #f9fafb;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
}

.draft-title {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 600;
  margin-bottom: 10rpx;
}

.draft-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
  font-size: 24rpx;
}

.draft-label {
  color: $text-secondary;
  min-width: 110rpx;
}

.draft-value {
  flex: 1;
  text-align: right;
  word-break: break-all;
  font-weight: 600;
  color: $text-primary;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.order-title {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 600;
}

.order-status {
  font-size: 22rpx;
  padding: 6rpx 12rpx;
  border-radius: $radius-full;
}

.status-default {
  color: #64748b;
  background: #f1f5f9;
}

.status-pending {
  color: #92400e;
  background: #fffbeb;
}

.status-paid {
  color: #166534;
  background: #f0fdf4;
}

.btn-full {
  display: block;
  width: calc(100% - 40rpx);
  margin: 0 20rpx;
}

.btn-link {
  margin-top: 10rpx;
  background: transparent;
  color: $primary-color;
  font-size: 23rpx;
  border: none;
  text-align: center;
  padding: 0;

  &::after {
    border: none;
  }
}

.btn-primary,
.btn-secondary {
  display: block;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: $radius-md;
  font-size: 28rpx;
  font-weight: 600;
  text-align: center;
  letter-spacing: 0;
}

.btn-primary {
  background: $primary-color;
  color: #ffffff;
  border: none;

  &[disabled] {
    opacity: 0.65;
  }

  &::after {
    border: none;
  }
}

.btn-secondary {
  background: #ffffff;
  color: $text-primary;
  border: 1rpx solid $border-color;

  &::after {
    border: none;
  }
}

.plan-entry {
  padding: 0 0 20rpx;
}

.plan-card {
  background: #ffffff;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid $border-color;
}

.plan-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-primary;
}

.plan-status {
  font-size: 20rpx;
  padding: 6rpx 12rpx;
  background: #dcfce7;
  color: #16a34a;
  border-radius: $radius-full;
}

.plan-info {
  margin-bottom: 20rpx;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f1f5f9;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 24rpx;
  color: $text-secondary;
}

.info-value {
  font-size: 24rpx;
  font-weight: 600;
  color: $text-primary;
}

.plan-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;

  button {
    flex: 1;
  }
}
</style>
