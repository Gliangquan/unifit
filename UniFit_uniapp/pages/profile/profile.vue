<template>
  <view class="page-content">
    <template v-if="!isAdminRole">
      <uni-section title="健康概览" class="section"></uni-section>
      <uni-card :border="false" padding="24">
        <uni-list :border="false">
          <uni-list-item title="身高(cm)" :right-text="displayValue(health.height)" />
          <uni-list-item title="体重(kg)" :right-text="displayValue(health.weight)" />
          <uni-list-item title="年龄" :right-text="displayValue(health.age)" />
          <uni-list-item title="性别" :right-text="genderText(health.gender)" />
          <uni-list-item title="BMI" :right-text="displayValue(health.bmiValue)" />
          <uni-list-item title="健康状态" :right-text="bmiStatusText(health.bmiStatus)" />
        </uni-list>
      </uni-card>

      <uni-section title="快捷入口" class="section"></uni-section>
      <uni-list :border="false">
        <uni-list-item title="健康档案" showArrow clickable @click="goHealthProfile" />
        <uni-list-item title="学生身份认证" :right-text="verifyStatusText" showArrow clickable @click="goStudentVerify" />
      </uni-list>

      <uni-section title="BMI历史记录" class="section"></uni-section>
      <uni-card :border="false" padding="24">
        <canvas id="bmiTrendCanvas" canvas-id="bmiTrendCanvas" class="bmi-chart"></canvas>
        <uni-list :border="false" v-if="records.length">
          <uni-list-item
            v-for="(item, idx) in records.slice(0, 20)"
            :key="idx"
            :title="formatDate(item.recordDate)"
            :right-text="`${displayValue(item.bmiValue)} / ${bmiStatusText(item.bmiStatus)}`"
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
    genderText(value) {
      const map = { male: '男', female: '女' }
      return map[value] || this.displayValue(value)
    },
    bmiStatusText(value) {
      const map = {
        underweight: '偏瘦',
        normal: '正常',
        overweight: '超重',
        obese: '肥胖',
        unknown: '待评估'
      }
      return map[value] || this.displayValue(value)
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
      setTimeout(() => this.drawBmiChart(), 100)
    },
    drawBmiChart() {
      const ctx = uni.createCanvasContext('bmiTrendCanvas', this)
      const screenWidth = uni.getSystemInfoSync().windowWidth
      const width = screenWidth - 80
      const height = 220
      const left = 36
      const right = width - 16
      const top = 16
      const bottom = height - 28
      ctx.clearRect(0, 0, width, height)
      ctx.setFillStyle('#f7fbff')
      ctx.fillRect(0, 0, width, height)
      const source = (this.records || []).slice(0, 10).slice().reverse()
      const values = source.map(item => Number(item.bmiValue)).filter(v => !Number.isNaN(v))
      if (!values.length) {
        ctx.draw()
        return
      }
      let min = Math.min(...values)
      let max = Math.max(...values)
      min = Math.floor((min - 1) * 10) / 10
      max = Math.ceil((max + 1) * 10) / 10
      if (max === min) {
        max += 1
        min -= 1
      }
      ctx.setStrokeStyle('#dbe7f5')
      ctx.setLineWidth(1)
      for (let i = 0; i <= 4; i++) {
        const y = top + ((bottom - top) / 4) * i
        ctx.beginPath()
        ctx.moveTo(left, y)
        ctx.lineTo(right, y)
        ctx.stroke()
      }
      const stepX = values.length > 1 ? (right - left) / (values.length - 1) : 0
      ctx.setStrokeStyle('#f97316')
      ctx.setLineWidth(2)
      ctx.beginPath()
      values.forEach((val, idx) => {
        const x = left + idx * stepX
        const y = bottom - ((val - min) / (max - min)) * (bottom - top)
        if (idx === 0) ctx.moveTo(x, y)
        else ctx.lineTo(x, y)
      })
      ctx.stroke()
      ctx.setFillStyle('#f97316')
      values.forEach((val, idx) => {
        const x = left + idx * stepX
        const y = bottom - ((val - min) / (max - min)) * (bottom - top)
        ctx.beginPath()
        ctx.arc(x, y, 3, 0, Math.PI * 2)
        ctx.fill()
      })
      ctx.setFillStyle('#64748b')
      ctx.setFontSize(11)
      ctx.fillText(String(max.toFixed(1)), 4, top + 8)
      ctx.fillText(String(min.toFixed(1)), 4, bottom + 4)
      ctx.draw()
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

<style lang="scss" scoped>
.bmi-chart {
  width: 100%;
  height: 220rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  background: #f8fafc;
}
</style>
