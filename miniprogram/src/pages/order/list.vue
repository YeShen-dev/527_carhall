<template>
  <view class="order-page">
    <!-- Tabs -->
    <view class="order-tabs">
      <view v-for="(t,i) in tabs" :key="i" class="ot-item" :class="{active:curTab===i}" @click="switchTab(i)">{{ t }}</view>
    </view>

    <scroll-view v-if="!loading" scroll-y class="order-list">
      <view v-for="o in orders" :key="o.id" class="order-card" @click="goDetail(o.id)">
        <view class="oc-hd">
          <text class="oc-no">{{ o.orderNo }}</text>
          <text class="oc-status" :style="{color:statusColor(o.status)}">{{ statusLabel(o.status) }}</text>
        </view>
        <view v-for="item in o.items" :key="item.productId" class="oc-item">
          <image v-if="item.productImage" :src="item.productImage" mode="aspectFill" class="oci-img" />
          <view v-else class="oci-img-fb"><text>📦</text></view>
          <view class="oci-info">
            <text class="oci-name">{{ item.productName }}</text>
            <text class="oci-meta">x{{ item.quantity }} ¥{{ item.price }}</text>
          </view>
        </view>
        <view class="oc-ft">
          <text class="oc-total">合计 <text style="font-size:32rpx;font-weight:800">¥{{ o.totalAmount }}</text></text>
          <view class="oc-actions">
            <button v-if="o.status==='PENDING'||o.status==='FAILED'" class="oc-btn pay" @click.stop="goPay(o.id)">去支付</button>
            <button v-if="o.status==='PAID'||o.status==='SHIPPED'" class="oc-btn" @click.stop="confirmReceipt(o.id)">确认收货</button>
            <button v-if="o.status==='COMPLETED'" class="oc-btn comment-btn" @click.stop="goComment(o)">评价</button>
            <button v-if="o.status==='PAID'||o.status==='SHIPPED'" class="oc-btn refund" @click.stop="applyRefund(o.id)">申请退款</button>
          </view>
        </view>
      </view>
    </scroll-view>
    <view v-else class="skeleton-list">
      <view v-for="i in 3" :key="i" class="skeleton" style="height:200rpx;margin:16rpx 24rpx;border-radius:16rpx" />
    </view>
    <view v-if="!loading && orders.length===0" class="empty-state"><text class="empty-icon">📋</text><text class="empty-text">暂无订单</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { listOrders, cancelOrder, confirmReceipt as apiConfirm } from '../../api/order.js'
import { createRefund } from '../../api/payment.js'

const tabs = ['全部','待付款','已支付','已发货','已完成']
const curTab = ref(0)
const orders = ref([])
const loading = ref(true)

onLoad(opts=>{ if(opts.tab!=null) curTab.value = parseInt(opts.tab||0) })

function statusLabel(s){ const m={PENDING:'待支付',PROCESSING:'支付中',PAID:'已支付',FAILED:'支付失败',CANCELLED:'已取消',SHIPPED:'已发货',COMPLETED:'已完成',REFUNDING:'退款中',REFUNDED:'已退款'}; return m[s]||s }
function statusColor(s){ const m={PENDING:'#F59E0B',PAID:'#10B981',FAILED:'#EF4444',SHIPPED:'#2563EB',COMPLETED:'#10B981',REFUNDING:'#F59E0B'}; return m[s]||'#475569' }

async function loadOrders() {
  loading.value = true
  try {
    const statusMap = ['','PENDING','PAID','SHIPPED','COMPLETED']
    const res = await listOrders({ page:0, size:50, status:statusMap[curTab.value]||undefined })
    orders.value = res.data?.content || []
  } finally { loading.value = false }
}

function switchTab(i) { curTab.value = i; loadOrders() }
function goDetail(id) { uni.navigateTo({ url:`/pages/order/detail?id=${id}` }) }
function goPay(id) { uni.navigateTo({ url:`/pages/payment/pay?orderId=${id}` }) }

async function confirmReceipt(id) {
  uni.showModal({ title:'确认收货', content:'确认已收到商品？', success:async r=>{ if(r.confirm){ await apiConfirm(id); loadOrders() } } })
}
async function applyRefund(id) {
  uni.showModal({ title:'申请退款', content:'确认申请退款？', success:async r=>{ if(r.confirm){ try{ await createRefund(id,'用户申请退款'); uni.showToast({title:'退款申请已提交',icon:'success'}); loadOrders() }catch{} } } })
}

function goComment(o) {
  if (!o.items || o.items.length === 0) return
  const item = o.items[0]
  uni.navigateTo({ url: `/pages/comment/add?productId=${item.productId}&orderId=${o.id}&productName=${encodeURIComponent(item.productName)}` })
}

onMounted(() => loadOrders())
</script>

<style scoped lang="scss">
.order-page { background:#F8FAFC;min-height:100vh; }
.order-tabs { display:flex;background:#fff;border-bottom:1rpx solid #F1F5F9; }
.ot-item { flex:1;text-align:center;padding:24rpx 0;font-size:28rpx;color:#64748B;position:relative; }
.ot-item.active { color:#2563EB;font-weight:600; }
.ot-item.active::after { content:'';position:absolute;bottom:0;left:50%;transform:translateX(-50%);width:40rpx;height:4rpx;background:#2563EB;border-radius:2rpx; }
.order-list { padding:16rpx 24rpx; }
.order-card { background:#fff;border-radius:16rpx;padding:20rpx;margin-bottom:16rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.oc-hd { display:flex;justify-content:space-between;align-items:center;margin-bottom:14rpx;padding-bottom:14rpx;border-bottom:1rpx solid #F8FAFC; }
.oc-no { font-size:24rpx;color:#94A3B8;font-family:monospace; }
.oc-status { font-size:24rpx;font-weight:600; }
.oc-item { display:flex;gap:14rpx;margin-bottom:12rpx; }
.oci-img { width:80rpx;height:80rpx;border-radius:10rpx; }
.oci-img-fb { width:80rpx;height:80rpx;border-radius:10rpx;background:#F1F5F9;display:flex;align-items:center;justify-content:center;font-size:36rpx; }
.oci-info { flex:1;display:flex;flex-direction:column;gap:4rpx; }
.oci-name { font-size:26rpx;font-weight:600;color:#0F172A; }
.oci-meta { font-size:22rpx;color:#94A3B8; }
.oc-ft { display:flex;justify-content:space-between;align-items:center;padding-top:14rpx;border-top:1rpx solid #F8FAFC; }
.oc-total { font-size:26rpx;color:#64748B; }
.oc-actions { display:flex;gap:12rpx; }
.oc-btn { padding:10rpx 24rpx;border-radius:20rpx;font-size:24rpx;border:1rpx solid #E2E8F0;background:#fff;color:#475569; }
.oc-btn.pay { background:#2563EB;color:#fff;border-color:#2563EB; }
.oc-btn.refund { color:#EF4444;border-color:#FEE2E2; }
.oc-btn.comment-btn { background:#10B981;color:#fff;border-color:#10B981; }
.empty-state { display:flex;flex-direction:column;align-items:center;padding:100rpx 0;color:#94A3B8; }
.empty-icon { font-size:64rpx; } .empty-text { font-size:28rpx;margin-top:12rpx; }
</style>
