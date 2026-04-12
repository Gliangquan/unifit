<template>
  <view class="page">
    <uni-card :is-shadow="false" :border="false" margin="20rpx" padding="20rpx">
      <view class="title-row">
        <view class="card-title">动作库</view>
        <uni-tag text="内容发布" type="primary" size="small" />
      </view>

      <uni-easyinput
        v-model="query.keyword"
        placeholder="搜索标题/简介"
        :input-border="true"
        @confirm="reload"
      />

      <view class="filter-row">
        <picker class="picker" :range="categoryOptions" range-key="label" @change="onCategoryChange">
          <view class="picker-text">类型：{{ categoryLabel }}</view>
        </picker>
        <picker class="picker" :range="difficultyOptions" range-key="label" @change="onDifficultyChange">
          <view class="picker-text">等级：{{ difficultyLabel }}</view>
        </picker>
      </view>

      <view class="action-row">
        <uni-tag text="搜索" type="primary" circle @click="reload" />
        <uni-tag text="重置筛选" type="default" circle @click="resetFilter" />
      </view>
    </uni-card>

    <uni-card :is-shadow="false" :border="false" margin="0 20rpx 20rpx 20rpx" padding="20rpx">
      <view class="title-row">
        <view class="card-title">内容列表</view>
        <text class="hint">共 {{ total }} 条</text>
      </view>

      <uni-load-more v-if="loading && !rows.length" status="loading" />

      <view v-if="rows.length">
        <view class="item-card" v-for="row in rows" :key="row.id" @click="goDetail(row)">
          <image v-if="row.coverImageUrl" class="cover" :src="row.coverImageUrl" mode="aspectFill" />
          <view class="content">
            <view class="item-title">{{ row.name || `动作#${row.id}` }}</view>
            <view class="meta-row">
              <uni-tag :text="categoryText(row.category)" type="primary" size="small" />
              <uni-tag :text="difficultyText(row.difficulty)" type="warning" size="small" />
              <uni-tag :text="equipmentText(row.equipmentRequired)" type="default" size="small" />
            </view>
            <view class="desc">{{ row.description || '暂无简介' }}</view>
            <view class="stat-row">
              <view class="stat-item">
                <uni-icons type="hand-up" size="15" color="#f97316"></uni-icons>
                <text>{{ row.likeCount || 0 }}</text>
              </view>
              <view class="stat-item">
                <uni-icons type="chat" size="15" color="#64748b"></uni-icons>
                <text>{{ row.commentCount || 0 }}</text>
              </view>
            </view>
          </view>
          <uni-icons type="right" size="16" color="#94a3b8"></uni-icons>
        </view>

        <view class="load-more-row">
          <uni-tag
            v-if="rows.length < total"
            text="加载更多"
            type="default"
            circle
            @click="loadMore"
          />
          <uni-load-more v-else status="noMore" :content-text="{ noMore: '已经到底啦' }" />
        </view>
      </view>

      <uni-load-more
        v-if="!loading && !rows.length"
        status="noMore"
        :content-text="{ noMore: '暂无动作内容' }"
      />
    </uni-card>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin } from '@/common/auth'

const categoryOptions = [
  { label: '全部类型', value: '' },
  { label: '上肢', value: 'upper' },
  { label: '下肢', value: 'lower' },
  { label: '核心', value: 'core' },
  { label: '有氧', value: 'cardio' },
  { label: '恢复', value: 'recovery' }
]

const difficultyOptions = [
  { label: '全部等级', value: '' },
  { label: '零基础', value: 'newbie' },
  { label: '初级', value: 'beginner' },
  { label: '进阶', value: 'intermediate' },
  { label: '强化', value: 'advanced' },
  { label: '高级', value: 'advanced' }
]

