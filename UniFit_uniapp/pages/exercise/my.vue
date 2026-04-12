<template>
  <view class="page">
    <uni-card :is-shadow="false" :border="false" margin="20rpx" padding="20rpx">
      <view class="title-row">
        <view class="title">我的互动</view>
        <uni-tag :text="activeTab === 'like' ? `点赞 ${likeTotal}` : `评论 ${commentTotal}`" type="primary" size="small" />
      </view>
      <view class="tab-row">
        <uni-tag
          :text="'我的点赞'"
          :type="activeTab === 'like' ? 'primary' : 'default'"
          circle
          @click="switchTab('like')"
        />
        <uni-tag
          :text="'我的评论'"
          :type="activeTab === 'comment' ? 'primary' : 'default'"
          circle
          @click="switchTab('comment')"
        />
      </view>
    </uni-card>

    <uni-card :is-shadow="false" :border="false" margin="0 20rpx 20rpx 20rpx" padding="20rpx">
      <uni-load-more v-if="loading && !currentRows.length" status="loading" />

      <view v-if="activeTab === 'like'">
        <view v-if="likeRows.length">
          <view class="item-card" v-for="item in likeRows" :key="`like-${item.exerciseId}-${item.likedAt}`" @click="goExercise(item.exerciseId)">
            <image v-if="item.coverImageUrl" class="cover" :src="item.coverImageUrl" mode="aspectFill" />
            <view class="content">
              <view class="item-title">{{ item.exerciseName || `动作#${item.exerciseId}` }}</view>
              <view class="meta-row">
                <uni-tag :text="categoryText(item.category)" type="primary" size="small" />
                <uni-tag :text="difficultyText(item.difficulty)" type="warning" size="small" />
              </view>
              <view class="desc">{{ item.description || '暂无简介' }}</view>
              <view class="time">点赞时间：{{ formatTime(item.likedAt) }}</view>
            </view>
            <uni-icons type="right" size="16" color="#94a3b8" />
          </view>
          <view class="load-row">
            <uni-tag v-if="likeRows.length < likeTotal" text="加载更多" type="default" circle @click="loadMore" />
            <uni-load-more v-else status="noMore" :content-text="{ noMore: '已经到底啦' }" />
          </view>
        </view>
        <uni-load-more v-else-if="!loading" status="noMore" :content-text="{ noMore: '还没有点赞记录' }" />
      </view>

      <view v-else>
        <view v-if="commentRows.length">
          <view class="comment-card" v-for="item in commentRows" :key="`comment-${item.id}`">
            <view class="comment-head">
              <view class="comment-title" @click="goExercise(item.exerciseId)">
                {{ item.exerciseName || `动作#${item.exerciseId}` }}
              </view>
              <view class="comment-op">
                <uni-tag text="查看" type="default" size="small" @click="goExercise(item.exerciseId)" />
                <uni-tag text="删除" type="error" size="small" @click="deleteComment(item)" />
              </view>
            </view>
            <view class="comment-body">{{ item.content }}</view>
            <view class="time">{{ formatTime(item.createTime) }}</view>
          </view>
          <view class="load-row">
            <uni-tag v-if="commentRows.length < commentTotal" text="加载更多" type="default" circle @click="loadMore" />
            <uni-load-more v-else status="noMore" :content-text="{ noMore: '已经到底啦' }" />
          </view>
        </view>
        <uni-load-more v-else-if="!loading" status="noMore" :content-text="{ noMore: '还没有评论记录' }" />
      </view>
    </uni-card>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin } from '@/common/auth'

export default {
  data() {
    return {
      activeTab: 'like',
      loading: false,
      likeRows: [],
      commentRows: [],
      likeQuery: {
        current: 1,
        pageSize: 10
      },
      commentQuery: {
        current: 1,
        pageSize: 10
      },
      likeTotal: 0,
      commentTotal: 0
    }
  },
  computed: {
    currentRows() {
      return this.activeTab === 'like' ? this.likeRows : this.commentRows
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.reloadCurrent()
  },
  methods: {
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
    formatTime(value) {
      if (!value) return ''
      return String(value).replace('T', ' ').slice(0, 16)
    },
    switchTab(tab) {
      if (this.activeTab === tab) return
      this.activeTab = tab
      this.reloadCurrent()
    },
    async reloadCurrent() {
      if (this.activeTab === 'like') {
        this.likeQuery.current = 1
        this.likeRows = []
        await this.loadLikes()
        return
      }
      this.commentQuery.current = 1
      this.commentRows = []
      await this.loadComments()
    },
    async loadLikes() {
      if (this.loading) return
      this.loading = true
      try {
        const page = await request({
          url: `/exercise/like/my?current=${this.likeQuery.current}&pageSize=${this.likeQuery.pageSize}`,
          showError: false
        }) || {}
        const records = page.records || []
        this.likeTotal = Number(page.total || 0)
        if (this.likeQuery.current === 1) {
          this.likeRows = records
        } else {
          this.likeRows = this.likeRows.concat(records)
        }
      } finally {
        this.loading = false
      }
    },
    async loadComments() {
      if (this.loading) return
      this.loading = true
      try {
        const page = await request({
          url: `/exercise/comment/my?current=${this.commentQuery.current}&pageSize=${this.commentQuery.pageSize}`,
          showError: false
        }) || {}
        const records = page.records || []
        this.commentTotal = Number(page.total || 0)
        if (this.commentQuery.current === 1) {
          this.commentRows = records
        } else {
          this.commentRows = this.commentRows.concat(records)
        }
      } finally {
        this.loading = false
      }
    },
    async loadMore() {
      if (this.activeTab === 'like') {
        if (this.likeRows.length >= this.likeTotal) return
        this.likeQuery.current += 1
        await this.loadLikes()
        return
      }
      if (this.commentRows.length >= this.commentTotal) return
      this.commentQuery.current += 1
      await this.loadComments()
    },
    goExercise(exerciseId) {
      if (!exerciseId) return
      uni.navigateTo({ url: `/pages/exercise/detail?id=${exerciseId}` })
    },
    async deleteComment(item) {
      if (!item || !item.id) return
      await request({
        url: '/exercise/comment/delete',
        method: 'POST',
        data: { id: item.id }
      })
      uni.showToast({ title: '评论已删除', icon: 'success' })
      await this.reloadCurrent()
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

.title {
  font-size: 30rpx;
  font-weight: 600;
  color: $text-primary;
}

.tab-row {
  display: flex;
  gap: 10rpx;
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
  width: 160rpx;
  height: 116rpx;
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
}

.desc {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-secondary;
  line-height: 1.5;
}

.time {
  margin-top: 8rpx;
  font-size: 20rpx;
  color: $text-muted;
}

.comment-card {
  padding: 14rpx 0;
  border-bottom: 1px dashed $border-color;
}

.comment-card:last-child {
  border-bottom: 0;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10rpx;
}

.comment-title {
  flex: 1;
  font-size: 26rpx;
  font-weight: 600;
  color: $text-primary;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.comment-op {
  display: flex;
  gap: 8rpx;
}

.comment-body {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $text-secondary;
  line-height: 1.6;
}

.load-row {
  padding-top: 14rpx;
  display: flex;
  justify-content: center;
}
</style>
