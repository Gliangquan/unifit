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
        <uni-forms-item label="班级">
          <uni-easyinput v-model="verifyForm.className" placeholder="请输入班级（如：软件工程2201）" />
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
      verifyForm: {
        studentId: '',
        realName: '',
        className: ''
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
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadProfile()
  },
  methods: {
    async loadProfile() {
      const data = await request({ url: '/student/profile/my', showError: false }) || {}
      this.studentProfile = data
      this.verifyForm.studentId = data.studentId || ''
      this.verifyForm.realName = data.realName || ''
      this.verifyForm.className = data.className || ''
    },
    async submitVerify() {
      if (!this.verifyForm.studentId || !this.verifyForm.realName) {
        uni.showToast({ title: '请填写学号和姓名', icon: 'none' })
        return
      }
      await request({
        url: '/student/verify/submit',
        method: 'POST',
        data: {
          studentId: this.verifyForm.studentId,
          realName: this.verifyForm.realName,
          className: this.verifyForm.className || ''
        }
      })
      uni.showToast({ title: '认证已提交', icon: 'success' })
      await this.loadProfile()
    }
  }
}
</script>
