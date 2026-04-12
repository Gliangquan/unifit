<template>
  <view class="page-content">
    <template v-if="!isAdminRole">
      <uni-section title="健康概览" class="section"></uni-section>
      <uni-card :border="false" padding="24">
        <uni-list :border="false">
          <uni-list-item title="身高(cm)" :right-text="displayValue(health.height)" />
          <uni-list-item title="体重(kg)" :right-text="displayValue(health.weight)" />
          <uni-list-item title="年龄" :right-text="displayValue(health.age)" />
          <uni-list-item title="性别" :right-text="displayValue(health.gender)" />
          <uni-list-item title="BMI" :right-text="displayValue(health.bmiValue)" />
          <uni-list-item title="健康状态" :right-text="displayValue(health.bmiStatus)" />
        </uni-list>
      </uni-card>

      <uni-section title="快捷入口" class="section"></uni-section>
      <uni-list :border="false">
        <uni-list-item title="健康档案" showArrow clickable @click="goHealthProfile" />
        <uni-list-item title="学生身份认证" :right-text="verifyStatusText" showArrow clickable @click="goStudentVerify" />
      </uni-list>

      <uni-section title="BMI历史记录" class="section"></uni-section>
      <uni-card :border="false" padding="24">
        <uni-list :border="false" v-if="records.length">
          <uni-list-item
            v-for="(item, idx) in records.slice(0, 20)"
            :key="idx"
            :title="formatDate(item.recordDate)"
            :right-text="`${displayValue(item.bmiValue)} / ${displayValue(item.bmiStatus)}`"
          />
        </uni-list>
        <uni-notice-bar v-else text="暂无BMI历史记录" show-icon />
      </uni-card>
    </template>

    <template v-else>
      <uni-section title="管理员说明" class="section"></uni-section>
      <uni-notice-bar text="管理员账号无需维护个人健康档案，请使用管理功能页" show-icon />
      <uni-list :border="false">
        <uni-list-item title="管理看板" showArrow clickable @click="go('/pages/admin/dashboard')" />
        <uni-list-item title="学生审核" showArrow clickable @click="go('/pages/admin/students')" />
        <uni-list-item title="留言回复" showArrow clickable @click="go('/pages/admin/messages')" />
      </uni-list>
    </template>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'

export default {
  data() {
    return {
      user: {},
      health: {},
      studentProfile: {},
      records: []
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
    if (!this.isAdminRole) {
      await Promise.all([
        this.loadHealth(),
        this.loadStudentProfile(),
        this.loadRecords()
      ])
    }
  },
  methods: {
    go(url) {
      uni.navigateTo({ url })
    },
    displayValue(value) {
      if (value === undefined || value === null || value === '') return '--'
      return String(value)
    },
    formatDate(v) {
      if (!v) return '--'
      return String(v).slice(0, 10)
    },
    async loadCurrentUser() {
      const latest = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = { ...localUser, ...latest, token: localUser.token }
      setUser(this.user)
    },
    async loadHealth() {
      this.health = await request({ url: '/health/profile/my', showError: false }) || {}
    },
    async loadStudentProfile() {
      this.studentProfile = await request({ url: '/student/profile/my', showError: false }) || {}
    },
    async loadRecords() {
      const data = await request({ url: '/health/records/my', showError: false }) || []
      this.records = data.slice().sort((a, b) => {
        const ta = new Date(a.recordDate || a.createTime || 0).getTime()
        const tb = new Date(b.recordDate || b.createTime || 0).getTime()
        return tb - ta
      })
    },
    goHealthProfile() {
      uni.navigateTo({ url: '/pages/mine/health-profile' })
    },
    goStudentVerify() {
      uni.navigateTo({ url: '/pages/mine/student-verify' })
    }
  }
}
</script>
