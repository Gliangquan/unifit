<template>
  <view class="page-content">
    <uni-section title="健康档案" class="section"></uni-section>
    <uni-card :border="false" padding="24">
      <uni-forms ref="healthFormRef" :modelValue="healthForm" label-position="top">
        <uni-forms-item label="身高(cm)" required>
          <uni-easyinput v-model="healthForm.height" type="digit" placeholder="请输入身高" />
        </uni-forms-item>
        <uni-forms-item label="体重(kg)" required>
          <uni-easyinput v-model="healthForm.weight" type="digit" placeholder="请输入体重" />
        </uni-forms-item>
        <uni-forms-item label="年龄" required>
          <uni-easyinput v-model="healthForm.age" type="number" placeholder="请输入年龄" />
        </uni-forms-item>
        <uni-forms-item label="性别" required>
          <uni-data-select v-model="healthForm.gender" :localdata="genderOptions" />
        </uni-forms-item>
      </uni-forms>

      <uni-list :border="false">
        <uni-list-item title="BMI" :right-text="bmiPreview" />
        <uni-list-item title="健康状态" :right-text="bmiStatusPreview" />
      </uni-list>

      <uni-list :border="false">
        <uni-list-item title="保存健康档案" showArrow clickable @click="saveHealthProfile" />
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
      healthForm: {
        age: '',
        gender: '',
        height: '',
        weight: ''
      },
      genderOptions: [
        { text: '男', value: 'male' },
        { text: '女', value: 'female' }
      ]
    }
  },
  computed: {
    bmiPreview() {
      const h = Number(this.healthForm.height)
      const w = Number(this.healthForm.weight)
      if (!h || !w || h <= 0) return '--'
      const bmi = w / ((h / 100) * (h / 100))
      if (!Number.isFinite(bmi)) return '--'
      return bmi.toFixed(2)
    },
    bmiStatusPreview() {
      const bmi = Number(this.bmiPreview)
      if (!Number.isFinite(bmi)) return '待评估'
      if (bmi < 18.5) return '偏瘦'
      if (bmi < 24) return '正常'
      if (bmi < 28) return '超重'
      return '肥胖'
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadHealthProfile()
  },
  methods: {
    async loadHealthProfile() {
      const data = await request({ url: '/health/profile/my', showError: false }) || {}
      this.healthForm = {
        age: data.age !== undefined && data.age !== null ? String(data.age) : '',
        gender: data.gender || '',
        height: data.height !== undefined && data.height !== null ? String(data.height) : '',
        weight: data.weight !== undefined && data.weight !== null ? String(data.weight) : ''
      }
    },
    async saveHealthProfile() {
      if (!this.healthForm.age || !this.healthForm.height || !this.healthForm.weight || !this.healthForm.gender) {
        uni.showToast({ title: '请完整填写健康档案', icon: 'none' })
        return
      }
      await request({
        url: '/health/profile/update',
        method: 'POST',
        data: {
          age: Number(this.healthForm.age),
          gender: this.healthForm.gender,
          height: Number(this.healthForm.height),
          weight: Number(this.healthForm.weight)
        }
      })
      uni.showToast({ title: '健康档案已保存', icon: 'success' })
      await this.loadHealthProfile()
    }
  }
}
</script>
