<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="uf-section-title">标准筛选</view>
      <view class="grid-two">
        <picker class="uf-picker" :range="stageOptions" @change="onStageFilterChange">
          <view>阶段：{{ query.stage || '全部' }}</view>
        </picker>
        <picker class="uf-picker" :range="genderFilterOptions" @change="onGenderFilterChange">
          <view>性别：{{ query.gender || '全部' }}</view>
        </picker>
      </view>
      <picker class="uf-picker" :range="testItemOptions" range-key="itemName" @change="onItemFilterChange">
        <view>项目：{{ query.itemCode || '全部' }}</view>
      </picker>
      <view class="ops">
        <button class="uf-btn-primary" @click="loadRows(true)">查询</button>
        <button class="uf-btn-secondary" @click="resetQuery">重置</button>
        <button class="uf-btn-secondary" @click="importCollege">导入高校全量标准</button>
      </view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="uf-section-title">标准编辑</view>
      <view class="grid-two">
        <picker class="uf-picker" :range="stageOptions" @change="onFormStageChange">
          <view>阶段：{{ form.stage }}</view>
        </picker>
        <input class="uf-input" v-model="form.gradeRange" placeholder="年级范围（all）" />
      </view>
      <view class="grid-two">
        <picker class="uf-picker" :range="genderOptions" @change="onFormGenderChange">
          <view>性别：{{ form.gender }}</view>
        </picker>
        <picker class="uf-picker" :range="testItemOptions" range-key="itemName" @change="onFormItemChange">
          <view>项目：{{ form.itemCode || '请选择' }}</view>
        </picker>
      </view>
      <view class="grid-two">
        <input class="uf-input" v-model="form.minScore" type="digit" placeholder="最小成绩" />
        <input class="uf-input" v-model="form.maxScore" type="digit" placeholder="最大成绩" />
      </view>
      <view class="grid-two">
        <picker class="uf-picker" :range="levelOptions" @change="onFormLevelChange">
          <view>等级：{{ form.level }}</view>
        </picker>
        <input class="uf-input" v-model="form.standardPoint" type="number" placeholder="标准分" />
      </view>
      <view class="ops">
        <button class="uf-btn-primary" @click="save">{{ form.id ? '更新标准' : '新增标准' }}</button>
        <button class="uf-btn-secondary" @click="resetForm">重置</button>
      </view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">标准列表</view>
        <text class="hint">共 {{ total }} 条</text>
      </view>

      <view v-if="rows.length">
        <view class="row" v-for="row in rows" :key="row.id">
          <view class="name">{{ itemName(row.itemCode) }} · {{ row.gender }} · {{ row.level }}</view>
          <view class="sub">阶段：{{ row.stage }} / 年级：{{ row.gradeRange }}</view>
          <view class="sub">成绩区间：{{ row.minScore }} - {{ row.maxScore }} · 标准分：{{ row.standardPoint }}</view>
          <view class="ops">
            <button class="uf-btn-secondary mini" @click="edit(row)">编辑</button>
            <button class="uf-btn-secondary mini" @click="remove(row)">删除</button>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无标准。</view>

      <view class="pager" v-if="total > query.pageSize">
        <button class="uf-btn-secondary mini" :disabled="query.current <= 1" @click="prevPage">上一页</button>
        <text class="page-text">第 {{ query.current }} 页</text>
        <button class="uf-btn-secondary mini" :disabled="query.current * query.pageSize >= total" @click="nextPage">下一页</button>
      </view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureAdmin } from '@/common/auth'

const newForm = () => ({
  id: null,
  stage: 'college',
  gradeRange: 'all',
  gender: 'male',
  itemCode: '',
  minScore: '',
  maxScore: '',
  level: 'pass',
  standardPoint: '60'
})

