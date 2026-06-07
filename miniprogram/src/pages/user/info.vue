<template>
  <view class="info-page">
    <view class="info-card">
      <view class="info-avatar">
        <text>{{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() || 'U' }}</text>
      </view>
      <view class="info-form">
        <view class="if-item"><text class="ifl">用户名</text><text class="ifv">{{ userStore.userInfo?.username }}</text></view>
        <view class="if-item"><text class="ifl">手机号</text><input v-model="phone" placeholder="请输入手机号" class="if-input" /></view>
        <view class="if-item"><text class="ifl">邮箱</text><input v-model="email" placeholder="请输入邮箱" class="if-input" /></view>
      </view>
      <button class="info-save" @click="save">保存修改</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../store/user.js'

const userStore = useUserStore()
const phone = ref(userStore.userInfo?.phone || '')
const email = ref(userStore.userInfo?.email || '')

async function save() {
  try { await userStore.updateProfile({ phone:phone.value, email:email.value }); uni.showToast({ title:'保存成功', icon:'success' }) }
  catch { uni.showToast({ title:'保存失败', icon:'none' }) }
}
</script>

<style scoped lang="scss">
.info-page { background:#F8FAFC;min-height:100vh;padding:24rpx; }
.info-card { background:#fff;border-radius:24rpx;padding:40rpx 32rpx;display:flex;flex-direction:column;align-items:center; }
.info-avatar { width:120rpx;height:120rpx;border-radius:50%;background:linear-gradient(135deg,#2563EB,#3B82F6);display:flex;align-items:center;justify-content:center;font-size:48rpx;font-weight:800;color:#fff;margin-bottom:32rpx; }
.info-form { width:100%; }
.if-item { display:flex;align-items:center;padding:24rpx 0;border-bottom:1rpx solid #F1F5F9; }
.ifl { width:120rpx;font-size:28rpx;color:#64748B; }
.ifv { font-size:28rpx;color:#0F172A; }
.if-input { flex:1;font-size:28rpx;color:#0F172A;text-align:right; }
.info-save { width:100%;background:linear-gradient(135deg,#2563EB,#3B82F6);color:#fff;border:none;border-radius:12rpx;padding:20rpx;font-size:28rpx;margin-top:32rpx; }
</style>
