<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">待回复留言</view>
        <text class="hint">{{ rows.length }} 条</text>
      </view>

      <view v-if="rows.length">
        <view class="row" v-for="row in rows" :key="row.id">
          <view class="name">{{ row.userName || ('用户' + row.userId) }} {{ row.studentId ? '(' + row.studentId + ')' : '' }}</view>
          <view class="question">Q：{{ row.questionContent }}</view>
          <textarea class="uf-input answer" v-model="replyMap[row.id]" placeholder="输入回复内容" />
          <button class="uf-btn-primary mini" @click="reply(row)">回复</button>
        </view>
      </view>
      <view v-else class="empty">暂无待回复留言。</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureAdmin } from '@/common/auth'

export default {
  data() {
    return {
      rows: [],
      replyMap: {}
    }
  },
  onShow() {
    if (!ensureAdmin()) return
    this.loadRows()
  },
  methods: {
    async loadRows() {
      this.rows = await request({ url: '/message/pending', showError: false }) || []
    },
    async reply(row) {
      const answerContent = (this.replyMap[row.id] || '').trim()
      if (!answerContent) {
        uni.showToast({ title: '请输入回复内容', icon: 'none' })
        return
      }
      await request({
        url: '/message/reply',
        method: 'POST',
        data: {
          id: row.id,
          answerContent
        }
      })
      this.replyMap[row.id] = ''
      uni.showToast({ title: '回复成功', icon: 'success' })
      await this.loadRows()
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

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
  font-size: 24rpx;
  color: $text-primary;
  font-weight: 600;
}

.question {
  margin-top: 8rpx;
  color: $text-secondary;
  font-size: 24rpx;
}

.answer {
  width: 100%;
  min-height: 120rpx;
  margin-top: 8rpx;
  box-sizing: border-box;
}

.mini {
  margin-top: 8rpx;
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
