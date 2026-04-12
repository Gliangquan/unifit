<template>
  <view class="page-container">
    <!-- 头像编辑卡片 -->
    <view class="profile-header-card">
      <view class="avatar-wrapper" @click="chooseAvatar">
        <!-- 预览头像 / 默认占位 -->
        <view class="avatar-circle" :class="{ 'avatar-uploading': uploading }">
          <image
            v-if="avatarPreview"
            :src="avatarPreview"
            class="avatar-img"
            mode="aspectFill"
          />
          <uni-icons v-else type="person-filled" size="52" color="#c1c9d2"></uni-icons>

          <!-- 上传遮罩 -->
          <view v-if="uploading" class="avatar-mask">
            <uni-icons type="spinner-cycle" size="28" color="#fff"></uni-icons>
          </view>
        </view>

        <!-- 相机图标角标 -->
        <view class="camera-badge">
          <uni-icons type="camera-filled" size="14" color="#fff"></uni-icons>
        </view>
      </view>

      <view class="user-meta">
        <text class="user-display-name">{{ user.userName || '未设置昵称' }}</text>
        <text class="user-account-text">{{ user.userAccount || '-' }}</text>
        <text v-if="uploadTip" class="upload-tip" :class="uploadTipClass">{{ uploadTip }}</text>
      </view>
    </view>

    <!-- 可编辑字段 -->
    <view class="section-container">
      <view class="section-header">修改信息</view>

      <view class="edit-item">
        <view class="edit-item-label">
          <uni-icons type="person" size="18" color="#64748b"></uni-icons>
          <text class="edit-label-text">昵称</text>
        </view>
        <input
          class="edit-input"
          v-model="form.userName"
          placeholder="请输入新昵称"
          placeholder-class="input-placeholder"
          maxlength="20"
        />
      </view>

      <view class="edit-item">
        <view class="edit-item-label">
          <uni-icons type="email" size="18" color="#64748b"></uni-icons>
          <text class="edit-label-text">邮箱</text>
        </view>
        <input
          class="edit-input"
          v-model="form.userEmail"
          placeholder="请输入邮箱（可选）"
          placeholder-class="input-placeholder"
          type="email"
        />
      </view>
    </view>

    <!-- 只读信息 -->
    <view class="section-container">
      <view class="section-header">基本信息</view>
      <view class="info-row">
        <view class="info-row-label">
          <uni-icons type="phone" size="18" color="#64748b"></uni-icons>
          <text class="info-label-text">账号</text>
        </view>
        <text class="info-value-text">{{ user.userAccount || '-' }}</text>
      </view>
      <view class="info-row no-border">
        <view class="info-row-label">
          <uni-icons type="vip" size="18" color="#64748b"></uni-icons>
          <text class="info-label-text">角色</text>
        </view>
        <text class="info-value-text">{{ isAdmin ? '管理员' : '学生' }}</text>
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="btn-area">
      <button
        class="save-btn"
        :disabled="saving || uploading"
        :loading="saving"
        @click="saveProfile"
      >保存修改</button>
    </view>
  </view>
</template>

<script>
import { BASE_URL } from '@/common/config'
import { request } from '@/common/request'
import { ensureLogin, setUser } from '@/common/auth'
import { resolveAssetUrl } from '@/common/asset'

