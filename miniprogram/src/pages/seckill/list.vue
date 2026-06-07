<template>
  <view class="sk-page">
    <!-- Hero countdown -->
    <view class="sk-hero">
      <text class="sk-hero-title">⚡ 限时秒杀</text>
      <text class="sk-hero-sub">超值配件 限时抢购</text>
      <view class="sk-countdown" v-if="countdown>0">
        <view class="sk-cd-item"><text class="cd-num">{{ pad2(Math.floor(countdown/3600)) }}</text><text class="cd-unit">时</text></view>
        <text class="cd-colon">:</text>
        <view class="sk-cd-item"><text class="cd-num">{{ pad2(Math.floor(countdown/60)%60) }}</text><text class="cd-unit">分</text></view>
        <text class="cd-colon">:</text>
        <view class="sk-cd-item"><text class="cd-num">{{ pad2(countdown%60) }}</text><text class="cd-unit">秒</text></view>
      </view>
    </view>

    <!-- Activity cards -->
    <view v-if="!loading" class="sk-list">
      <view v-for="a in activities" :key="a.id" class="sk-card" :class="{'sk-soldout':a.stock<=0}">
        <image v-if="a.productImage" :src="a.productImage" mode="aspectFill" class="sk-img" />
        <view v-else class="sk-img-fb"><text>📦</text></view>
        <view class="sk-body">
          <text class="sk-name">{{ a.productName }}</text>
          <view class="sk-prices"><text class="sk-price">¥{{ a.seckillPrice }}</text><text class="sk-origin" v-if="a.productPrice">¥{{ a.productPrice }}</text></view>
          <view class="sk-progress">
            <view class="sk-pg-bar"><view class="sk-pg-fill" :class="a.stock<=0?'fill-gray':''" :style="{width:pgPct(a)+'%'}" /></view>
            <text class="sk-pg-text">{{ pgPct(a) }}% 已抢</text>
          </view>
          <view class="sk-footer">
            <text class="sk-stock" :class="a.stock<20?'text-red':''">{{ a.stock<=0?'已售罄':'剩余'+a.stock+'件' }}</text>
            <button class="sk-btn" :disabled="a.stock<=0" @click="doSeckill(a)">{{ a.stock<=0?'已抢光':'立即秒杀' }}</button>
          </view>
        </view>
      </view>
    </view>

    <!-- Skeleton -->
    <view v-else class="sk-list">
      <view v-for="i in 4" :key="i" class="skeleton" style="height:300rpx;margin:12rpx 24rpx;border-radius:16rpx" />
    </view>

    <view v-if="!loading && activities.length===0" class="empty-state"><text>暂无秒杀活动</text></view>

    <!-- Success modal -->
    <view v-if="showSuccess" class="sk-modal-mask" @click="showSuccess=false">
      <view class="sk-modal" @click.stop>
        <text class="modal-icon">✅</text>
        <text class="modal-title">秒杀成功！</text>
        <text class="modal-sub">订单已生成，请尽快支付</text>
        <button class="modal-btn" @click="goOrder">查看订单</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { listSeckillActivities, executeSeckill } from '../../api/seckill.js'

const activities = ref([]); const loading = ref(true); const countdown = ref(0)
const showSuccess = ref(false); const lastOrderId = ref(null)
let cdTimer = null
const initStock = {}

function pad2(n){ return String(n).padStart(2,'0') }
function pgPct(a){ const init=initStock[a.id]||100;return Math.max(0,Math.min(99,Math.round((1-a.stock/init)*100))) }

async function fetch(){
  loading.value=true
  try{ const r=await listSeckillActivities(); activities.value=r.data||[]
    for(const a of activities.value){ if(!(a.id in initStock)) initStock[a.id]=a.stock }
    if(activities.value.length>0){ const et=new Date(activities.value[0].endTime).getTime(); countdown.value=Math.max(0,Math.floor((et-Date.now())/1000)) }
  }finally{loading.value=false}
}

async function doSeckill(a){
  try{ const r=await executeSeckill(a.id); lastOrderId.value=r.data.orderId; showSuccess.value=true; fetch() }
  catch(e){ uni.showToast({title:e?.response?.data?.message||'秒杀失败',icon:'none'}); fetch() }
}

