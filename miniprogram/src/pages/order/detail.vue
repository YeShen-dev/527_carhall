<template>
  <view class="od-page">
    <view v-if="!loading && order" class="od-card">
      <view class="od-hd">
        <text class="od-no">{{ order.orderNo }}</text>
        <text class="od-status" :style="{color:statusColor(order.status)}">{{ statusLabel(order.status) }}</text>
      </view>
      <view v-for="item in order.items" :key="item.productId" class="od-item">
        <image v-if="item.productImage" :src="item.productImage" mode="aspectFill" class="odi-img" />
        <view v-else class="odi-img-fb"><text>📦</text></view>
        <view class="odi-info">
          <text class="odi-name">{{ item.productName }}</text>
          <text class="odi-price">¥{{ item.price }} x{{ item.quantity }}</text>
        </view>
        <text class="odi-sub">¥{{ item.subtotal }}</text>
      </view>
      <view class="od-total">合计: ¥{{ order.totalAmount }}</view>
      <view class="od-date">下单时间: {{ order.createdAt }}</view>
    </view>
    <view v-else-if="loading" class="skeleton-detail">
      <view class="skeleton" style="height:100rpx;margin:24rpx" /><view class="skeleton" style="height:160rpx;margin:24rpx" />
    </view>
    <view v-else class="empty-state"><text>订单不存在</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getOrderDetail } from '../../api/order.js'

const order = ref(null); const loading = ref(true)
onLoad(async opts=>{ try{ const r=await getOrderDetail(opts.id); order.value=r.data }finally{loading.value=false} })

function statusLabel(s){ const m={PENDING:'待支付',PROCESSING:'支付中',PAID:'已支付',FAILED:'支付失败',CANCELLED:'已取消',SHIPPED:'已发货',COMPLETED:'已完成',REFUNDING:'退款中',REFUNDED:'已退款'}; return m[s]||s }
function statusColor(s){ const m={PENDING:'#F59E0B',PAID:'#10B981',FAILED:'#EF4444',SHIPPED:'#2563EB',COMPLETED:'#10B981'}; return m[s]||'#475569' }
</script>

<style scoped lang="scss">
.od-page { background:#F8FAFC; min-height:100vh; padding:24rpx; }
.od-card { background:#fff; border-radius:16rpx; padding:24rpx; }
.od-hd { display:flex; justify-content:space-between; align-items:center; margin-bottom:20rpx; padding-bottom:20rpx; border-bottom:1rpx solid #F1F5F9; }
.od-no { font-size:24rpx; color:#94A3B8; font-family:monospace; }
.od-status { font-size:28rpx; font-weight:600; }
.od-item { display:flex; align-items:center; gap:16rpx; margin-bottom:16rpx; }
.odi-img { width:80rpx; height:80rpx; border-radius:10rpx; }
.odi-img-fb { width:80rpx; height:80rpx; border-radius:10rpx; background:#F1F5F9; display:flex; align-items:center; justify-content:center; font-size:36rpx; }
.odi-info { flex:1; display:flex; flex-direction:column; gap:4rpx; }
.odi-name { font-size:26rpx; font-weight:600; color:#0F172A; }
.odi-price { font-size:22rpx; color:#94A3B8; }
.odi-sub { font-size:28rpx; font-weight:700; color:#EF4444; }
.od-total { text-align:right; font-size:28rpx; color:#0F172A; font-weight:700; padding-top:20rpx; border-top:1rpx solid #F1F5F9; }
.od-date { text-align:right; font-size:22rpx; color:#94A3B8; margin-top:8rpx; }
.empty-state { text-align:center; padding:100rpx 0; color:#94A3B8; }
</style>
