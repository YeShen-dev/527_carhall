<template>
  <view class="user-page">
    <!-- Header -->
    <view class="user-header">
      <view class="uh-bg" />
      <view class="uh-content">
        <image v-if="userStore.isLoggedIn" :src="userStore.userInfo?.avatar" class="uh-avatar" mode="aspectFill" />
        <view v-else class="uh-avatar-fb" @click="goLogin"><text>👤</text></view>
        <template v-if="userStore.isLoggedIn">
          <text class="uh-name">{{ userStore.userInfo?.username }}</text>
          <text class="uh-role" :class="userStore.isAdmin?'admin':''">{{ userStore.isAdmin?'管理员':'普通用户' }}</text>
        </template>
        <template v-else>
          <text class="uh-name" @click="goLogin">点击登录</text>
        </template>
      </view>
      <!-- Stats -->
      <view class="uh-stats" v-if="userStore.isLoggedIn">
        <view class="uhs-item" v-for="s in stats" :key="s.label" @click="s.action">
          <text class="uhs-num">{{ s.num }}</text><text class="uhs-label">{{ s.label }}</text>
        </view>
      </view>
    </view>

    <!-- Menu -->
    <view class="user-menu">
      <view v-for="m in menus" :key="m.label" class="um-item" @click="m.action">
        <text class="um-icon">{{ m.icon }}</text>
        <text class="um-label">{{ m.label }}</text>
        <text class="um-arrow">→</text>
      </view>
    </view>

    <!-- Logout -->
    <view class="user-menu" v-if="userStore.isLoggedIn" style="margin-top:24rpx">
      <view class="um-item" @click="handleLogout"><text class="um-icon">🚪</text><text class="um-label" style="color:#EF4444">退出登录</text><text class="um-arrow">→</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user.js'

const userStore = useUserStore()

const stats = [
  { label:'待付款', num:'0', action:()=>uni.navigateTo({url:'/pages/order/list?tab=0'}) },
  { label:'待发货', num:'0', action:()=>uni.navigateTo({url:'/pages/order/list?tab=1'}) },
  { label:'待收货', num:'0', action:()=>uni.navigateTo({url:'/pages/order/list?tab=2'}) },
  { label:'已完成', num:'0', action:()=>uni.navigateTo({url:'/pages/order/list?tab=3'}) }
]
const menus = [
  { icon:'📋', label:'我的订单', action:()=>checkAuth('/pages/order/list') },
  { icon:'💬', label:'我的评论', action:()=>checkAuth('/pages/comment/my') },
  { icon:'❤️', label:'我的收藏', action:()=>checkAuth('/pages/user/collection') },
  { icon:'📍', label:'收货地址', action:()=>checkAuth('/pages/user/address') },
  { icon:'📝', label:'个人资料', action:()=>checkAuth('/pages/user/info') },
  { icon:'🤖', label:'AI配件顾问', action:()=>uni.navigateTo({url:'/pages/ai/index'}) },
  { icon:'📞', label:'联系客服', action:()=>uni.showToast({title:'客服电话: 18029538913',icon:'none',duration:5000}) }
]

function checkAuth(path) {
  if (!userStore.isLoggedIn) return uni.navigateTo({ url:'/pages/login/login' })
  uni.navigateTo({ url: path })
}

function goLogin() { uni.navigateTo({ url:'/pages/login/login' }) }

function handleLogout() {
  uni.showModal({ title:'提示', content:'确认退出登录？', success(r){ if(r.confirm){ userStore.logout(); uni.switchTab({url:'/pages/index/index'}) } } })
}
</script>

<style scoped lang="scss">
.user-page { background: #F8FAFC; min-height: 100vh; }
.user-header { position:relative; padding-bottom:32rpx; }
.uh-bg { height:240rpx; background:linear-gradient(135deg,#1E3A8A,#2563EB,#3B82F6); border-radius:0 0 40rpx 40rpx; }
.uh-content { display:flex;flex-direction:column;align-items:center;margin-top:-80rpx;position:relative;z-index:1; }
.uh-avatar { width:140rpx;height:140rpx;border-radius:50%;border:4rpx solid #fff;box-shadow:0 4rpx 16rpx rgba(0,0,0,.1); }
.uh-avatar-fb { width:140rpx;height:140rpx;border-radius:50%;border:4rpx solid #fff;background:#F1F5F9;display:flex;align-items:center;justify-content:center;font-size:56rpx; }
.uh-name { font-size:36rpx;font-weight:700;color:#0F172A;margin-top:16rpx; }
.uh-role { font-size:22rpx;padding:4rpx 16rpx;border-radius:20rpx;background:#EFF6FF;color:#2563EB;margin-top:8rpx; }
.uh-role.admin { background:#FEF2F2;color:#EF4444; }
.uh-stats { display:grid;grid-template-columns:repeat(4,1fr);background:#fff;margin:24rpx 24rpx;border-radius:16rpx;padding:24rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.uhs-item { display:flex;flex-direction:column;align-items:center;gap:8rpx; }
.uhs-num { font-size:36rpx;font-weight:800;color:#0F172A; }
.uhs-label { font-size:22rpx;color:#94A3B8; }
.user-menu { background:#fff;margin:0 24rpx;border-radius:16rpx;overflow:hidden;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.um-item { display:flex;align-items:center;gap:16rpx;padding:28rpx 24rpx;border-bottom:1rpx solid #F8FAFC; }
.um-icon { font-size:28rpx; } .um-label { flex:1;font-size:28rpx;color:#0F172A; } .um-arrow { font-size:24rpx;color:#CBD5E1; }
</style>