function goOrder(){ showSuccess.value=false; if(lastOrderId.value) uni.navigateTo({url:`/pages/order/detail?id=${lastOrderId.value}`}) }

onMounted(()=>{ fetch(); cdTimer=setInterval(()=>{if(countdown.value>0)countdown.value--},1000) })
onUnmounted(()=>{ if(cdTimer)clearInterval(cdTimer) })
</script>

<style scoped lang="scss">
.sk-page { background:#F8FAFC;min-height:100vh; }
.sk-hero { background:linear-gradient(135deg,#7C2D12,#C2410C,#EA580C); padding:48rpx 24rpx; text-align:center; }
.sk-hero-title { font-size:48rpx;font-weight:900;color:#fff;display:block; }
.sk-hero-sub { font-size:24rpx;color:rgba(255,255,255,.7);margin:8rpx 0 24rpx;display:block; }
.sk-countdown { display:flex;align-items:center;justify-content:center;gap:6rpx; }
.sk-cd-item { display:flex;flex-direction:column;align-items:center;gap:2rpx; }
.cd-num { background:rgba(0,0,0,.25);color:#fff;font-size:40rpx;font-weight:900;font-family:monospace;padding:8rpx 16rpx;border-radius:10rpx;min-width:52rpx;text-align:center; }
.cd-unit { font-size:20rpx;color:rgba(255,255,255,.65); }
.cd-colon { font-size:32rpx;font-weight:700;color:rgba(255,255,255,.8);padding-bottom:16rpx; }
.sk-list { padding:16rpx 24rpx; }
.sk-card { background:#fff;border-radius:16rpx;overflow:hidden;margin-bottom:16rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); display:flex; }
.sk-card.sk-soldout { opacity:.7; }
.sk-img { width:200rpx;height:200rpx; }
.sk-img-fb { width:200rpx;height:200rpx;background:#F1F5F9;display:flex;align-items:center;justify-content:center;font-size:56rpx; }
.sk-body { flex:1;padding:16rpx;display:flex;flex-direction:column; }
.sk-name { font-size:28rpx;font-weight:600;color:#0F172A;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
.sk-prices { display:flex;align-items:baseline;gap:8rpx;margin-top:8rpx; }
.sk-price { font-size:36rpx;font-weight:800;color:#EF4444; }
.sk-origin { font-size:22rpx;color:#94A3B8;text-decoration:line-through; }
.sk-progress { display:flex;align-items:center;gap:10rpx;margin-top:12rpx; }
.sk-pg-bar { flex:1;height:8rpx;background:#F1F5F9;border-radius:4rpx;overflow:hidden; }
.sk-pg-fill { height:100%;background:linear-gradient(90deg,#EF4444,#F97316);border-radius:4rpx;transition:width .5s; }
.fill-gray { background:#CBD5E1; }
.sk-pg-text { font-size:20rpx;color:#EF4444;font-weight:600; }
.sk-footer { display:flex;justify-content:space-between;align-items:center;margin-top:auto;padding-top:12rpx; }
.sk-stock { font-size:22rpx;color:#64748B; }
.text-red { color:#EF4444;font-weight:600; }
.sk-btn { background:linear-gradient(135deg,#EF4444,#DC2626);color:#fff;border:none;border-radius:20rpx;padding:12rpx 24rpx;font-size:24rpx;font-weight:600; }
.sk-btn[disabled] { background:#CBD5E1; }
.sk-modal-mask { position:fixed;inset:0;background:rgba(0,0,0,.5);display:flex;align-items:center;justify-content:center;z-index:999; }
.sk-modal { background:#fff;border-radius:24rpx;padding:48rpx;text-align:center;width:560rpx; }
.modal-icon { font-size:80rpx; }
.modal-title { font-size:36rpx;font-weight:700;color:#0F172A;display:block;margin:16rpx 0 8rpx; }
.modal-sub { font-size:26rpx;color:#94A3B8;display:block;margin-bottom:24rpx; }
.modal-btn { background:#2563EB;color:#fff;border:none;border-radius:20rpx;padding:16rpx 48rpx;font-size:28rpx; }
</style>
