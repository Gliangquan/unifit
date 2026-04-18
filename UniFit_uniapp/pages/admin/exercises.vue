<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="uf-section-title">动作编辑</view>
      <input class="uf-input" v-model="form.name" placeholder="动作名" />
      <input class="uf-input" v-model="form.category" placeholder="分类" />
      <input class="uf-input" v-model="form.difficulty" placeholder="难度" />
      <input class="uf-input" v-model="form.equipmentRequired" placeholder="器械" />
      <input class="uf-input" v-model="form.coverImageUrl" placeholder="封面图URL" />
      <input class="uf-input" v-model="form.demoVideoUrl" placeholder="示范视频URL" />
      <textarea class="uf-input area" v-model="form.description" placeholder="描述" />
      <textarea class="uf-input area" v-model="form.contentMd" placeholder="Markdown内容，可选" />
      <view class="ops">
        <button class="uf-btn-primary" @click="save">{{ form.id ? '更新动作' : '新增动作' }}</button>
        <button class="uf-btn-secondary" @click="resetForm">重置</button>
      </view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">动作列表</view>
        <text class="hint">{{ rows.length }} 条</text>
      </view>
      <view v-if="rows.length">
        <view class="row" v-for="row in rows" :key="row.id">
          <view class="name">{{ row.name }}</view>
          <view class="sub">{{ row.category || '-' }} / {{ row.difficulty || '-' }} / {{ row.equipmentRequired || '-' }}</view>
          <view class="sub">状态：{{ row.status === 1 ? '启用' : '禁用' }}</view>
          <view class="ops">
            <button class="uf-btn-secondary mini" @click="edit(row)">编辑</button>
            <button class="uf-btn-secondary mini" @click="remove(row)">删除</button>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无动作。</view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">替代动作关系</view>
        <text class="hint">主动作 {{ selectedMainExerciseName || '-' }}</text>
      </view>
      <picker
        class="uf-picker"
        :range="rows"
        range-key="name"
        @change="onMainExerciseChange"
      >
        <view>主动作：{{ selectedMainExerciseName || '请选择主动作' }}</view>
      </picker>
      <picker
        class="uf-picker"
        :range="alternativeOptions"
        range-key="name"
        @change="onAlternativeExerciseChange"
      >
        <view>替代动作：{{ selectedAlternativeExerciseName || '请选择替代动作' }}</view>
      </picker>
      <button class="uf-btn-primary" @click="addAlternative">新增替代关系</button>

      <view v-if="relations.length" style="margin-top: 16rpx;">
        <view class="row" v-for="row in relations" :key="row.id">
          <view class="name">{{ resolveExerciseName(row.exerciseId) }} → {{ resolveExerciseName(row.alternativeExerciseId) }}</view>
          <view class="sub">关系ID：{{ row.id }}</view>
          <view class="ops">
            <button class="uf-btn-secondary mini" @click="removeAlternative(row)">删除关系</button>
          </view>
        </view>
      </view>
      <view v-else class="empty">当前主动作暂无替代关系。</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureAdmin } from '@/common/auth'

const emptyForm = () => ({
  id: null,
  name: '',
  category: '',
  difficulty: '',
  equipmentRequired: '',
  coverImageUrl: '',
  demoVideoUrl: '',
  description: '',
  contentMd: '',
  status: 1
})

export default {
  data() {
    return {
      form: emptyForm(),
      rows: [],
      relationForm: {
        exerciseId: null,
        alternativeExerciseId: null
      },
      relations: []
    }
  },
  computed: {
    selectedMainExerciseName() {
      const current = this.rows.find(item => item.id === this.relationForm.exerciseId)
      return current ? current.name : ''
    },
    selectedAlternativeExerciseName() {
      const current = this.rows.find(item => item.id === this.relationForm.alternativeExerciseId)
      return current ? current.name : ''
    },
    alternativeOptions() {
      return this.rows.filter(item => item.id !== this.relationForm.exerciseId)
    }
  },
  onShow() {
    if (!ensureAdmin()) return
    this.loadRows()
  },
  methods: {
    async loadRows() {
      const page = await request({ url: '/exercise/list?current=1&pageSize=200', showError: false }) || {}
      this.rows = page.records || []
      if (!this.rows.length) {
        this.relationForm.exerciseId = null
        this.relationForm.alternativeExerciseId = null
        this.relations = []
        return
      }
      if (!this.relationForm.exerciseId || !this.rows.find(item => item.id === this.relationForm.exerciseId)) {
        this.relationForm.exerciseId = this.rows[0].id
      }
      if (this.relationForm.alternativeExerciseId === this.relationForm.exerciseId) {
        this.relationForm.alternativeExerciseId = null
      }
      await this.loadRelations()
    },
    resolveExerciseName(id) {
      const row = this.rows.find(item => item.id === id)
      if (!row) return `动作#${id}`
      return row.name || `动作#${id}`
    },
    onMainExerciseChange(e) {
      const row = this.rows[Number(e.detail.value)]
      if (!row) return
      this.relationForm.exerciseId = row.id
      if (this.relationForm.alternativeExerciseId === row.id) {
        this.relationForm.alternativeExerciseId = null
      }
      this.loadRelations()
    },
    onAlternativeExerciseChange(e) {
      const row = this.alternativeOptions[Number(e.detail.value)]
      this.relationForm.alternativeExerciseId = row ? row.id : null
    },
    async loadRelations() {
      if (!this.relationForm.exerciseId) {
        this.relations = []
        return
      }
      this.relations = await request({
        url: `/exercise/alternative/relations?exerciseId=${this.relationForm.exerciseId}`,
        showError: false
      }) || []
    },
    async addAlternative() {
      if (!this.relationForm.exerciseId) {
        uni.showToast({ title: '请选择主动作', icon: 'none' })
        return
      }
      if (!this.relationForm.alternativeExerciseId) {
        uni.showToast({ title: '请选择替代动作', icon: 'none' })
        return
      }
      if (this.relationForm.exerciseId === this.relationForm.alternativeExerciseId) {
        uni.showToast({ title: '主动作和替代动作不能相同', icon: 'none' })
        return
      }
      await request({
        url: '/exercise/alternative/upsert',
        method: 'POST',
        data: {
          exerciseId: this.relationForm.exerciseId,
          alternativeExerciseId: this.relationForm.alternativeExerciseId
        }
      })
      uni.showToast({ title: '新增成功', icon: 'success' })
      await this.loadRelations()
    },
    async removeAlternative(row) {
      await request({
        url: '/exercise/alternative/delete',
        method: 'POST',
        data: { id: row.id }
      })
      uni.showToast({ title: '删除成功', icon: 'success' })
      await this.loadRelations()
    },
    edit(row) {
      this.form = { ...row }
    },
    resetForm() {
      this.form = emptyForm()
    },
    async save() {
      if (!this.form.name) {
        uni.showToast({ title: '请输入动作名', icon: 'none' })
        return
      }
      await request({
        url: '/exercise/upsert',
        method: 'POST',
        data: { ...this.form }
      })
      uni.showToast({ title: '保存成功', icon: 'success' })
      this.resetForm()
      await this.loadRows()
    },
    async remove(row) {
      await request({
        url: '/exercise/delete',
        method: 'POST',
        data: { id: row.id }
      })
      uni.showToast({ title: '删除成功', icon: 'success' })
      await this.loadRows()
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
