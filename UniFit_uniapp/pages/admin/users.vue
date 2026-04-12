<template>
  <view class="uf-page">
    <view class="uf-card uf-fade-up">
      <view class="uf-section-title">用户筛选</view>
      <input class="uf-input" v-model="query.userName" placeholder="用户名" />
      <input class="uf-input" v-model="query.userPhone" placeholder="手机号" />
      <picker class="uf-picker" :range="roles" @change="onRoleChange">
        <view>角色：{{ query.userRole || '全部' }}</view>
      </picker>
      <button class="uf-btn-primary" @click="loadRows(true)">查询</button>
    </view>

    <view class="uf-card uf-fade-up" style="margin-top:20rpx;">
      <view class="row-between">
        <view class="uf-section-title" style="margin:0;">用户列表</view>
        <text class="hint">共 {{ total }} 条</text>
      </view>

      <view v-if="rows.length">
        <view class="row" v-for="row in rows" :key="row.id">
          <view class="name">{{ row.userName || row.userAccount }}</view>
          <view class="sub">账号：{{ row.userAccount }} · {{ row.userRole }} · {{ row.userPhone || '-' }}</view>
          <view class="sub">状态：{{ row.status === 1 ? '启用' : '禁用' }} · 余额：￥{{ Number(row.balance || 0).toFixed(2) }}</view>
          <view class="ops">
            <button class="uf-btn-secondary mini" @click="toggle(row)">{{ row.status === 1 ? '禁用' : '启用' }}</button>
          </view>
        </view>
      </view>
      <view v-else class="empty">暂无数据。</view>

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

export default {
  data() {
    return {
      roles: ['全部', 'student', 'admin', 'ban'],
      query: {
        current: 1,
        pageSize: 10,
        userName: '',
        userPhone: '',
        userRole: ''
      },
      rows: [],
      total: 0
    }
  },
  onShow() {
    if (!ensureAdmin()) return
    this.loadRows(true)
  },
  methods: {
    onRoleChange(e) {
      const role = this.roles[Number(e.detail.value)]
      this.query.userRole = role === '全部' ? '' : role
    },
    async loadRows(reset = false) {
      if (reset) this.query.current = 1
      const res = await request({
        url: '/user/list/page',
        method: 'POST',
        data: {
          current: this.query.current,
          pageSize: this.query.pageSize,
          userName: this.query.userName || undefined,
          userPhone: this.query.userPhone || undefined,
          userRole: this.query.userRole || undefined
        },
        showError: false
      }) || {}
      this.rows = res.records || []
      this.total = Number(res.total || 0)
    },
    async toggle(row) {
      await request({
        url: `/admin/user/status?userId=${row.id}&status=${row.status === 1 ? 0 : 1}`,
        method: 'POST'
      })
      uni.showToast({ title: '状态已更新', icon: 'success' })
      await this.loadRows()
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
  margin-top: 10rpx;
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
