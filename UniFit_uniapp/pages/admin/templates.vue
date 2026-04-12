<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="uf-section-title">模板编辑</view>
      <input class="uf-input" v-model="templateForm.templateCode" placeholder="模板编码" />
      <input class="uf-input" v-model="templateForm.templateName" placeholder="模板名称" />

      <picker class="uf-picker" :range="testItemOptions" range-key="itemName" @change="onTestItemChange">
        <view>体测项目：{{ templateForm.testItemCode || '请选择' }}</view>
      </picker>

      <input class="uf-input" v-model="templateForm.scoreLevel" placeholder="成绩等级（beginner/intermediate/advanced）" />
      <input class="uf-input" v-model="templateForm.fitnessLevel" placeholder="基础（newbie/basic/advanced）" />
      <input class="uf-input" v-model="templateForm.equipmentType" placeholder="器械类型（bodyweight/track/gym）" />
      <input class="uf-input" v-model="templateForm.daysPerWeek" type="number" placeholder="每周训练天数" />
      <textarea class="uf-input area" v-model="templateForm.description" placeholder="描述" />
      <view class="ops">
        <button class="uf-btn-primary" @click="saveTemplate">{{ templateForm.id ? '更新模板' : '新增模板' }}</button>
        <button class="uf-btn-secondary" @click="resetTemplate">重置</button>
      </view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">模板列表</view>
        <text class="hint">{{ templates.length }} 条</text>
      </view>
      <view v-if="templates.length">
        <view class="row" v-for="row in templates" :key="row.id">
          <view class="name">{{ row.templateName }}（{{ row.templateCode }}）</view>
          <view class="sub">{{ row.testItemCode }} / {{ row.scoreLevel }} / 每周{{ row.daysPerWeek }}天</view>
          <view class="ops">
            <button class="uf-btn-secondary mini" @click="editTemplate(row)">编辑</button>
            <button class="uf-btn-secondary mini" @click="selectTemplate(row)">动作项</button>
            <button class="uf-btn-secondary mini" @click="deleteTemplate(row)">删除</button>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无模板。</view>
    </view>

    <view v-if="currentTemplate" class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="uf-section-title">模板动作项（{{ currentTemplate.templateName }}）</view>
      <view class="grid-two">
        <input class="uf-input" v-model="itemForm.weekNo" type="number" placeholder="周次" />
        <input class="uf-input" v-model="itemForm.dayNo" type="number" placeholder="日次" />
      </view>
      <picker class="uf-picker" :range="exerciseOptions" range-key="name" @change="onExerciseChange">
        <view>动作：{{ itemForm.exerciseId || '请选择' }}</view>
      </picker>
      <view class="grid-two">
        <input class="uf-input" v-model="itemForm.setsCount" type="number" placeholder="组数" />
        <input class="uf-input" v-model="itemForm.repsCount" type="number" placeholder="次数" />
      </view>
      <view class="grid-two">
        <input class="uf-input" v-model="itemForm.durationMinutes" type="number" placeholder="时长(分钟)" />
        <input class="uf-input" v-model="itemForm.sortNo" type="number" placeholder="排序" />
      </view>
      <input class="uf-input" v-model="itemForm.intensityNote" placeholder="强度说明" />
      <view class="ops">
        <button class="uf-btn-primary" @click="saveItem">{{ itemForm.id ? '更新动作项' : '新增动作项' }}</button>
        <button class="uf-btn-secondary" @click="resetItem">重置</button>
      </view>

      <view v-if="items.length" style="margin-top:12rpx;">
        <view class="row" v-for="row in items" :key="row.id">
          <view class="name">W{{ row.weekNo }} D{{ row.dayNo }} · #{{ row.exerciseId }}</view>
          <view class="sub">{{ row.setsCount || 0 }}组 × {{ row.repsCount || 0 }}次 · {{ row.durationMinutes || 0 }}分钟</view>
          <view class="ops">
            <button class="uf-btn-secondary mini" @click="editItem(row)">编辑</button>
            <button class="uf-btn-secondary mini" @click="deleteItem(row)">删除</button>
          </view>
        </view>
      </view>
      <view v-else class="empty">该模板暂无动作项。</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureAdmin } from '@/common/auth'

const newTemplateForm = () => ({
  id: null,
  templateCode: '',
  templateName: '',
  testItemCode: '',
  scoreLevel: 'beginner',
  fitnessLevel: 'newbie',
  equipmentType: 'bodyweight',
  bmiRange: 'all',
  daysPerWeek: 3,
  description: '',
  status: 1
})

