<template>
  <view class="cat-page">
    <scroll-view scroll-y class="cat-list" v-if="!loading">
      <view v-for="c in categories" :key="c.name" class="cat-card" @click="goGoods(c.name)">
        <view class="cat-icon-wrap" :style="{background:c.color}"><text class="cat-icon">{{ c.icon }}</text></view>
        <view class="cat-info"><text class="cat-name">{{ c.name }}</text><text class="cat-count">{{ c.count }}+ 件</text></view>
        <text class="cat-arrow">→</text>
      </view>
    </scroll-view>
    <view v-else class="skeleton-list">
      <view v-for="i in 6" :key="i" class="skeleton" style="height:100rpx;margin:12rpx 24rpx;border-radius:12rpx" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const categories = [
  { name:'发动机', icon:'🔧', count:320, color:'#EFF6FF' }, { name:'刹车系统', icon:'🛑', count:180, color:'#FFF7ED' },
  { name:'滤清器', icon:'🔍', count:150, color:'#EFF6FF' }, { name:'点火系统', icon:'⚡', count:120, color:'#FFF7ED' },
  { name:'底盘系统', icon:'🔩', count:200, color:'#F5F3FF' }, { name:'传感器', icon:'📡', count:95, color:'#ECFEFF' },
  { name:'悬挂系统', icon:'🏗️', count:130, color:'#F0FDF4' }, { name:'车身附件', icon:'🚗', count:250, color:'#F8FAFC' },
  { name:'车灯照明', icon:'💡', count:110, color:'#FFFBEB' }
]
const loading = ref(false)
function goGoods(cat) { uni.navigateTo({ url:`/pages/goods/list?category=${cat}` }) }
onMounted(() => { loading.value = false })
</script>

<style scoped lang="scss">
.cat-page { background: #F8FAFC; min-height: 100vh; }
.cat-card { display: flex; align-items: center; gap: 20rpx; background: #fff; margin: 12rpx 24rpx; padding: 24rpx; border-radius: 16rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,.04); }
.cat-icon-wrap { width: 72rpx; height: 72rpx; border-radius: 20rpx; display: flex; align-items: center; justify-content: center; }
.cat-icon { font-size: 36rpx; }
.cat-info { flex:1; display:flex; flex-direction:column; gap:4rpx; }
.cat-name { font-size: 30rpx; font-weight: 600; color: #0F172A; }
.cat-count { font-size: 24rpx; color: #94A3B8; }
.cat-arrow { font-size: 28rpx; color: #CBD5E1; }
.skeleton-list { padding: 16rpx 0; }
</style>
