<template>
  <div class="od-page">
    <el-button text type="primary" @click="$router.push('/orders')" class="back-btn">
      <el-icon><ArrowLeft /></el-icon> 返回订单列表
    </el-button>

    <div v-if="order" class="od-card">
      <div class="od-header">
        <div><h2>订单详情</h2><span class="od-no">{{ order.orderNo }}</span></div>
        <el-tag :type="statusType(order.status)" size="large">{{ statusLabel(order.status) }}</el-tag>
      </div>

      <el-divider />

      <div class="od-items">
        <div v-for="item in order.items" :key="item.productId" class="od-item">
          <el-image v-if="item.productImage" :src="item.productImage" fit="cover" class="od-img" />
          <div class="od-info"><span class="od-name">{{ item.productName }}</span><span class="od-meta">单价 ¥{{ item.price }} × {{ item.quantity }}</span></div>
          <span class="od-subtotal">¥{{ item.subtotal }}</span>
        </div>
      </div>

      <el-divider />

      <div class="od-footer">
        <div class="od-row"><span>下单时间</span><span>{{ formatDate(order.createdAt) }}</span></div>
        <div class="od-row total-row"><span>合计</span><span class="total-price">¥{{ order.totalAmount }}</span></div>
      </div>

      <div v-if="order.status==='PENDING'||order.status==='FAILED'" class="od-pay">
        <el-button type="danger" size="large" @click="$router.push(`/payment/${order.id}`)">立即支付 ¥{{ order.totalAmount }}</el-button>
      </div>
      <div v-if="order.status==='PROCESSING'" class="od-status-note"><el-icon><Clock /></el-icon> 支付处理中，请稍候...</div>
      <div v-if="order.status==='REFUNDING'" class="od-status-note"><el-icon><Clock /></el-icon> 退款处理中，请稍候...</div>
      <div v-if="order.status==='PAID'" class="od-status-note success"><el-icon><CircleCheck /></el-icon> 支付成功</div>
    </div>
    <el-empty v-else description="订单不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetail } from '../api/order.js'
const route=useRoute(); const order=ref(null)
function statusLabel(s){const m={PENDING:'待支付',PROCESSING:'支付中',PAID:'已支付',FAILED:'支付失败',CANCELLED:'已取消',SHIPPED:'已发货',COMPLETED:'已完成',REFUNDING:'退款中',REFUNDED:'已退款',REFUND_FAIL:'退款失败'};return m[s]||s}
function statusType(s){const m={PENDING:'warning',PROCESSING:'',PAID:'success',FAILED:'danger',CANCELLED:'info',SHIPPED:'',COMPLETED:'success',REFUNDING:'warning',REFUNDED:'info',REFUND_FAIL:'danger'};return m[s]||'info'}
function formatDate(d){if(!d)return'';return new Date(d).toLocaleString('zh-CN')}
onMounted(async()=>{const r=await getOrderDetail(route.params.id);order.value=r.data})
</script>

<style scoped>
.od-page{max-width:800px;margin:0 auto}
.back-btn{margin-bottom:16px}
.od-card{background:#fff;border-radius:16px;padding:28px;border:1px solid #F1F5F9;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.od-header{display:flex;justify-content:space-between;align-items:flex-start}
.od-header h2{font-size:20px;font-weight:700;color:#0F172A;margin-bottom:4px}
.od-no{font-size:13px;color:#94A3B8;font-family:monospace}
.od-items{display:flex;flex-direction:column;gap:14px}
.od-item{display:flex;align-items:center;gap:14px}
.od-img{width:64px;height:64px;border-radius:10px;flex-shrink:0}
.od-info{flex:1;display:flex;flex-direction:column;gap:2px}
.od-name{font-weight:600;color:#0F172A}
.od-meta{font-size:12px;color:#94A3B8}
.od-subtotal{font-size:18px;font-weight:700;color:#EF4444}
.od-footer{display:flex;flex-direction:column;gap:8px}
.od-row{display:flex;justify-content:space-between;color:#64748B;font-size:14px}
.total-row{padding-top:8px;border-top:1px solid #F1F5F9;font-size:18px;font-weight:700;color:#0F172A}
.total-price{color:#EF4444}
.od-pay{margin-top:24px;padding-top:20px;border-top:1px solid #F1F5F9;display:flex;justify-content:flex-end}
.od-status-note{display:flex;align-items:center;gap:8px;margin-top:20px;padding:14px 18px;border-radius:12px;font-size:14px;font-weight:500;background:#F8FAFC;color:#64748B}
.od-status-note.success{background:#ECFDF5;color:#10B981}
</style>
