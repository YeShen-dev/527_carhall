<template>
  <view class="app-root">
    <slot />
  </view>
</template>

<script setup>
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  // Check for update
  const updateManager = uni.getUpdateManager()
  updateManager.onCheckForUpdate(res => {
    if (res.hasUpdate) {
      updateManager.onUpdateReady(() => {
        uni.showModal({
          title: '更新提示',
          content: '新版本已准备好，是否重启应用？',
          success: r => { if (r.confirm) updateManager.applyUpdate() }
        })
      })
    }
  })
})
</script>

<style lang="scss">
@import 'uview-plus/index.scss';
@import './uni.scss';

page {
  background-color: #F8FAFC;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', sans-serif;
  color: #0F172A;
  font-size: 28rpx;
  -webkit-font-smoothing: antialiased;
}

.app-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

// Safe area
.safe-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

// One-line text ellipsis
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// Two-line clamp
.text-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

// Card utility
.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin: 16rpx 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}

// Skeleton
.skeleton {
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: 8rpx;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