export default {
  data() {
    return {
      stageOptions: ['college'],
      genderOptions: ['male', 'female'],
      genderFilterOptions: ['全部', 'male', 'female'],
      levelOptions: ['excellent', 'good', 'pass', 'fail'],
      testItemOptions: [],
      query: {
        current: 1,
        pageSize: 20,
        stage: '',
        gender: '',
        itemCode: ''
      },
      rows: [],
      total: 0,
      form: newForm()
    }
  },
  async onShow() {
    if (!ensureAdmin()) return
    await this.loadTestItems()
    await this.loadRows(true)
  },
  methods: {
    itemName(code) {
      const item = this.testItemOptions.find(i => i.itemCode === code)
      return item ? item.itemName : code
    },
    onStageFilterChange(e) {
      const value = this.stageOptions[Number(e.detail.value)]
      this.query.stage = value === 'college' ? 'college' : ''
    },
    onGenderFilterChange(e) {
      const value = this.genderFilterOptions[Number(e.detail.value)]
      this.query.gender = value === '全部' ? '' : value
    },
    onItemFilterChange(e) {
      const row = this.testItemOptions[Number(e.detail.value)]
      this.query.itemCode = row ? row.itemCode : ''
    },
    onFormStageChange(e) {
      this.form.stage = this.stageOptions[Number(e.detail.value)]
    },
    onFormGenderChange(e) {
      this.form.gender = this.genderOptions[Number(e.detail.value)]
    },
    onFormItemChange(e) {
      const row = this.testItemOptions[Number(e.detail.value)]
      this.form.itemCode = row ? row.itemCode : ''
    },
    onFormLevelChange(e) {
      this.form.level = this.levelOptions[Number(e.detail.value)]
    },
    async loadTestItems() {
      this.testItemOptions = await request({ url: '/admin/standard/test-items', showError: false }) || []
      if (!this.form.itemCode && this.testItemOptions.length) {
        this.form.itemCode = this.testItemOptions[0].itemCode
      }
    },
    async loadRows(reset = false) {
      if (reset) this.query.current = 1
      const page = await request({
        url: '/admin/standard/list/page',
        method: 'POST',
        data: {
          current: this.query.current,
          pageSize: this.query.pageSize,
          stage: this.query.stage || undefined,
          gender: this.query.gender || undefined,
          itemCode: this.query.itemCode || undefined
        },
        showError: false
      }) || {}
      this.rows = page.records || []
      this.total = Number(page.total || 0)
    },
    resetQuery() {
      this.query = {
        current: 1,
        pageSize: 20,
        stage: '',
        gender: '',
        itemCode: ''
      }
      this.loadRows(true)
    },
    edit(row) {
      this.form = {
        id: row.id,
        stage: row.stage || 'college',
        gradeRange: row.gradeRange || 'all',
        gender: row.gender || 'male',
        itemCode: row.itemCode || '',
        minScore: row.minScore !== null && row.minScore !== undefined ? String(row.minScore) : '',
        maxScore: row.maxScore !== null && row.maxScore !== undefined ? String(row.maxScore) : '',
        level: row.level || 'pass',
        standardPoint: row.standardPoint !== null && row.standardPoint !== undefined ? String(row.standardPoint) : '60'
      }
    },
    resetForm() {
      this.form = newForm()
      if (this.testItemOptions.length) {
        this.form.itemCode = this.testItemOptions[0].itemCode
      }
    },
    async save() {
      if (!this.form.stage || !this.form.gradeRange || !this.form.gender || !this.form.itemCode || !this.form.level) {
        uni.showToast({ title: '请填写完整字段', icon: 'none' })
        return
      }
      if (this.form.minScore === '' || this.form.maxScore === '' || this.form.standardPoint === '') {
        uni.showToast({ title: '请填写成绩与标准分', icon: 'none' })
        return
      }
      await request({
        url: '/admin/standard/upsert',
        method: 'POST',
        data: {
          id: this.form.id || undefined,
          stage: this.form.stage,
          gradeRange: this.form.gradeRange,
          gender: this.form.gender,
          itemCode: this.form.itemCode,
          minScore: Number(this.form.minScore),
          maxScore: Number(this.form.maxScore),
          level: this.form.level,
          standardPoint: Number(this.form.standardPoint)
        }
      })
      uni.showToast({ title: '标准已保存', icon: 'success' })
      this.resetForm()
      await this.loadRows()
    },
    async remove(row) {
      await request({
        url: '/admin/standard/delete',
        method: 'POST',
        data: { id: row.id }
      })
      uni.showToast({ title: '标准已删除', icon: 'success' })
      await this.loadRows()
    },
    async importCollege() {
      const res = await request({
        url: '/admin/standard/import/college-full',
        method: 'POST'
      }) || {}
      uni.showToast({ title: `导入${res.inserted || 0}条`, icon: 'success' })
      await this.loadRows(true)
    },
    prevPage() {
      if (this.query.current <= 1) return
      this.query.current -= 1
      this.loadRows()
    },
    nextPage() {
      if (this.query.current * this.query.pageSize >= this.total) return
      this.query.current += 1
      this.loadRows()
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.grid-two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.hint {
  color: $text-secondary;
  font-size: 22rpx;
}

.row {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: 14rpx;
  margin-bottom: 10rpx;
}

.name {
  font-size: 26rpx;
  color: $text-primary;
  font-weight: 600;
}

.sub {
  margin-top: 6rpx;
  color: $text-secondary;
  font-size: 22rpx;
}

.ops {
  display: flex;
  gap: 12rpx;
  margin-top: 10rpx;
  flex-wrap: wrap;
}

.mini {
  height: 60rpx;
  line-height: 60rpx;
  font-size: 22rpx;
  padding: 0 22rpx;
}

.pager {
  margin-top: 12rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16rpx;
}

.page-text {
  font-size: 22rpx;
  color: $text-secondary;
}

.empty {
  color: $text-secondary;
  font-size: 24rpx;
}
</style>