export default {
  data() {
    return {
      rows: [],
      total: 0,
      loading: false,
      categoryOptions,
      difficultyOptions,
      query: {
        current: 1,
        pageSize: 10,
        keyword: '',
        category: '',
        difficulty: ''
      }
    }
  },
  computed: {
    categoryLabel() {
      const row = this.categoryOptions.find(item => item.value === this.query.category)
      return row ? row.label : '全部类型'
    },
    difficultyLabel() {
      const row = this.difficultyOptions.find(item => item.value === this.query.difficulty)
      return row ? row.label : '全部等级'
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.reload()
  },
  methods: {
    buildListUrl() {
      const params = [
        `current=${this.query.current}`,
        `pageSize=${this.query.pageSize}`
      ]
      if (this.query.keyword) params.push(`keyword=${encodeURIComponent(this.query.keyword)}`)
      if (this.query.category) params.push(`category=${encodeURIComponent(this.query.category)}`)
      if (this.query.difficulty) params.push(`difficulty=${encodeURIComponent(this.query.difficulty)}`)
      return `/exercise/list?${params.join('&')}`
    },
    categoryText(value) {
      const map = {
        upper: '上肢',
        upper_body: '上肢',
        lower: '下肢',
        lower_body: '下肢',
        core: '核心',
        cardio: '有氧',
        aerobic: '有氧',
        recovery: '恢复'
      }
      return map[value] || value || '未分类'
    },
    difficultyText(value) {
      const map = {
        newbie: '零基础',
        beginner: '初级',
        basic: '初级',
        intermediate: '进阶',
        advanced: '强化'
      }
      return map[value] || value || '未分级'
    },
    equipmentText(value) {
      if (!value) return '无器械'
      const map = {
        bodyweight: '无器械',
        dorm: '宿舍器械',
        dorm_equipment: '宿舍器械',
        gym: '健身房',
        track: '跑道',
        band: '弹力带'
      }
      return value
        .split(/[、,，\s]+/)
        .filter(Boolean)
        .map(item => map[item] || item)
        .join('、')
    },
    resetFilter() {
      this.query.keyword = ''
      this.query.category = ''
      this.query.difficulty = ''
      this.reload()
    },
    async reload() {
      this.query.current = 1
      this.rows = []
      await this.load()
    },
    async load() {
      if (this.loading) return
      this.loading = true
      try {
        const page = await request({ url: this.buildListUrl(), showError: false }) || {}
        const records = page.records || []
        this.total = Number(page.total || 0)
        if (this.query.current === 1) {
          this.rows = records
        } else {
          this.rows = this.rows.concat(records)
        }
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      if (this.rows.length >= this.total) return
      this.query.current += 1
      await this.load()
    },
    onCategoryChange(e) {
      const row = this.categoryOptions[Number(e.detail.value)]
      this.query.category = row ? row.value : ''
      this.reload()
    },
    onDifficultyChange(e) {
      const row = this.difficultyOptions[Number(e.detail.value)]
      this.query.difficulty = row ? row.value : ''
      this.reload()
    },
    goDetail(row) {
      uni.navigateTo({ url: `/pages/exercise/detail?id=${row.id}` })
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.page {
  min-height: 100vh;
  background: #f5f5f7;
  padding-bottom: 20rpx;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-primary;
}

.filter-row {
  display: flex;
  gap: 12rpx;
  margin-top: 4rpx;
}

.picker {
  flex: 1;
  background: #fff;
  border: 1px solid $border-color;
  border-radius: 12rpx;
  padding: 14rpx 16rpx;
}

.picker-text {
  font-size: 24rpx;
  color: $text-secondary;
}

.action-row {
  margin-top: 12rpx;
  display: flex;
  gap: 10rpx;
}

.hint {
  color: $text-secondary;
  font-size: 22rpx;
}

.item-card {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 16rpx 0;
  border-bottom: 1px dashed $border-color;
}

.item-card:last-child {
  border-bottom: 0;
}

.cover {
  width: 180rpx;
  height: 128rpx;
  border-radius: 12rpx;
  background: #f1f5f9;
}

.content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 28rpx;
  color: $text-primary;
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.meta-row {
  margin-top: 8rpx;
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
}

.desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-secondary;
  line-height: 1.5;
}

.stat-row {
  margin-top: 10rpx;
  display: flex;
  gap: 16rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  color: $text-muted;
  font-size: 22rpx;
}

.load-more-row {
  padding-top: 14rpx;
  display: flex;
  justify-content: center;
}
</style>
