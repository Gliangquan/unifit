<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up" v-if="!isAdminRole">
      <view class="uf-section-title">提交咨询</view>
      <textarea class="uf-input question" v-model="questionContent" placeholder="请输入你的训练问题（500字内）" />
      <button class="uf-btn-primary" @click="submitQuestion">提交留言</button>
    </view>

    <view class="uf-card uf-fade-up" :style="{ marginTop: !isAdminRole ? '20rpx' : '0' }">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">{{ isAdminRole ? '待回复留言' : '我的留言记录' }}</view>
        <text class="hint">{{ rows.length }} 条</text>
      </view>

      <view v-if="rows.length">
        <view class="msg-row" v-for="row in rows" :key="row.id">
          <view class="msg-question">Q：{{ row.questionContent }}</view>
          <view v-if="!isAdminRole" class="msg-answer">
            A：{{ row.answerContent || '暂未回复' }}
          </view>
          <view v-if="!isAdminRole && row.replyUserName" class="msg-replier">
            回复人：{{ row.replyUserName }}
          </view>

          <view v-if="isAdminRole" class="reply-block">
            <textarea class="uf-input answer-input" v-model="replyMap[row.id]" placeholder="输入回复内容" />
            <button class="uf-btn-primary mini" @click="reply(row)">回复</button>
          </view>

          <view class="meta-row">
            <text>状态：{{ row.status }}</text>
            <text>{{ formatDate(row.createTime) }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无数据。</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'

export default {
  data() {
    return {
      userRole: '',
      questionContent: '',
      rows: [],
      replyMap: {}
    }
  },
  computed: {
    isAdminRole() {
      return this.userRole === 'admin'
    }
  },
  async onShow() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
    await this.loadRows()
  },
  methods: {
    async loadCurrentUser() {
      const latestUser = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      const user = {
        ...localUser,
        ...latestUser,
        token: localUser.token
      }
      setUser(user)
      this.userRole = user.userRole || ''
    },
    formatDate(v) {
      if (!v) return ''
      return String(v).slice(0, 16).replace('T', ' ')
    },
    async loadRows() {
      if (this.isAdminRole) {
        this.rows = await request({ url: '/message/pending', showError: false }) || []
      } else {
        this.rows = await request({ url: '/message/my', showError: false }) || []
      }
    },
    async submitQuestion() {
      if (!this.questionContent.trim()) {
        uni.showToast({ title: '请输入留言内容', icon: 'none' })
        return
      }
      await request({
        url: '/message/submit',
        method: 'POST',
        data: { questionContent: this.questionContent.trim() }
      })
      this.questionContent = ''
      uni.showToast({ title: '提交成功', icon: 'success' })
      await this.loadRows()
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

.question {
  min-height: 180rpx;
  width: 100%;
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

.msg-row {
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
  background: #fff;
}

.msg-question {
  font-size: 26rpx;
  color: $text-primary;
  line-height: 1.5;
}

.msg-answer {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: $text-secondary;
  line-height: 1.5;
}

.msg-replier {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $text-muted;
}

.reply-block {
  margin-top: 12rpx;
}

.answer-input {
  min-height: 120rpx;
  width: 100%;
  box-sizing: border-box;
  margin-bottom: 10rpx;
}

.mini {
  height: 64rpx;
  line-height: 64rpx;
  font-size: 24rpx;
}

.meta-row {
  margin-top: 10rpx;
  display: flex;
  justify-content: space-between;
  color: $text-muted;
  font-size: 22rpx;
}

.empty {
  color: $text-secondary;
  font-size: 24rpx;
}
</style>
