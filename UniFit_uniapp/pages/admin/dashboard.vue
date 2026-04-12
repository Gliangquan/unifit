<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="uf-section-title">管理看板</view>
      <view class="stat-grid">
        <view class="stat-item">
          <view class="stat-num">{{ stats.studentCount }}</view>
          <view class="stat-label">学生总数</view>
        </view>
        <view class="stat-item">
          <view class="stat-num">{{ stats.checkinLast7Days }}</view>
          <view class="stat-label">近7天打卡</view>
        </view>
        <view class="stat-item">
          <view class="stat-num">{{ stats.pendingStudentAudit || 0 }}</view>
          <view class="stat-label">待审核学生</view>
        </view>
        <view class="stat-item">
          <view class="stat-num">{{ stats.pendingMessages || 0 }}</view>
          <view class="stat-label">待回复留言</view>
        </view>
        <view class="stat-item">
          <view class="stat-num">{{ stats.activeClasses || 0 }}</view>
          <view class="stat-label">活跃班级</view>
        </view>
      </view>
      <view class="ops">
        <button class="uf-btn-secondary" @click="goExport">导出用户成绩CSV</button>
        <button class="uf-btn-secondary" @click="goClassExport">导出班级挑战CSV</button>
      </view>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">打卡排行榜</view>
        <picker :range="dayOptions" @change="onDayChange">
          <view class="picker-text">近{{ dayOptions[dayIndex] }}天</view>
        </picker>
      </view>
      <view v-if="ranking.length">
        <view class="rank-row" v-for="(item, idx) in ranking" :key="item.userId">
          <view>{{ idx + 1 }}. {{ item.userName || ('用户'+item.userId) }}</view>
          <view>{{ item.checkinCount }}次 / {{ item.totalDuration }}分</view>
        </view>
      </view>
      <view v-else class="empty">暂无数据</view>
    </view>
  </view>
</template>

<script>
import { request } from '@/common/request'
import { ensureAdmin, getUser } from '@/common/auth'
import { BASE_URL } from '@/common/config'

export default {
  data() {
    return {
      stats: { studentCount: 0, checkinLast7Days: 0, pendingStudentAudit: 0, pendingMessages: 0, activeClasses: 0 },
      ranking: [],
      dayOptions: [7, 30],
      dayIndex: 0
    }
  },
  onShow() {
    if (!ensureAdmin()) return
    this.loadAll()
  },
  methods: {
    onDayChange(e) {
      this.dayIndex = Number(e.detail.value)
      this.loadRanking()
    },
    async loadAll() {
      await Promise.all([this.loadStats(), this.loadRanking()])
    },
    async loadStats() {
      this.stats = await request({ url: '/admin/dashboard', showError: false }) || {
        studentCount: 0,
        checkinLast7Days: 0,
        pendingStudentAudit: 0,
        pendingMessages: 0,
        activeClasses: 0
      }
    },
    async loadRanking() {
      const days = this.dayOptions[this.dayIndex]
      this.ranking = await request({ url: `/checkin/ranking?days=${days}&topN=20`, showError: false }) || []
    },
    goExport() {
      const user = getUser()
      const token = user.token || ''
      uni.downloadFile({
        url: `${BASE_URL}/admin/export/users-scores`,
        header: { Authorization: `Bearer ${token}` },
        success: () => uni.showToast({ title: '导出请求已发送', icon: 'success' }),
        fail: () => uni.showToast({ title: '导出失败', icon: 'none' })
      })
    },
    goClassExport() {
      const user = getUser()
      const token = user.token || ''
      uni.downloadFile({
        url: `${BASE_URL}/admin/export/class-challenge?days=${this.dayOptions[this.dayIndex]}`,
        header: { Authorization: `Bearer ${token}` },
        success: () => uni.showToast({ title: '导出请求已发送', icon: 'success' }),
        fail: () => uni.showToast({ title: '导出失败', icon: 'none' })
      })
    }
  }
}
</script>

<style lang="scss">
@import "@/styles/common.scss";

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12rpx;
  margin-bottom: 16rpx;
}

.stat-item {
  border-radius: $radius-md;
  border: 1px solid #d7e1ec;
  background: $primary-light;
  padding: 16rpx;
  text-align: center;
}

.stat-num {
  font-size: 40rpx;
  color: #334155;
  font-weight: 700;
}

.stat-label {
  font-size: 22rpx;
  color: #64748b;
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.picker-text {
  font-size: 22rpx;
  color: $text-secondary;
}

.rank-row {
  padding: 14rpx 0;
  border-bottom: 1px dashed $border-color;
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
}

.empty {
  color: $text-secondary;
  font-size: 24rpx;
}

.ops {
  display: flex;
  gap: 12rpx;
}
</style>
