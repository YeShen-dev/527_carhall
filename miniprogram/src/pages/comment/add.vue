<template>
  <view class="comment-add">
    <view class="ca-card">
      <text class="ca-product">{{ productName }}</text>
      <text class="ca-label">评分</text>
      <view class="ca-stars">
        <text v-for="s in 5" :key="s" class="ca-star" :class="{ active: s <= rating }"
          @click="rating = s">{{ s <= rating ? '★' : '☆' }}</text>
      </view>

      <text class="ca-label">评价内容</text>
      <textarea v-model="content" placeholder="分享你的使用体验 (10-500字)" maxlength="500" class="ca-textarea" />
      <text class="ca-count">{{ content.length }}/500</text>

      <view class="ca-options">
        <view class="ca-opt" @click="anonymous = !anonymous">
          <text :style="{color:anonymous?'#2563EB':'#94A3B8'}">{{ anonymous ? '👻 匿名发布' : '👤 实名发布' }}</text>
        </view>
      </view>

      <button class="ca-submit" :disabled="loading" @click="handleSubmit">
        {{ loading ? '提交中...' : isAppend ? '发布追评' : '发布评价' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '../../api/request.js'

const productId = ref('')
const orderId = ref('')
const productName = ref('')
const isAppend = ref(0)
const rating = ref(5)
const content = ref('')
const anonymous = ref(false)
const loading = ref(false)

onLoad(opts => {
  productId.value = opts.productId || ''
  orderId.value = opts.orderId || ''
  productName.value = decodeURIComponent(opts.productName || '')
})

async function handleSubmit() {
  if (!content.value.trim() || content.value.trim().length < 10) {
    return uni.showToast({ title: '评论内容至少10个字', icon: 'none' })
  }
  loading.value = true
  try {
    await request.post('/comment/add', {
      productId: Number(productId.value),
      orderId: Number(orderId.value),
      content: content.value.trim(),
      rating: rating.value,
      isAnonymous: anonymous.value ? 1 : 0,
      isAppend: isAppend.value
    })
    uni.showToast({ title: '评价成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1500)
  } catch (e) {
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  } finally { loading.value = false }
}
</script>

<style scoped lang="scss">
.comment-add { background: #F8FAFC; min-height: 100vh; padding: 24rpx; }
.ca-card { background: #fff; border-radius: 20rpx; padding: 32rpx; }
.ca-product { font-size: 32rpx; font-weight: 700; color: #0F172A; display: block; margin-bottom: 28rpx; }
.ca-label { font-size: 26rpx; font-weight: 600; color: #475569; display: block; margin-bottom: 12rpx; margin-top: 24rpx; }
.ca-stars { display: flex; gap: 12rpx; }
.ca-star { font-size: 52rpx; color: #E2E8F0; transition: transform 0.15s; }
.ca-star.active { color: #F59E0B; transform: scale(1.1); }
.ca-textarea { width: 100%; height: 220rpx; background: #F1F5F9; border-radius: 12rpx; padding: 16rpx; font-size: 28rpx; box-sizing: border-box; }
.ca-count { text-align: right; font-size: 22rpx; color: #94A3B8; }
.ca-options { margin-top: 24rpx; }
.ca-opt { padding: 12rpx 0; }
.ca-submit { background: linear-gradient(135deg,#2563EB,#3B82F6); color: #fff; border: none; border-radius: 16rpx; font-size: 30rpx; font-weight: 600; padding: 22rpx; margin-top: 32rpx; }
.ca-submit[disabled] { background: #CBD5E1; }
</style>
