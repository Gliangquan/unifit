<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">待审核学生</view>
        <text class="hint">{{ rows.length }} 人</text>
      </view>

      <view v-if="rows.length">
        <view class="row" v-for="row in rows" :key="row.id">
          <view class="main">
            <view class="name">{{ row.realName }}（{{ row.studentId }}）</view>
            <view class="sub">用户ID：{{ row.userId }} · 班级：{{ row.className || '-' }} · 状态：{{ row.verificationStatus }}</view>
          </view>
          <view class="ops">
            <button class="uf-btn-primary mini" @click="audit(row, 'approved')">通过</button>
            <button class="uf-btn-secondary mini" @click="audit(row, 'rejected')">拒绝</button>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无待审核数据。</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureAdmin } from '@/common/auth'

export default {
  data() {
    return {
      rows: []
    }
  },
  onShow() {
    if (!ensureAdmin()) return
    this.loadRows()
  },
  methods: {
    async loadRows() {
      this.rows = await request({ url: '/student/verify/pending', showError: false }) || []
    },
    async audit(row, verificationStatus) {
      await request({
        url: '/student/verify/audit',
        method: 'POST',
        data: {
          userId: row.userId,
          verificationStatus,
          rejectReason: verificationStatus === 'rejected' ? '信息不完整，请重新提交' : ''
        }
      })
      uni.showToast({ title: verificationStatus === 'approved' ? '审核通过' : '已拒绝', icon: 'success' })
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
  font-size: 26rpx;
  color: $text-primary;
}

.sub {
  margin-top: 6rpx;
  color: $text-secondary;
  font-size: 22rpx;
}

.ops {
  display: flex;
  gap: 10rpx;
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
