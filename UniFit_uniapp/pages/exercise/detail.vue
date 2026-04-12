<template>
  <view class="page">
    <uni-load-more v-if="loading" status="loading"></uni-load-more>

    <template v-if="!loading && exercise.id">
      <uni-card :is-shadow="false" :border="false" margin="20rpx 20rpx 0 20rpx" padding="0">
        <view class="head-wrap">
          <image v-if="exercise.coverImageUrl" class="cover" :src="exercise.coverImageUrl" mode="aspectFill" />
          <view class="title">{{ exercise.name || `动作#${exercise.id}` }}</view>
          <view class="tag-row">
            <uni-tag :text="categoryText(exercise.category)" type="primary" size="small" />
            <uni-tag :text="difficultyText(exercise.difficulty)" type="warning" size="small" />
            <uni-tag :text="equipmentText(exercise.equipmentRequired)" type="default" size="small" />
          </view>
          <view class="desc">{{ exercise.description || '暂无简介' }}</view>
          <view class="stat-row">
            <view class="stat-item" @click="toggleLike">
              <uni-icons type="hand-up" size="18" :color="likeState.liked ? '#f97316' : '#94a3b8'"></uni-icons>
              <text class="stat-text">{{ likeState.liked ? '已点赞' : '点赞' }} {{ likeState.likeCount || 0 }}</text>
            </view>
            <view class="stat-item">
              <uni-icons type="chat" size="18" color="#64748b"></uni-icons>
              <text class="stat-text">评论 {{ comments.length }}</text>
            </view>
          </view>
        </view>
      </uni-card>

      <uni-card title="动作内容" :is-shadow="false" :border="false" margin="20rpx" padding="20rpx">
        <rich-text class="md-content" :nodes="contentHtml"></rich-text>
        <view v-if="contentVideos.length">
          <video
            v-for="(src, idx) in contentVideos"
            :key="src + idx"
            class="video-block"
            :src="src"
            controls
          ></video>
        </view>
      </uni-card>

      <uni-card title="评论区" :is-shadow="false" :border="false" margin="20rpx" padding="20rpx">
        <uni-easyinput
          v-model="commentInput"
          type="textarea"
          :input-border="true"
          placeholder="说点什么..."
          autoHeight
        />
        <view class="publish-row">
          <uni-tag text="发布评论" type="primary" circle @click="submitComment" />
        </view>

        <view v-if="comments.length" class="comment-wrap">
          <view class="comment-item" v-for="item in comments" :key="item.id">
            <view class="comment-head">
              <view class="user-wrap">
                <uni-icons type="person" size="16" color="#64748b"></uni-icons>
                <text class="comment-user">{{ item.userName || `用户${item.userId}` }}</text>
                <text class="comment-time">{{ formatTime(item.createTime) }}</text>
              </view>
              <uni-tag
                v-if="canDeleteComment(item)"
                text="删除"
                type="error"
                size="small"
                @click="deleteComment(item)"
              />
            </view>
            <view class="comment-content">{{ item.content }}</view>
          </view>
        </view>
        <uni-load-more v-else status="noMore" :content-text="{ noMore: '暂无评论，快来抢沙发' }"></uni-load-more>
      </uni-card>
    </template>

    <uni-load-more v-if="!loading && !exercise.id" status="noMore" :content-text="{ noMore: '未找到动作内容' }"></uni-load-more>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, getUser } from '@/common/auth'

