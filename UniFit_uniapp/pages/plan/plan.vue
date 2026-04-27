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

      <view class="section-container">
        <view class="section-header row-between plan-header-bar">
          <text>当前训练计划</text>
          <button class="btn-primary plan-generate-btn" @click="openGeneratePopup">{{ currentPlan ? '重新生成' : '生成计划' }}</button>
        </view>

        <view v-if="currentPlan" class="plan-card">
          <view class="plan-header">
            <view class="plan-title">{{ currentPlan.testItemName || selectedTestItem || '训练计划' }}</view>
            <view class="plan-status">{{ currentPlan.status === 'completed' ? '已完成' : '进行中' }}</view>
          </view>
          <view class="plan-info">
            <view class="info-row">
              <text class="info-label">训练项目</text>
              <text class="info-value">{{ currentPlan.testItemName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">计划分档</text>
              <text class="info-value">{{ scoreLevelText(currentPlan.scoreLevel) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">目标成绩</text>
              <text class="info-value">{{ currentPlan.targetScore }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">当前成绩</text>
              <text class="info-value">{{ currentPlan.currentScore }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">BMI范围</text>
              <text class="info-value">{{ currentPlan.bmiRangeLabel || bmiRangeTextFromPlan(currentPlan) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">训练基础</text>
              <text class="info-value">{{ currentPlan.fitnessLevelLabel }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">器械类型</text>
              <text class="info-value">{{ currentPlan.equipmentTypeLabel }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">每周天数</text>
              <text class="info-value">每周 {{ currentPlan.daysPerWeek }} 天</text>
            </view>
          </view>
          <view class="plan-actions single-action">
            <button class="btn-secondary" @click="viewPlanDetails">查看详情</button>
          </view>
        </view>

        <view v-else class="empty-box plan-empty-box">
          <text class="empty-text">暂无训练计划</text>
          <text class="empty-hint">点击右上角“生成计划”，填写信息后即可生成</text>
          <text v-if="debugPlanText" class="plan-debug-text">{{ debugPlanText }}</text>
        </view>
      </view>

      <view v-if="showGeneratePopup" class="plan-popup-mask" @click="closeGeneratePopup">
        <view class="plan-popup" @click.stop>
          <view class="plan-popup-header">
            <view>
              <view class="plan-popup-title">生成训练计划</view>
              <view class="plan-popup-subtitle">按你的当前状态生成更匹配的计划</view>
            </view>
            <text class="plan-popup-close" @click="closeGeneratePopup">×</text>
          </view>

          <view class="form-item">
            <view class="form-label">
              <uni-icons type="bars" size="18" color="#64748b"></uni-icons>
              <text>训练项目</text>
            </view>
            <picker class="form-picker" :range="testItemOptions" range-key="itemName" @change="onTestItemChange">
              <view class="picker-value">{{ selectedTestItem || '请选择项目' }}</view>
            </picker>
          </view>

          <view class="form-item">
            <view class="form-label">
              <uni-icons type="medal" size="18" color="#64748b"></uni-icons>
              <text>当前成绩</text>
            </view>
            <input class="form-input" v-model="form.currentScore" type="text" :placeholder="currentScorePlaceholder" @input="syncScoreLevel" />
            <view class="field-hint" v-if="currentTestItemUnit">单位：{{ currentTestItemUnit }}</view>
            <view class="field-hint">{{ currentScoreReasonableText }}</view>
            <view v-if="currentScoreRangeList.length" class="score-rule-box">
              <view class="score-rule-title">分档说明</view>
              <view v-for="(range, index) in currentScoreRangeList" :key="`${form.testItemCode}_${index}`" class="score-rule-row">
                <text class="score-rule-level">{{ scoreLevelText(range.planLevel) }}</text>
                <text class="score-rule-range">{{ range.label }}</text>
                <text class="score-rule-point">{{ range.standardPoint }}分</text>
              </view>
            </view>
          </view>

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

          <view class="form-row">
            <view class="form-item form-item-half">
              <view class="form-label">
                <uni-icons type="star" size="18" color="#64748b"></uni-icons>
                <text>计划分档</text>
              </view>
              <view class="form-input readonly-field">{{ scoreLevelText(form.scoreLevel) }}</view>
            </view>

            <view class="form-item form-item-half">
              <view class="form-label">
                <uni-icons type="paperplane" size="18" color="#64748b"></uni-icons>
                <text>BMI范围</text>
              </view>
              <view class="form-input readonly-field">{{ bmiRangeText }}</view>
            </view>
          </view>

          <view class="form-item">
            <view class="form-label">
              <uni-icons type="calendar" size="18" color="#64748b"></uni-icons>
              <text>每周训练天数</text>
            </view>
            <input class="form-input" v-model="form.daysPerWeek" type="number" placeholder="请输入天数（1-7）" />
          </view>

          <view class="match-tip">
            将按「项目 + 分档 + 训练基础 + 器械类型 + BMI范围 + 每周天数」匹配训练模板
          </view>

          <view class="plan-popup-actions">
            <button class="btn-secondary" @click="closeGeneratePopup">取消</button>
            <button class="btn-primary" @click="generate">生成训练计划</button>
          </view>
        </view>
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
        { value: 'newbie', label: '入门' },
        { value: 'basic', label: '基础' },
        { value: 'advanced', label: '进阶' }
      ],
      equipmentTypeOptions: [
        { value: 'bodyweight', label: '无器械' },
        { value: 'dorm', label: '宿舍器械' },
        { value: 'track', label: '跑道' },
        { value: 'gym', label: '健身房' },
        { value: 'mixed', label: '综合' }
      ],
      form: {
        testItemCode: '',
        currentScore: '',
        scoreLevel: '',
        fitnessLevel: 'newbie',
        fitnessLevelLabel: '入门',
        equipmentType: 'bodyweight',
        equipmentTypeLabel: '无器械',
        daysPerWeek: '3',
        bmiValue: null
      },
      scoreRuleMap: {},
      scoreLevelRequestId: 0,
      selectedTestItem: '',
      studentProfile: {},
      needStudentVerify: false,
      showGeneratePopup: false,
      currentPlan: null,
      debugPlanText: ''
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
    },
    currentScoreRule() {
      return this.scoreRuleMap[this.form.testItemCode] || null
    },
    currentScoreReasonableText() {
      const rule = this.currentScoreRule
      if (!rule) return '将按高校体测分档自动判定'
      const unit = rule.scoreUnit ? ` ${rule.scoreUnit}` : ''
      return `合理成绩范围：${this.formatScoreValue(rule.reasonableMin)} - ${this.formatScoreValue(rule.reasonableMax)}${unit}`
    },
    currentScoreRangeList() {
      const rule = this.currentScoreRule
      return rule && Array.isArray(rule.ranges) ? rule.ranges : []
    },
    bmiRangeText() {
      const bmi = Number(this.form.bmiValue)
      if (!Number.isFinite(bmi) || bmi <= 0) return '未获取'
      if (bmi < 18.5) return '偏瘦'
      if (bmi < 24) return '正常'
      if (bmi < 28) return '超重'
      return '肥胖'
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    if (this.isAdminRole) {
      await this.loadAdminOverview()
      return
    }
    const cachedPlan = this.readCachedPlan()
    if (cachedPlan) {
      this.currentPlan = cachedPlan
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
      await this.loadScoreRules()
    },
    async loadScoreRules() {
      const nextMap = {}
      for (const item of this.testItemOptions) {
        if (!item || !item.itemCode) continue
        try {
          const rule = await request({
            url: `/test/score/rule-preview?itemCode=${encodeURIComponent(item.itemCode)}`,
            showError: false
          })
          if (rule) {
            nextMap[item.itemCode] = rule
          }
        } catch (e) {
          // ignore
        }
      }
      this.scoreRuleMap = nextMap
    },
    onTestItemChange(e) {
      const row = this.testItemOptions[Number(e.detail.value)]
      if (row) {
        this.form.testItemCode = row.itemCode
        this.selectedTestItem = row.itemName
        this.form.currentScore = ''
        this.form.scoreLevel = ''
        this.scoreLevelRequestId += 1
      }
    },
    onFitnessChange(e) {
      const row = this.fitnessLevelOptions[Number(e.detail.value)]
      if (row) {
        this.form.fitnessLevel = row.value
        this.form.fitnessLevelLabel = row.label
      }
    },
    async syncScoreLevel() {
      const rawScore = String(this.form.currentScore ?? '').trim()
      const normalizedScore = rawScore.replace(/[^\d.]/g, '')
      if (normalizedScore !== rawScore) {
        this.form.currentScore = normalizedScore
      }
      const currentScore = Number(normalizedScore)
      const requestId = ++this.scoreLevelRequestId
      if (!this.form.testItemCode || !normalizedScore || !Number.isFinite(currentScore) || currentScore < 0) {
        this.form.scoreLevel = ''
        return
      }
      const rule = this.currentScoreRule
      if (rule) {
        const min = Number(rule.reasonableMin)
        const max = Number(rule.reasonableMax)
        if (Number.isFinite(min) && Number.isFinite(max) && (currentScore < min || currentScore > max)) {
          this.form.scoreLevel = ''
          uni.showToast({ title: `成绩超出合理范围：${this.formatScoreValue(rule.reasonableMin)}-${this.formatScoreValue(rule.reasonableMax)}`, icon: 'none' })
          return
        }
      }
      try {
        const preview = await request({
          url: `/test/score/level-preview?itemCode=${encodeURIComponent(this.form.testItemCode)}&scoreValue=${encodeURIComponent(currentScore)}`,
          showError: false
        })
        if (requestId !== this.scoreLevelRequestId) return
        if (preview && preview.outOfRange) {
          this.form.scoreLevel = ''
          uni.showToast({ title: preview.message || '成绩超出合理范围', icon: 'none' })
          return
        }
        this.form.scoreLevel = (preview && preview.level) || ''
      } catch (e) {
        if (requestId !== this.scoreLevelRequestId) return
        this.form.scoreLevel = this.resolveScoreLevel(currentScore)
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
      const rawScore = String(this.form.currentScore ?? '').trim()
      const normalizedScore = rawScore.replace(/[^\d.]/g, '')
      if (!normalizedScore) {
        uni.showToast({ title: '请先输入当前成绩', icon: 'none' })
        return null
      }
      if (normalizedScore !== rawScore) {
        this.form.currentScore = normalizedScore
      }
      const currentScore = Number(normalizedScore)
      if (!Number.isFinite(currentScore) || currentScore < 0) {
        uni.showToast({ title: '请输入有效成绩', icon: 'none' })
        return null
      }
      const rule = this.currentScoreRule
      if (rule) {
        const min = Number(rule.reasonableMin)
        const max = Number(rule.reasonableMax)
        if (Number.isFinite(min) && Number.isFinite(max) && (currentScore < min || currentScore > max)) {
          uni.showToast({ title: rule.message || `成绩需在${this.formatScoreValue(rule.reasonableMin)}-${this.formatScoreValue(rule.reasonableMax)}之间`, icon: 'none' })
          return null
        }
      }
      const scoreLevel = this.form.scoreLevel || this.resolveScoreLevel(currentScore)
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
      this.form.scoreLevel = scoreLevel
      return {
        testItemCode: this.form.testItemCode,
        currentScore,
        scoreLevel,
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
        generatedPlan = this.ensureRenderablePlan(generatedPlan)
        const renderPlan = this.normalizePlan(generatedPlan, payload)
        this.debugPlanText = `generate返回: ${JSON.stringify(generatedPlan)}`
        console.log('[plan-page] generate result', generatedPlan)
        console.log('[plan-page] render plan', renderPlan)
        this.cacheCurrentPlan(renderPlan)
        this.currentPlan = renderPlan
        this.showGeneratePopup = false
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
            const nextUser = {
              ...localUser,
              balance: purchaseResult && purchaseResult.balance !== undefined ? purchaseResult.balance : localUser.balance,
              planUnlocked: purchaseResult && purchaseResult.planUnlocked !== undefined ? purchaseResult.planUnlocked : localUser.planUnlocked,
              planUnlockTime: purchaseResult && purchaseResult.planUnlockTime !== undefined ? purchaseResult.planUnlockTime : localUser.planUnlockTime
            }
            this.user = nextUser
            setUser(nextUser)
            uni.setStorageSync('user', nextUser)
            this.appendPlanAccessOrder(purchaseResult)
            let generatedPlan = await request({
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
            generatedPlan = this.ensureRenderablePlan(generatedPlan)
            const renderPlan = this.normalizePlan(generatedPlan, payload)
            this.debugPlanText = `purchase后generate返回: ${JSON.stringify(generatedPlan)}`
            console.log('[plan-page] generate result after purchase', generatedPlan)
            console.log('[plan-page] render plan after purchase', renderPlan)
            this.cacheCurrentPlan(renderPlan)
            this.currentPlan = renderPlan
            this.showGeneratePopup = false
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
    getPlanCacheKey() {
      return `unifit_current_plan_${this.user.id || 'guest'}`
    },
    ensureRenderablePlan(plan) {
      if (!plan) return null
      if (plan.planId) return plan
      if (plan.id) {
        return { ...plan, planId: plan.id }
      }
      return plan
    },
    buildDraftPlan(payload, plan) {
      const basePlan = this.ensureRenderablePlan(plan) || {}
      const scoreLevel = basePlan.scoreLevel || 'beginner'
      const snapshot = basePlan.snapshot || {
        currentScore: payload ? payload.currentScore : ''
      }
      return {
        ...basePlan,
        planId: basePlan.planId || `draft-${Date.now()}`,
        testItemCode: basePlan.testItemCode || (payload ? payload.testItemCode : ''),
        scoreLevel,
        fitnessLevel: basePlan.fitnessLevel || (payload ? payload.fitnessLevel : ''),
        equipmentType: basePlan.equipmentType || (payload ? payload.equipmentType : ''),
        daysPerWeek: basePlan.daysPerWeek || (payload ? payload.daysPerWeek : ''),
        snapshot,
        status: basePlan.status || 'active'
      }
    },
    normalizePlan(plan, payload) {
      const safePlan = this.ensureRenderablePlan(plan) || this.buildDraftPlan(payload, plan)
      if (!safePlan) return null
      return {
        ...safePlan,
        testItemName: this.testItemLabel(safePlan.testItemCode) || this.selectedTestItem || '训练计划',
        targetScore: this.targetScoreText(safePlan.scoreLevel),
        currentScore: this.currentScoreText(safePlan),
        fitnessLevelLabel: this.fitnessLevelText(safePlan.fitnessLevel),
        equipmentTypeLabel: this.equipmentTypeText(safePlan.equipmentType),
        bmiRangeLabel: this.bmiRangeTextFromPlan(safePlan)
      }
    },
    cacheCurrentPlan(plan) {
      if (!plan) return
      uni.setStorageSync(this.getPlanCacheKey(), plan)
    },
    appendPlanAccessOrder(purchaseResult) {
      const user = this.user || uni.getStorageSync('user') || {}
      const uid = user.id || 'guest'
      const key = `purchase_orders_${uid}`
      const list = uni.getStorageSync(key) || []
      const nextList = Array.isArray(list) ? list.slice() : []
      nextList.unshift({
        orderNo: `PLAN_ACCESS_${Date.now()}`,
        type: 'plan_access',
        typeText: '方案开通',
        planName: '重新生成计划开通',
        amount: purchaseResult && purchaseResult.cost !== undefined ? Number(purchaseResult.cost || 0) : 19.9,
        status: 'paid',
        createdAt: purchaseResult && purchaseResult.planUnlockTime ? purchaseResult.planUnlockTime : new Date().toISOString(),
        paidAt: purchaseResult && purchaseResult.planUnlockTime ? purchaseResult.planUnlockTime : new Date().toISOString()
      })
      uni.setStorageSync(key, nextList)
    },
    readCachedPlan() {
      return uni.getStorageSync(this.getPlanCacheKey()) || null
    },
    async loadCurrentPlan() {
      try {
        let plan = await request({ url: '/plan/current', showError: false }).catch(() => null)
        if (!plan) {
          const plans = await request({ url: '/plan/list', showError: false }).catch(() => []) || []
          plan = plans.find(item => item && item.status === 'active') || plans[plans.length - 1] || null
        }
        if (!plan) {
          plan = this.readCachedPlan()
        }
        if (plan) {
          const renderPlan = this.normalizePlan(plan)
          this.debugPlanText = `loadCurrentPlan命中: ${JSON.stringify(plan)}`
          console.log('[plan-page] loadCurrentPlan source plan', plan)
          console.log('[plan-page] loadCurrentPlan renderPlan', renderPlan)
          this.currentPlan = renderPlan
          this.cacheCurrentPlan(renderPlan)
        } else {
          this.debugPlanText = 'loadCurrentPlan未拿到计划'
          console.log('[plan-page] loadCurrentPlan empty')
          this.currentPlan = null
        }
      } catch (error) {
        const cached = this.readCachedPlan()
        this.debugPlanText = `loadCurrentPlan异常: ${error && error.message ? error.message : error}`
        console.log('[plan-page] loadCurrentPlan error', error)
        console.log('[plan-page] loadCurrentPlan cached', cached)
        this.currentPlan = cached ? this.normalizePlan(cached) : null
      }
    },
    openGeneratePopup() {
      if (this.ensureStudentVerifiedAction()) {
        return
      }
      this.showGeneratePopup = true
    },
    closeGeneratePopup() {
      this.showGeneratePopup = false
    },
    viewPlanDetails() {
      if (this.currentPlan && this.currentPlan.planId && !String(this.currentPlan.planId).startsWith('draft-')) {
        this.go(`/pages/plan/current?planId=${this.currentPlan.planId}`)
        return
      }
      uni.showToast({ title: '计划详情同步中，请稍后再试', icon: 'none' })
    },
    testItemLabel(code) {
      if (!code) return '-'
      const row = this.testItemOptions.find(item => item.itemCode === code)
      return row ? row.itemName : (this.selectedTestItem || code)
    },
    resolveScoreLevel(currentScore) {
      if (!this.form.testItemCode || !Number.isFinite(currentScore)) return ''
      const item = this.testItemOptions.find(row => row.itemCode === this.form.testItemCode)
      if (item && Array.isArray(item.standards) && item.standards.length) {
        const standards = [...item.standards].sort((a, b) => Number(a.thresholdScore || 0) - Number(b.thresholdScore || 0))
        const isLowerBetter = item.direction === 'lower'
        if (isLowerBetter) {
          if (currentScore <= Number(standards[0].thresholdScore || 0)) return 'advanced'
          if (standards[1] && currentScore <= Number(standards[1].thresholdScore || 0)) return 'intermediate'
          return 'beginner'
        }
        const advancedLine = standards[standards.length - 1]
        const intermediateLine = standards.length >= 2 ? standards[standards.length - 2] : null
        if (currentScore >= Number(advancedLine.thresholdScore || 0)) return 'advanced'
        if (intermediateLine && currentScore >= Number(intermediateLine.thresholdScore || 0)) return 'intermediate'
        return 'beginner'
      }
      const direction = (item && item.scoreDirection) || (item && item.direction) || 'higher'
      if (direction === 'lower') {
        if (currentScore <= 220) return 'advanced'
        if (currentScore <= 280) return 'intermediate'
        return 'beginner'
      }
      if (currentScore >= 80) return 'advanced'
      if (currentScore >= 60) return 'intermediate'
      return 'beginner'
    },
    scoreLevelText(value) {
      const map = {
        beginner: '基础档',
        intermediate: '提升档',
        advanced: '强化档'
      }
      return map[value] || '待计算'
    },
    formatScoreValue(value) {
      if (value === undefined || value === null || value === '') return '--'
      const num = Number(value)
      if (!Number.isFinite(num)) return String(value)
      return Number.isInteger(num) ? String(num) : num.toFixed(2).replace(/\.00$/, '').replace(/(\.\d)0$/, '$1')
    },
    fitnessLevelText(value) {
      const map = {
        newbie: '入门',
        beginner: '入门',
        basic: '基础',
        intermediate: '基础',
        advanced: '进阶'
      }
      return map[value] || value || '-'
    },
    equipmentTypeText(value) {
      const map = {
        bodyweight: '无器械',
        dorm: '宿舍器械',
        dorm_equipment: '宿舍器械',
        track: '跑道',
        gym: '健身房',
        mixed: '综合'
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
    },
    bmiRangeTextFromValue(value) {
      const bmi = Number(value)
      if (!Number.isFinite(bmi) || bmi <= 0) return '未获取'
      if (bmi < 18.5) return '偏瘦'
      if (bmi < 24) return '正常'
      if (bmi < 28) return '超重'
      return '肥胖'
    },
    bmiRangeTextFromPlan(plan) {
      if (!plan) return '未获取'
      const snapshot = plan.snapshot || {}
      return this.bmiRangeTextFromValue(snapshot.bmiValue)
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

.plan-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
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

.readonly-field {
  display: flex;
  align-items: center;
  color: $text-secondary;
  background: #f8fafc;
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

.score-rule-box {
  margin-top: 12rpx;
  border: 1rpx solid $border-color;
  border-radius: $radius-md;
  background: #f8fafc;
  padding: 12rpx 14rpx;
}

.score-rule-title {
  font-size: 23rpx;
  color: $text-primary;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.score-rule-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 8rpx 0;
  border-bottom: 1rpx solid #e5e7eb;
}

.score-rule-row:last-child {
  border-bottom: none;
}

.score-rule-level {
  min-width: 90rpx;
  font-size: 22rpx;
  color: $primary-color;
  font-weight: 600;
}

.score-rule-range,
.score-rule-point {
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

.plan-generate-btn {
  width: auto;
  min-width: 172rpx;
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 22rpx;
  font-size: 24rpx;
  border-radius: 999rpx;
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

.single-action {
  button {
    flex: none;
    width: 100%;
  }
}

.plan-empty-box {
  margin: 16rpx 20rpx 20rpx;
}

.plan-debug-text {
  display: block;
  margin-top: 12rpx;
  font-size: 20rpx;
  line-height: 1.5;
  color: #94a3b8;
  word-break: break-all;
}

.plan-popup-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.48);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 999;
}

.plan-popup {
  width: 100%;
  max-height: 84vh;
  background: #fff;
  border-radius: 28rpx 28rpx 0 0;
  padding: 24rpx 24rpx 32rpx;
  box-sizing: border-box;
  overflow-y: auto;
}

.plan-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20rpx;
}

.plan-popup-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $text-primary;
}

.plan-popup-subtitle {
  margin-top: 6rpx;
  font-size: 22rpx;
  color: $text-secondary;
}

.plan-popup-close {
  width: 48rpx;
  height: 48rpx;
  border-radius: 24rpx;
  background: #f3f4f6;
  text-align: center;
  line-height: 48rpx;
  font-size: 32rpx;
  color: $text-secondary;
}

.match-tip {
  margin-top: 18rpx;
  padding: 16rpx;
  border-radius: $radius-md;
  background: #f8fafc;
  border: 1rpx dashed #d7dee8;
  color: $text-secondary;
  font-size: 23rpx;
  line-height: 1.7;
}

.plan-popup-actions {
  display: flex;
  gap: 12rpx;
  margin-top: 20rpx;

  button {
    flex: 1;
  }
}
</style>
