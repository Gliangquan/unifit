<template>
</template>

<script>
export default {
  onLaunch() {
    console.log('App Launch')
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import '@/uni_modules/uni-scss/index.scss';
@import '@/styles/common.scss';

page {
  background: $bg-page;
  color: $text-primary;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

.uf-page {
  padding: $spacing-md;
  min-height: 100vh;
  box-sizing: border-box;
}

.uf-card {
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(17, 24, 39, 0.05);
}

.uf-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $text-primary;
}

.uf-subtitle {
  margin-top: 8rpx;
  color: $text-secondary;
  font-size: 26rpx;
  line-height: 1.4;
}

.uf-section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-md;
}

.uf-input,
.uf-picker {
  background: #fff;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  padding: 18rpx 20rpx;
  margin-bottom: $spacing-md;
  font-size: 26rpx;
  color: $text-primary;
  box-sizing: border-box;
}

.uf-btn-primary {
  background: $primary-color;
  color: #fff;
  border-radius: $radius-md;
  border: none;
  font-size: 26rpx;
  font-weight: 600;
  padding: 16rpx 26rpx;
}

.uf-btn-primary[disabled] {
  opacity: 0.6;
}

.uf-btn-secondary {
  background: #fff;
  color: $text-primary;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  font-size: 24rpx;
  font-weight: 600;
  padding: 14rpx 22rpx;
}

.uf-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8rpx 16rpx;
  border-radius: $radius-full;
  font-size: 22rpx;
  background: $primary-light;
  color: $primary-color;
}

.uf-list-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-sm 0;
  border-bottom: 1px dashed $border-color;
  font-size: 26rpx;
}

.uf-list-row:last-child {
  border-bottom: 0;
}

.uf-fade-up {
  animation: fadeUp 0.2s ease both;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(18rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

button::after {
  border: 0;
}
</style>