export default {
  data() {
    return {
      exerciseId: 0,
      loading: false,
      exercise: {},
      commentInput: '',
      comments: [],
      likeState: {
        liked: false,
        likeCount: 0
      },
      contentHtml: '',
      contentVideos: [],
      currentUser: {}
    }
  },
  onLoad(options) {
    this.exerciseId = Number((options && options.id) || 0)
  },
  async onShow() {
    if (!ensureLogin()) return
    this.currentUser = getUser() || {}
    if (!this.exerciseId) {
      uni.showToast({ title: '动作ID无效', icon: 'none' })
      return
    }
    await this.refreshAll()
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
    formatTime(value) {
      if (!value) return ''
      return String(value).replace('T', ' ').slice(0, 16)
    },
    canDeleteComment(item) {
      if (!item || !this.currentUser) return false
      return Number(item.userId) === Number(this.currentUser.id) || (this.currentUser.userRole || '') === 'admin'
    },
    parseMd(md) {
      const source = String(md || '')
      const videos = []
      const noVideoMd = source.replace(/<video[^>]*src=["']([^"']+)["'][^>]*>\s*<\/video>/gi, (all, src) => {
        if (src) videos.push(src)
        return ''
      })
      const html = noVideoMd
        .replace(/^### (.*)$/gm, '<h3>$1</h3>')
        .replace(/^## (.*)$/gm, '<h2>$1</h2>')
        .replace(/^# (.*)$/gm, '<h1>$1</h1>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img alt="$1" src="$2" style="max-width:100%;border-radius:12rpx;margin:10rpx 0;" />')
        .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<span style="color:#f97316;">$1：$2</span>')
        .replace(/\n/g, '<br/>')
      return { html, videos }
    },
    async refreshAll() {
      this.loading = true
      try {
        const [exercise, comments, likeStatus] = await Promise.all([
          request({ url: `/exercise/get?id=${this.exerciseId}` }),
          request({ url: `/exercise/comment/list?exerciseId=${this.exerciseId}` }),
          request({ url: `/exercise/like/status?exerciseId=${this.exerciseId}`, showError: false }).catch(() => null)
        ])
        this.exercise = exercise || {}
        this.comments = comments || []
        this.likeState = likeStatus || { liked: false, likeCount: Number((exercise && exercise.likeCount) || 0) }
        const parsed = this.parseMd(this.exercise.contentMd)
        this.contentHtml = parsed.html
        this.contentVideos = parsed.videos
      } finally {
        this.loading = false
      }
    },
    async toggleLike() {
      const data = await request({
        url: '/exercise/like/toggle',
        method: 'POST',
        data: { exerciseId: this.exerciseId }
      })
      this.likeState = data || this.likeState
      this.exercise.likeCount = this.likeState.likeCount || 0
    },
    async submitComment() {
      const content = String(this.commentInput || '').trim()
      if (!content) {
        uni.showToast({ title: '请输入评论内容', icon: 'none' })
        return
      }
      await request({
        url: '/exercise/comment/add',
        method: 'POST',
        data: {
          exerciseId: this.exerciseId,
          content
        }
      })
      this.commentInput = ''
      await this.refreshAll()
      uni.showToast({ title: '评论成功', icon: 'success' })
    },
    async deleteComment(item) {
      await request({
        url: '/exercise/comment/delete',
        method: 'POST',
        data: { id: item.id }
      })
      await this.refreshAll()
      uni.showToast({ title: '删除成功', icon: 'success' })
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.page {
  min-height: 100vh;
  background: #f5f5f7;
  padding-bottom: 24rpx;
}

.head-wrap {
  padding: 20rpx;
}

.cover {
  width: 100%;
  height: 360rpx;
  border-radius: 16rpx;
  background: #f1f5f9;
}

.title {
  margin-top: 14rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: $text-primary;
}

.tag-row {
  margin-top: 10rpx;
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
}

.desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: $text-secondary;
}

.stat-row {
  margin-top: 16rpx;
  display: flex;
  gap: 24rpx;
  align-items: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-text {
  font-size: 24rpx;
  color: #334155;
}

.md-content {
  color: $text-primary;
  font-size: 24rpx;
  line-height: 1.7;
}

.video-block {
  width: 100%;
  margin-top: 16rpx;
  border-radius: 12rpx;
  background: #000;
}

.publish-row {
  margin-top: 12rpx;
  display: flex;
  justify-content: flex-end;
}

.comment-wrap {
  margin-top: 8rpx;
}

.comment-item {
  padding: 16rpx 0;
  border-bottom: 1px dashed $border-color;
}

.comment-item:last-child {
  border-bottom: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-wrap {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.comment-user {
  font-size: 24rpx;
  color: $text-primary;
  font-weight: 600;
}

.comment-time {
  margin-left: 8rpx;
  font-size: 20rpx;
  color: $text-muted;
}

.comment-content {
  margin-top: 8rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: $text-secondary;
}
</style>
