<template>
  <div class="return-page">
    <div class="return-card">
      <el-icon :size="56" color="#07c160"><CircleCheckFilled /></el-icon>
      <h2>支付完成</h2>
      <p class="info">支付宝支付处理中，系统正在确认支付结果...</p>
      <el-button type="primary" size="large" @click="goToOrder">查看订单</el-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { queryPaymentStatus } from '../api/payment.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const orderId = ref(null)

async function goToOrder() {
  if (orderId.value) {
    router.replace(`/orders/${orderId.value}`)
  } else {
    router.replace('/orders')
  }
}

onMounted(async () => {
  // Try to extract paymentNo and poll for status
  const paymentNo = route.query.paymentNo

  // Poll for payment status
  const orderIdFromRoute = route.query.orderId
  if (orderIdFromRoute) {
    orderId.value = orderIdFromRoute
    let attempts = 0
    const poll = setInterval(async () => {
      try {
        attempts++
        const res = await queryPaymentStatus(orderIdFromRoute)
        if (res.data.status === 'PAID') {
          clearInterval(poll)
          ElMessage.success('支付成功！')
          router.replace(`/orders/${orderIdFromRoute}`)
        } else if (attempts > 30) {
          clearInterval(poll)
        }
      } catch { /* continue */ }
    }, 2000)
  }
})
</script>

<style scoped>
.return-page { min-height: 80vh; display: flex; align-items: center; justify-content: center; }
.return-card {
  background: #1c1c2c; border: 1px solid #2a2a3e; border-radius: 16px;
  padding: 48px; text-align: center; display: flex; flex-direction: column; align-items: center; gap: 16px;
  max-width: 420px; width: 100%;
}
.return-card h2 { font-size: 20px; font-weight: 700; color: #f0f0f5; }
.return-card .info { font-size: 14px; color: #9898a8; margin-bottom: 8px; }
</style>
