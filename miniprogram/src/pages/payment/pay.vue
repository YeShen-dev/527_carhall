<template>
  <view class="pay-page">
    <view class="pay-card">
      <text class="pay-title">确认支付</text>
      <text class="pay-amount">¥{{ amount }}</text>
      <text class="pay-order">订单号: {{ orderId }}</text>

      <view class="pay-methods">
        <view class="pm-item active" @click="payMethod='WECHAT'">
          <text class="pm-icon">💚</text>
          <view><text class="pm-name">微信支付</text><text class="pm-desc">推荐使用</text></view>
          <text class="pm-check">✅</text>
        </view>
        <view class="pm-item" @click="payMethod='ALIPAY'">
          <text class="pm-icon">💙</text>
          <view><text class="pm-name">支付宝</text></view>
        </view>
      </view>

      <button class="pay-btn" :loading="paying" @click="handlePay">
        立即支付 ¥{{ amount }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createPayment, queryPaymentStatus } from '../../api/payment.js'

const orderId = ref('')
const amount = ref('0.00')
const payMethod = ref('WECHAT')
const paying = ref(false)

onLoad(opts => { orderId.value = opts.orderId || ''; amount.value = opts.amount || '0.00' })

async function handlePay() {
  paying.value = true
  try {
    const res = await createPayment(orderId.value, payMethod.value)
    const payData = res.data

    if (payMethod.value === 'WECHAT') {
      // In mock mode, simulate WeChat pay
      if (payData.qrCodeUrl?.startsWith('weixin://')) {
        // Real WeChat Pay flow would invoke wx.requestPayment here
        uni.showToast({ title: '微信支付(模拟)', icon: 'none' })
        await pollStatus()
      }
    } else {
      uni.showToast({ title: '支付宝支付(模拟)', icon: 'none' })
      await pollStatus()
    }
  } catch (e) {
    uni.showToast({ title: e.message || '支付失败', icon: 'none' })
  } finally { paying.value = false }
}

async function pollStatus() {
  let attempts = 0
  const timer = setInterval(async () => {
    try {
      attempts++
      const r = await queryPaymentStatus(orderId.value)
      if (r.data?.status === 'PAID') {
        clearInterval(timer)
        uni.showToast({ title: '支付成功', icon: 'success' })
        setTimeout(() => uni.redirectTo({ url: `/pages/order/detail?id=${orderId.value}` }), 1000)
      } else if (attempts > 60) {
        clearInterval(timer)
        uni.showToast({ title: '支付确认超时', icon: 'none' })
      }
    } catch { /* continue */ }
  }, 2000)
}
</script>

<style scoped lang="scss">
.pay-page { background:#F8FAFC; min-height:100vh; padding:48rpx 24rpx; }
.pay-card { background:#fff; border-radius:24rpx; padding:48rpx 32rpx; box-shadow:0 4rpx 24rpx rgba(0,0,0,.06); }
.pay-title { font-size:32rpx; font-weight:700; color:#0F172A; text-align:center; display:block; }
.pay-amount { font-size:72rpx; font-weight:900; color:#EF4444; text-align:center; display:block; margin:20rpx 0 8rpx; }
.pay-order { font-size:24rpx; color:#94A3B8; text-align:center; display:block; margin-bottom:40rpx; }
.pay-methods { display:flex;flex-direction:column;gap:16rpx;margin-bottom:32rpx; }
.pm-item { display:flex;align-items:center;gap:16rpx;background:#F8FAFC;border-radius:16rpx;padding:24rpx;border:2rpx solid transparent; }
.pm-item.active { border-color:#2563EB;background:#EFF6FF; }
.pm-icon { font-size:40rpx; }
.pm-name { font-size:28rpx;font-weight:600;color:#0F172A;display:block; }
.pm-desc { font-size:22rpx;color:#94A3B8; }
.pm-check { margin-left:auto;font-size:32rpx; }
.pay-btn { background:linear-gradient(135deg,#EF4444,#DC2626);color:#fff;border:none;border-radius:40rpx;font-size:32rpx;font-weight:700;padding:22rpx; }
</style>
