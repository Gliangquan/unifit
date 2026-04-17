<template>
  <view class="page-content">
    <uni-section title="学生身份认证" class="section"></uni-section>
    <uni-card :border="false" padding="24">
      <uni-list :border="false">
        <uni-list-item title="当前状态" :right-text="verifyStatusText" />
      </uni-list>

      <uni-forms ref="verifyFormRef" :modelValue="verifyForm" label-position="top">
        <uni-forms-item label="学号" required>
          <uni-easyinput v-model="verifyForm.studentId" placeholder="请输入学号" />
        </uni-forms-item>
        <uni-forms-item label="姓名" required>
          <uni-easyinput v-model="verifyForm.realName" placeholder="请输入姓名" />
        </uni-forms-item>
        <uni-forms-item label="选择班级" required>
          <picker class="class-picker" :range="classOptions" range-key="label" @change="onClassChange">
            <view class="picker-text">{{ selectedClassLabel }}</view>
          </picker>
        </uni-forms-item>
      </uni-forms>

      <uni-notice-bar v-if="studentProfile.rejectReason" :text="`驳回原因：${studentProfile.rejectReason}`" show-icon />

      <uni-list :border="false">
        <uni-list-item title="提交认证" showArrow clickable @click="submitVerify" />
      </uni-list>
    </uni-card>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin } from '@/common/auth'

export default {
  data() {
    return {
      studentProfile: {},
      classOptions: [],
      verifyForm: {
        studentId: '',
        realName: '',
        className: '',
        classId: null
      }
    }
  },
  computed: {
    verifyStatusText() {
      const map = {
        pending: '待审核',
        approved: '已通过',
        rejected: '已拒绝'
      }
      return map[this.studentProfile.verificationStatus] || '未提交'
    },
    selectedClassLabel() {
      const row = this.classOptions.find(item => item.value === this.verifyForm.classId)
      return row ? row.label : '请选择班级'
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await Promise.all([this.loadClasses(), this.loadProfile()])
  },
  methods: {
    async loadClasses() {
      const rows = await request({ url: '/student/classes', showError: false }) || []
      this.classOptions = rows.map(item => ({
        label: item.className + (item.classCode ? `（${item.classCode}）` : ''),
        value: item.id,
        className: item.className
      }))
    },
    async loadProfile() {
      const data = await request({ url: '/student/profile/my', showError: false }) || {}
      this.studentProfile = data
      this.verifyForm.studentId = data.studentId || ''
      this.verifyForm.realName = data.realName || ''
      this.verifyForm.className = data.className || ''
      this.verifyForm.classId = data.classId || null
    },
    onClassChange(e) {
      const row = this.classOptions[Number(e.detail.value)]
      this.verifyForm.classId = row ? row.value : null
      this.verifyForm.className = row ? row.className : ''
    },
    async submitVerify() {
      if (!this.verifyForm.studentId || !this.verifyForm.realName) {
        uni.showToast({ title: '请填写学号和姓名', icon: 'none' })
        return
      }
      if (!this.verifyForm.classId) {
        uni.showToast({ title: '请选择班级', icon: 'none' })
        return
      }
      await request({
        url: '/student/verify/submit',
        method: 'POST',
        data: {
          studentId: this.verifyForm.studentId,
          realName: this.verifyForm.realName,
          classId: this.verifyForm.classId,
          className: this.verifyForm.className || ''
        }
      })
      uni.showToast({ title: '认证已提交', icon: 'success' })
      await this.loadProfile()
    }
  }
}
</script>

<style lang="scss" scoped>
.class-picker {
  padding: 20rpx;
  border: 1px solid #d7dde8;
  border-radius: 16rpx;
  background: #fff;
}

.picker-text {
  color: #334155;
  font-size: 28rpx;
}
</style>