export default {
  data() {
    return {
      user: {},
      form: {
        userName: '',
        userEmail: '',
        userAvatar: ''   // 已上传到服务器的 URL
      },
      avatarPreview: '',  // 本地预览路径 or 服务器 URL
      uploading: false,
      saving: false,
      uploadTip: '',
      uploadTipClass: ''
    }
  },

  computed: {
    isAdmin() {
      return (this.user.userRole || 'student') === 'admin'
    }
  },

  async onLoad() {
    if (!ensureLogin()) return
    await this.loadCurrentUser()
  },

  methods: {
    async loadCurrentUser() {
      const latest = await request({ url: '/user/get/login', showError: false }) || {}
      const localUser = uni.getStorageSync('user') || {}
      this.user = { ...localUser, ...latest, token: localUser.token }
      setUser(this.user)
      this.form.userName  = this.user.userName  || ''
      this.form.userEmail = this.user.userEmail || ''
      this.form.userAvatar = this.user.userAvatar || ''
      this.avatarPreview  = resolveAssetUrl(this.user.userAvatar)
    },

    // ── 选图 → 上传 ──────────────────────────────────────────
    chooseAvatar() {
      if (this.uploading) return
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempPath = res.tempFilePaths[0]
          this.avatarPreview = tempPath   // 立即预览
          this.uploadAvatar(tempPath)
        }
      })
    },

    uploadAvatar(filePath) {
      this.uploading = true
      this.uploadTip = '上传中...'
      this.uploadTipClass = 'tip-info'

      const user = uni.getStorageSync('user') || {}
      uni.uploadFile({
        url: `${BASE_URL}/user/avatar/upload`,
        filePath,
        name: 'file',
        header: user.token ? { Authorization: `Bearer ${user.token}` } : {},
        success: (uploadRes) => {
          try {
            const body = JSON.parse(uploadRes.data)
            if (body.code === 0) {
              this.form.userAvatar = body.data   // 服务器返回的永久 URL
              this.avatarPreview   = resolveAssetUrl(body.data)
              this.uploadTip = '头像上传成功 ✓'
              this.uploadTipClass = 'tip-success'
            } else {
              this.avatarPreview = resolveAssetUrl(this.user.userAvatar)  // 回滚预览
              this.form.userAvatar = this.user.userAvatar || ''
              this.uploadTip = body.message || '上传失败'
              this.uploadTipClass = 'tip-error'
            }
          } catch (e) {
            this.avatarPreview = resolveAssetUrl(this.user.userAvatar)
            this.uploadTip = '上传失败，请重试'
            this.uploadTipClass = 'tip-error'
          }
        },
        fail: () => {
          this.avatarPreview = resolveAssetUrl(this.user.userAvatar)
          this.uploadTip = '网络异常，上传失败'
          this.uploadTipClass = 'tip-error'
        },
        complete: () => {
          this.uploading = false
          // 3 秒后清除提示
          setTimeout(() => { this.uploadTip = '' }, 3000)
        }
      })
    },

    // ── 保存资料 ─────────────────────────────────────────────
    async saveProfile() {
      if (this.saving || this.uploading) return
      this.saving = true
      try {
        const data = {}
        const name = (this.form.userName || '').trim()
        const email = (this.form.userEmail || '').trim()
        if (name)  data.userName  = name
        if (email) data.userEmail = email
        if (this.form.userAvatar) data.userAvatar = this.form.userAvatar

        await request({ url: '/user/update/my', method: 'POST', data })
        uni.showToast({ title: '保存成功', icon: 'success' })
        await this.loadCurrentUser()
        setTimeout(() => uni.navigateBack(), 1200)
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/common.scss';

.page-container {
  min-height: 100vh;
  background-color: #f7f9fc;
  padding-bottom: 80rpx;
}

.header-bg {
  display: none;
}

/* ── 头像卡片 ── */
.profile-header-card {
  position: relative;
  z-index: 1;
  margin: 16rpx 16rpx 16rpx;
  padding: 24rpx;
  background: #fff;
  border: 1rpx solid #e5e7eb;
  border-radius: $radius-md;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.04);
  display: flex;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-right: 30rpx;
  flex-shrink: 0;
}

.avatar-circle {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background-color: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid #fff;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.08);
  overflow: hidden;
  position: relative;

  &.avatar-uploading {
    opacity: 0.7;
  }
}

.avatar-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.35);
  border-radius: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  border-radius: 20rpx;
  background: $primary-color;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid #fff;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.15);
}

.user-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-display-name {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8rpx;
}

.user-account-text {
  font-size: 26rpx;
  color: #64748b;
}

.upload-tip {
  font-size: 22rpx;
  margin-top: 10rpx;
  font-weight: 500;
}
.tip-info    { color: #64748b; }
.tip-success { color: #10b981; }
.tip-error   { color: #ef4444; }

/* ── 通用卡片 ── */
.section-container {
  position: relative;
  z-index: 1;
  margin: 0 30rpx 24rpx;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
}

.section-header {
  padding: 28rpx 30rpx 10rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;

  &::before {
    content: '';
    display: block;
    width: 8rpx;
    height: 28rpx;
    background: #F97316;
    border-radius: 4rpx;
    margin-right: 16rpx;
  }
}

/* ── 编辑项 ── */
.edit-item {
  padding: 24rpx 30rpx;
  border-bottom: 1px solid #f0f4f8;
  &:last-child { border-bottom: none; }
}

.edit-item-label {
  display: flex;
  align-items: center;
  margin-bottom: 14rpx;
}

.edit-label-text {
  font-size: 26rpx;
  color: #475569;
  font-weight: 500;
  margin-left: 10rpx;
}

.edit-input {
  width: 100%;
  height: 80rpx;
  background: #f7f9fc;
  border: 1px solid #e2e8f0;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  color: #1f2937;
  box-sizing: border-box;
}

.input-placeholder {
  color: #cbd5e1;
  font-size: 28rpx;
}

/* ── 只读行 ── */
.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 30rpx;
  border-bottom: 1px solid #f0f4f8;
  &.no-border { border-bottom: none; }
}

.info-row-label {
  display: flex;
  align-items: center;
}

.info-label-text {
  font-size: 28rpx;
  color: #475569;
  margin-left: 12rpx;
}

.info-value-text {
  font-size: 28rpx;
  color: #94a3b8;
}

/* ── 保存按钮 ── */
.btn-area {
  margin: 40rpx 30rpx 0;
}

.save-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
  color: #fff;
  border-radius: 20rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
  border: none;
  box-shadow: 0 8rpx 20rpx rgba(249, 115, 22, 0.30);
  letter-spacing: 4rpx;

  &[disabled] { opacity: 0.65; }
  &::after { border: none; }
}
</style>
