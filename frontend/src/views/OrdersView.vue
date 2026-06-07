<template>
  <div class="orders-page">
    <h2 class="page-title">我的订单</h2>

    <!-- Order Cards -->
    <div v-loading="loading" class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-card" @click="$router.push(`/orders/${order.id}`)">
        <div class="oc-header">
          <span class="oc-no">订单号: {{ order.orderNo }}</span>
          <el-tag :type="statusType(order.status)" size="small" effect="plain">{{ statusLabel(order.status) }}</el-tag>
        </div>
        <div class="oc-items">
          <div v-for="item in order.items" :key="item.productId" class="oc-item">
            <el-image v-if="item.productImage" :src="item.productImage" fit="cover" class="oc-img" />
            <div class="oc-info">
              <span class="oc-name">{{ item.productName }}</span>
              <span class="oc-meta">x{{ item.quantity }} · ¥{{ item.price }}</span>
            </div>
          </div>
        </div>
        <div class="oc-footer">
          <span class="oc-date">{{ formatDate(order.createdAt) }}</span>
          <div class="oc-actions">
            <span class="oc-total">合计 <strong>¥{{ order.totalAmount }}</strong></span>
            <el-button v-if="order.status==='PENDING'||order.status==='FAILED'" type="danger" size="small" @click.stop="$router.push(`/payment/${order.id}`)">立即支付</el-button>
            <el-button size="small" text type="primary" @click.stop="$router.push(`/orders/${order.id}`)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && orders.length===0" description="暂无订单" />
    <div v-if="total>0" class="pagination-wrapper">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev,pager,next" background @current-change="doLoad" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listOrders } from '../api/order.js'
const orders=ref([]); const loading=ref(false); const page=ref(1); const size=ref(10); const total=ref(0)

function statusLabel(s){ const m={PENDING:'待支付',PROCESSING:'支付中',PAID:'已支付',FAILED:'支付失败',CANCELLED:'已取消',SHIPPED:'已发货',COMPLETED:'已完成',REFUNDING:'退款中',REFUNDED:'已退款',REFUND_FAIL:'退款失败'}; return m[s]||s }
function statusType(s){ const m={PENDING:'warning',PROCESSING:'',PAID:'success',FAILED:'danger',CANCELLED:'info',SHIPPED:'',COMPLETED:'success',REFUNDING:'warning',REFUNDED:'info',REFUND_FAIL:'danger'}; return m[s]||'info' }
function formatDate(d){ if(!d)return''; return new Date(d).toLocaleString('zh-CN') }

async function doLoad(){ loading.value=true; try{ const r=await listOrders({page:page.value-1,size:size.value}); orders.value=r.data.content||[]; total.value=r.data.totalElements||0 }finally{loading.value=false} }
onMounted(()=>doLoad())
</script>

<style scoped>
.orders-page{max-width:800px;margin:0 auto}
.page-title{font-size:22px;font-weight:700;color:#0F172A;margin-bottom:20px}
.order-list{display:flex;flex-direction:column;gap:14px}
.order-card{background:#fff;border-radius:16px;border:1px solid #F1F5F9;overflow:hidden;cursor:pointer;transition:all .2s;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.order-card:hover{box-shadow:0 8px 24px rgba(0,0,0,.08);transform:translateY(-2px)}
.oc-header{display:flex;justify-content:space-between;align-items:center;padding:14px 18px;background:#F8FAFC;border-bottom:1px solid #F1F5F9}
.oc-no{font-size:13px;color:#64748B;font-family:monospace}
.oc-items{padding:12px 18px;display:flex;flex-direction:column;gap:10px}
.oc-item{display:flex;align-items:center;gap:12px}
.oc-img{width:56px;height:56px;border-radius:10px;flex-shrink:0}
.oc-info{display:flex;flex-direction:column;gap:2px}
.oc-name{font-size:14px;font-weight:600;color:#0F172A}
.oc-meta{font-size:12px;color:#94A3B8}
.oc-footer{display:flex;justify-content:space-between;align-items:center;padding:12px 18px;border-top:1px solid #F1F5F9}
.oc-date{font-size:12px;color:#94A3B8}
.oc-actions{display:flex;align-items:center;gap:10px}
.oc-total{font-size:14px;color:#475569}
.oc-total strong{font-size:18px;font-weight:700;color:#0F172A}
.pagination-wrapper{display:flex;justify-content:center;padding:24px 0}
</style>