const newItemForm = () => ({
  id: null,
  weekNo: 1,
  dayNo: 1,
  exerciseId: null,
  setsCount: 3,
  repsCount: 10,
  durationMinutes: 15,
  intensityNote: '',
  sortNo: 1
})

export default {
  data() {
    return {
      templates: [],
      testItemOptions: [],
      exerciseOptions: [],
      templateForm: newTemplateForm(),
      currentTemplate: null,
      items: [],
      itemForm: newItemForm()
    }
  },
  async onShow() {
    if (!ensureAdmin()) return
    await this.loadOptions()
    await this.loadTemplates()
  },
  methods: {
    async loadOptions() {
      this.testItemOptions = await request({ url: '/admin/template/test-items', showError: false }) || []
      this.exerciseOptions = await request({ url: '/admin/template/exercises', showError: false }) || []
      if (!this.templateForm.testItemCode && this.testItemOptions.length) {
        this.templateForm.testItemCode = this.testItemOptions[0].itemCode
      }
      if (!this.itemForm.exerciseId && this.exerciseOptions.length) {
        this.itemForm.exerciseId = this.exerciseOptions[0].id
      }
    },
    async loadTemplates() {
      const page = await request({ url: '/admin/template/list?current=1&pageSize=50', showError: false }) || {}
      this.templates = page.records || []
    },
    onTestItemChange(e) {
      const row = this.testItemOptions[Number(e.detail.value)]
      this.templateForm.testItemCode = row ? row.itemCode : ''
    },
    onExerciseChange(e) {
      const row = this.exerciseOptions[Number(e.detail.value)]
      this.itemForm.exerciseId = row ? row.id : null
    },
    editTemplate(row) {
      this.templateForm = { ...row }
    },
    resetTemplate() {
      this.templateForm = newTemplateForm()
    },
    async saveTemplate() {
      if (!this.templateForm.templateCode || !this.templateForm.templateName || !this.templateForm.testItemCode) {
        uni.showToast({ title: '请填写模板核心字段', icon: 'none' })
        return
      }
      await request({
        url: '/admin/template/upsert',
        method: 'POST',
        data: {
          ...this.templateForm,
          daysPerWeek: Number(this.templateForm.daysPerWeek || 3)
        }
      })
      uni.showToast({ title: '模板已保存', icon: 'success' })
      this.resetTemplate()
      await this.loadTemplates()
    },
    async deleteTemplate(row) {
      await request({
        url: '/admin/template/delete',
        method: 'POST',
        data: { id: row.id }
      })
      uni.showToast({ title: '模板已删除', icon: 'success' })
      if (this.currentTemplate && this.currentTemplate.id === row.id) {
        this.currentTemplate = null
        this.items = []
      }
      await this.loadTemplates()
    },
    async selectTemplate(row) {
      this.currentTemplate = row
      this.items = await request({ url: `/admin/template/items?templateId=${row.id}`, showError: false }) || []
      this.resetItem()
    },
    editItem(row) {
      this.itemForm = { ...row }
    },
    resetItem() {
      this.itemForm = newItemForm()
      if (this.exerciseOptions.length) {
        this.itemForm.exerciseId = this.exerciseOptions[0].id
      }
    },
    async saveItem() {
      if (!this.currentTemplate) return
      await request({
        url: '/admin/template/item/upsert',
        method: 'POST',
        data: {
          ...this.itemForm,
          templateId: this.currentTemplate.id,
          weekNo: Number(this.itemForm.weekNo || 1),
          dayNo: Number(this.itemForm.dayNo || 1),
          exerciseId: Number(this.itemForm.exerciseId),
          setsCount: Number(this.itemForm.setsCount || 0),
          repsCount: Number(this.itemForm.repsCount || 0),
          durationMinutes: Number(this.itemForm.durationMinutes || 0),
          sortNo: Number(this.itemForm.sortNo || 1)
        }
      })
      uni.showToast({ title: '动作项已保存', icon: 'success' })
      await this.selectTemplate(this.currentTemplate)
    },
    async deleteItem(row) {
      await request({
        url: '/admin/template/item/delete',
        method: 'POST',
        data: { id: row.id }
      })
      uni.showToast({ title: '动作项已删除', icon: 'success' })
      await this.selectTemplate(this.currentTemplate)
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.area {
  width: 100%;
  min-height: 120rpx;
  box-sizing: border-box;
}

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
}

.mini {
  height: 60rpx;
  line-height: 60rpx;
  font-size: 22rpx;
  padding: 0 22rpx;
}

.empty {
  color: $text-secondary;
  font-size: 24rpx;
}
</style>
