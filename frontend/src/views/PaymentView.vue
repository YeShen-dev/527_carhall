<template>
  <div class="payment-page">
    <div class="payment-container">

      <!-- Page Header -->
      <div class="pay-header">
        <h2 class="pay-title">订单支付</h2>
        <p class="pay-subtitle">选择支付方式，完成安全支付</p>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        </div>
        <span>正在加载订单信息...</span>
      </div>

      <!-- Order Summary Card -->
      <div v-if="order && !showQr && !showAlipay" class="order-summary">
        <div class="summary-header">
          <span class="summary-icon">📋</span>
          <span>订单信息</span>
        </div>
        <div class="summary-body">
          <div class="summary-row">
            <span class="slabel">订单编号</span>
            <span class="svalue mono">{{ order.orderNo }}</span>
          </div>
          <div class="summary-row">
            <span class="slabel">商品数量</span>
            <span class="svalue">{{ order.items?.length || 0 }} 件</span>
          </div>
          <div class="summary-row total-row">
            <span class="slabel">支付金额</span>
            <span class="amount">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </div>

      <!-- Payment Method Selection -->
      <div class="payment-methods" v-if="!showQr && !showAlipay && !loading">
        <div class="methods-header">
          <h3>选择支付方式</h3>
          <span class="secure-badge">
            <el-icon><Lock /></el-icon> 安全保障
          </span>
        </div>

        <!-- WeChat -->
        <div class="method-card" :class="{ active: selectedMethod === 'WECHAT' }"
             @click="selectedMethod = 'WECHAT'">
          <div class="method-left">
            <div class="method-icon wechat-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.742 2.04 2.668 3.533 4.231 3.938a4.35 4.35 0 0 0 .723.115c.287.008.575-.02.859-.074l1.342.785a.277.277 0 0 0 .144.038c.119 0 .217-.09.24-.2l.273-1.046c.028-.11.092-.168.186-.227 2.015-1.174 3.284-2.988 3.284-5.023 0-3.548-3.258-6.312-7.222-6.312zm-2.034 2.964c.458 0 .83.378.83.843a.836.836 0 0 1-.83.842.836.836 0 0 1-.83-.842c0-.465.372-.843.83-.843zm4.518 0c.458 0 .83.378.83.843a.836.836 0 0 1-.83.842.836.836 0 0 1-.83-.842c0-.465.372-.843.83-.843z"/></svg>
            </div>
            <div class="method-info">
              <span class="method-name">微信支付</span>
              <span class="method-desc">打开微信扫一扫，完成扫码支付</span>
            </div>
          </div>
          <div class="method-right">
            <span class="recommend-tag" v-if="selectedMethod === ''">推荐</span>
            <el-icon v-if="selectedMethod === 'WECHAT'" class="check-icon" :size="24"><CircleCheckFilled /></el-icon>
          </div>
        </div>

        <!-- Alipay -->
        <div class="method-card" :class="{ active: selectedMethod === 'ALIPAY' }"
             @click="selectedMethod = 'ALIPAY'">
          <div class="method-left">
            <div class="method-icon alipay-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor"><path d="M18.577 13.868c-.162.315-.537.653-.88.79-.34.133-.47.053-.47.053s-6.83-2.083-7.308-2.237c-.473-.155-.743-.188-.86-.313-.115-.125-.245-.465-.033-.89.215-.424.65-.86.994-.994.347-.133.505-.053.505-.053s6.832 2.087 7.308 2.24c.473.156.74.19.857.317.118.127.248.47.035.898l-.148.19zM9.08 6.764c-.86.393-1.575.215-2.07-.22-.496-.438-.61-1.113-.33-1.678.277-.565.9-.854 1.762-.91.86-.057 1.96.203 3.077 1.058 1.118.858 1.935 2.12 2.273 3.182-.86-.392-3.85-1.827-4.713-1.432zM4.044 12.4c-.5 1.357-.205 2.643.39 3.218.598.578 1.37.53 1.9-.01s.658-1.37.13-2.728c-.53-1.358-1.52-3.072-2.556-4.175-.5 1.358.136 3.695.136 3.695zM19.992 8.09c-.735-.157-1.48.08-2.098.384-1.395.685-2.536 1.873-3.126 3.305 1.153.37 4.164 1.308 5.69 1.777.163.05.323.088.48.116-.148-1.808-.21-4.197-.946-5.58zM22.098 15.454c-.275-.624-1.48-1.22-3.254-1.75l.002.002c-.656-.197-1.387-.385-2.11-.562-.083-.02-.165-.04-.248-.058 1.427-2.398 3.61-3.858 5.61-3.518v-.002s1.696 5.215 0 5.89z"/></svg>
            </div>
            <div class="method-info">
              <span class="method-name">支付宝</span>
              <span class="method-desc">跳转至支付宝收银台，安全便捷</span>
            </div>
          </div>
          <div class="method-right">
            <el-icon v-if="selectedMethod === 'ALIPAY'" class="check-icon" :size="24"><CircleCheckFilled /></el-icon>
          </div>
        </div>

        <!-- Bank Card -->
        <div class="method-card" :class="{ active: selectedMethod === 'BANKCARD' }"
             @click="selectedMethod = 'BANKCARD'">
          <div class="method-left">
            <div class="method-icon bank-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="4" width="20" height="16" rx="2"/><line x1="2" y1="10" x2="22" y2="10"/><rect x="4" y="14" width="4" height="2" fill="currentColor" opacity="0.4"/></svg>
            </div>
            <div class="method-info">
              <span class="method-name">银行卡支付</span>
              <span class="method-desc">支持储蓄卡、信用卡快捷支付</span>
            </div>
          </div>
          <div class="method-right">
            <el-icon v-if="selectedMethod === 'BANKCARD'" class="check-icon" :size="24"><CircleCheckFilled /></el-icon>
          </div>
        </div>

        <!-- Bank Card Form -->
        <transition name="slide-fade">
          <div v-if="selectedMethod === 'BANKCARD'" class="card-form">
            <div class="card-form-header">银行卡信息</div>
            <div class="card-preview">
              <div class="card-chip">💳</div>
              <div class="card-number-display">{{ cardForm.cardNo || '•••• •••• •••• ••••' }}</div>
              <div class="card-bottom">
                <span>{{ cardForm.expiry || 'MM/YY' }}</span>
                <span>{{ cardForm.cvv ? '•••' : 'CVV' }}</span>
              </div>
            </div>
            <div class="form-item">
              <label>卡号</label>
              <el-input v-model="cardForm.cardNo" placeholder="请输入银行卡号" maxlength="19"
                @input="formatCardNo" size="large" />
            </div>
            <div class="form-row">
              <div class="form-item flex-1">
                <label>有效期</label>
                <el-input v-model="cardForm.expiry" placeholder="MM/YY" maxlength="5" size="large" />
              </div>
              <div class="form-item flex-1">
                <label>CVV安全码</label>
                <el-input v-model="cardForm.cvv" placeholder="安全码" maxlength="4" type="password" size="large" />
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- WeChat QR Section -->
      <div v-if="showQr" class="qr-section">
        <div class="qr-panel">
          <div class="qr-left">
            <div class="qr-canvas-wrap">
              <canvas ref="qrCanvas" width="220" height="220"></canvas>
              <div v-if="countdown <= 0" class="qr-expired-overlay">
                <el-icon :size="32"><WarningFilled /></el-icon>
                <span>二维码已过期</span>
                <el-button size="small" type="primary" @click="refreshQrCode">刷新二维码</el-button>
              </div>
            </div>
          </div>
          <div class="qr-right">
            <div class="qr-method-badge wechat-badge">微信支付</div>
            <div class="qr-amount-large">¥{{ payResponse?.amount || order?.totalAmount }}</div>
            <div class="qr-steps">
              <div class="qr-step">
                <span class="step-num">1</span>
                <span>打开微信</span>
              </div>
              <div class="qr-step">
                <span class="step-num">2</span>
                <span>点击扫一扫</span>
              </div>
              <div class="qr-step">
                <span class="step-num">3</span>
                <span>扫描二维码支付</span>
              </div>
            </div>
            <div class="qr-meta">
              <span class="meta-label">流水号</span>
              <span class="meta-value">{{ payResponse?.paymentNo }}</span>
            </div>
            <div class="qr-countdown-bar" v-if="countdown > 0">
              <div class="countdown-progress" :style="{ width: (countdown / (30*60) * 100) + '%' }"></div>
              <span class="countdown-text">
                <el-icon><Clock /></el-icon>
                {{ Math.floor(countdown / 60) }}:{{ String(countdown % 60).padStart(2, '0') }}
              </span>
            </div>
          </div>
        </div>

        <!-- Status indicators -->
        <div class="qr-status-bar" v-if="pollingStatus === 'polling'">
          <div class="status-dot-pulse"></div>
          <span>等待支付确认中... 完成支付后自动跳转</span>
        </div>
        <div class="qr-status-bar success-bar" v-if="pollingStatus === 'paid'">
          <el-icon :size="22"><CircleCheckFilled /></el-icon>
          <span>支付成功！正在跳转订单详情...</span>
        </div>

        <div class="qr-bottom-actions" v-if="pollingStatus !== 'paid'">
          <el-button size="large" @click="cancelPayment">取消支付</el-button>
          <el-button size="large" type="primary" @click="switchMethod">更换支付方式</el-button>
        </div>
      </div>

      <!-- Alipay Redirect Section -->
      <div v-if="showAlipay" class="alipay-section">
        <div class="alipay-redirect-card">
          <div class="alipay-brand">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="#1677ff"><path d="M18.577 13.868c-.162.315-.537.653-.88.79-.34.133-.47.053-.47.053s-6.83-2.083-7.308-2.237c-.473-.155-.743-.188-.86-.313-.115-.125-.245-.465-.033-.89.215-.424.65-.86.994-.994.347-.133.505-.053.505-.053s6.832 2.087 7.308 2.24c.473.156.74.19.857.317.118.127.248.47.035.898l-.148.19zM9.08 6.764c-.86.393-1.575.215-2.07-.22-.496-.438-.61-1.113-.33-1.678.277-.565.9-.854 1.762-.91.86-.057 1.96.203 3.077 1.058 1.118.858 1.935 2.12 2.273 3.182-.86-.392-3.85-1.827-4.713-1.432zM4.044 12.4c-.5 1.357-.205 2.643.39 3.218.598.578 1.37.53 1.9-.01s.658-1.37.13-2.728c-.53-1.358-1.52-3.072-2.556-4.175-.5 1.358.136 3.695.136 3.695zM19.992 8.09c-.735-.157-1.48.08-2.098.384-1.395.685-2.536 1.873-3.126 3.305 1.153.37 4.164 1.308 5.69 1.777.163.05.323.088.48.116-.148-1.808-.21-4.197-.946-5.58zM22.098 15.454c-.275-.624-1.48-1.22-3.254-1.75l.002.002c-.656-.197-1.387-.385-2.11-.562-.083-.02-.165-.04-.248-.058 1.427-2.398 3.61-3.858 5.61-3.518v-.002s1.696 5.215 0 5.89z"/></svg>
          </div>
          <div class="alipay-loading-dots">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
          <p class="alipay-text">正在前往支付宝收银台</p>
          <p class="alipay-sub">如未自动跳转，请点击下方按钮</p>
          <el-button type="primary" size="large" round @click="submitAlipayForm">
            前往支付宝支付 ¥{{ order?.totalAmount }}
          </el-button>
          <el-button text type="info" size="small" @click="switchMethod" style="margin-top:12px">
            更换支付方式
          </el-button>
        </div>
        <div v-html="alipayFormHtml" style="display:none"></div>
      </div>

      <!-- Action Buttons (method selection) -->
      <div class="pay-actions" v-if="!showQr && !showAlipay && !loading">
        <el-button size="large" round @click="$router.back()">取消</el-button>
        <el-button type="danger" size="large" round
          :disabled="!selectedMethod" :loading="creating" class="pay-btn-primary"
          @click="handleCreatePayment">
          <template v-if="!selectedMethod">请选择支付方式</template>
          <template v-else-if="selectedMethod === 'ALIPAY'">前往支付宝支付</template>
          <template v-else-if="selectedMethod === 'WECHAT'">微信扫码支付</template>
          <template v-else>确认支付 ¥{{ order?.totalAmount }}</template>
        </el-button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '../api/order.js'
import { createPayment, queryPaymentStatus, closePayment } from '../api/payment.js'
import QRCode from 'qrcode'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const creating = ref(false)
const selectedMethod = ref('')
const payResponse = ref(null)

const showQr = ref(false)
const qrCanvas = ref(null)
const pollingStatus = ref('')
const countdown = ref(0)
let pollTimer = null
let countdownTimer = null

const showAlipay = ref(false)
const alipayFormHtml = ref('')
const alipayFormContainer = ref(null)

const cardForm = ref({ cardNo: '', expiry: '', cvv: '' })

function formatCardNo() {
  let val = cardForm.value.cardNo.replace(/\s/g, '')
  val = val.replace(/\D/g, '')
  val = val.replace(/(.{4})/g, '$1 ').trim()
  cardForm.value.cardNo = val
}

async function handleCreatePayment() {
  if (!order.value || !selectedMethod.value) return

  if (selectedMethod.value === 'BANKCARD') {
    if (!cardForm.value.cardNo.trim()) { ElMessage.warning('请输入银行卡号'); return }
    if (cardForm.value.cardNo.replace(/\s/g, '').length < 16) { ElMessage.warning('请输入有效的银行卡号'); return }
    if (!cardForm.value.expiry.trim()) { ElMessage.warning('请输入有效期'); return }
    if (!cardForm.value.cvv.trim()) { ElMessage.warning('请输入CVV安全码'); return }
  }

  creating.value = true
  try {
    const res = await createPayment(order.value.id, selectedMethod.value)
    payResponse.value = res.data

    if (selectedMethod.value === 'WECHAT') {
      showQr.value = true
      countdown.value = 30 * 60
      startCountdown()
      await nextTick()
      if (qrCanvas.value) {
        await QRCode.toCanvas(qrCanvas.value, res.data.qrCodeUrl || 'weixin://wxpay/bizpayurl', {
          width: 220, margin: 1, color: { dark: '#000000', light: '#ffffff' }
        })
      }
      pollingStatus.value = 'polling'
      startPolling()
    } else if (selectedMethod.value === 'ALIPAY') {
      showAlipay.value = true
      alipayFormHtml.value = res.data.qrCodeUrl
      await nextTick()
      setTimeout(() => submitAlipayForm(), 600)
    } else {
      ElMessage.success('支付成功！')
      setTimeout(() => router.replace(`/orders/${order.value.id}`), 800)
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '创建支付失败，请重试')
  } finally {
    creating.value = false
  }
}

function startCountdown() {
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) countdown.value--
    else clearInterval(countdownTimer)
  }, 1000)
}

function startPolling() {
  let attempts = 0
  pollTimer = setInterval(async () => {
    try {
      attempts++
      const res = await queryPaymentStatus(order.value.id)
      if (res.data.status === 'PAID') {
        stopTimers()
        pollingStatus.value = 'paid'
        ElMessage.success('支付成功！')
        setTimeout(() => router.replace(`/orders/${order.value.id}`), 1800)
      } else if (res.data.status === 'CANCELED' || res.data.status === 'CLOSED') {
        stopTimers()
        showQr.value = false
        ElMessage.warning('支付已取消')
      } else if (attempts > 120) {
        stopTimers()
        ElMessage.warning('支付确认超时，请查看订单详情确认支付状态')
      }
    } catch { /* continue polling */ }
  }, 2000)
}

function stopTimers() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
}

async function cancelPayment() {
  try {
    await closePayment(order.value.id)
    stopTimers()
    showQr.value = false
    showAlipay.value = false
    ElMessage.info('支付已取消')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '取消支付失败')
  }
}

function switchMethod() {
  stopTimers()
  showQr.value = false
  showAlipay.value = false
  payResponse.value = null
  pollingStatus.value = ''
  selectedMethod.value = ''
}

function refreshQrCode() {
  selectedMethod.value = 'WECHAT'
  handleCreatePayment()
}

function submitAlipayForm() {
  if (alipayFormContainer.value) {
    const formEl = alipayFormContainer.value.querySelector('form')
    if (formEl) {
      formEl.submit()
      return
    }
  }
  // Mock fallback
  if (payResponse.value?.qrCodeUrl) {
    const match = payResponse.value.qrCodeUrl.match(/action='([^']+)'/)
    if (match) window.location.href = match[1]
    else {
      setTimeout(() => {
        router.push(`/payment/return?paymentNo=${payResponse.value?.paymentNo}&orderId=${order.value?.id}`)
      }, 2000)
    }
  }
}

onMounted(async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
    const s = res.data.status
    if (s !== 'PENDING' && s !== 'PROCESSING' && s !== 'FAILED') {
      ElMessage.warning('该订单无需支付')
      if (s === 'PAID') router.replace(`/orders/${res.data.id}`)
    }
  } catch {
    ElMessage.error('订单不存在')
    router.replace('/orders')
  } finally {
    loading.value = false
  }
})

onUnmounted(() => { stopTimers() })
</script>

<style scoped>
.payment-page { min-height: 80vh; display: flex; justify-content: center; padding: 32px 16px 60px; }
.payment-container { width: 100%; max-width: 640px; }

/* Header */
.pay-header { text-align: center; margin-bottom: 28px; }
.pay-title { font-size: 26px; font-weight: 800; color: #f0f0f5; margin-bottom: 6px; letter-spacing: -0.5px; }
.pay-subtitle { font-size: 14px; color: #707080; }

/* Loading */
.loading-state { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 80px 0; color: #9898a8; font-size: 15px; }
.loading-spinner { width: 80px; height: 80px; border-radius: 50%; background: rgba(255,107,53,0.06); display: flex; align-items: center; justify-content: center; }

/* Order Summary */
.order-summary { background: linear-gradient(135deg, #1c1c2c 0%, #22223a 100%); border: 1px solid #2a2a3e; border-radius: 16px; overflow: hidden; margin-bottom: 24px; }
.summary-header { display: flex; align-items: center; gap: 8px; padding: 14px 20px; background: rgba(255,107,53,0.06); border-bottom: 1px solid #2a2a3e; font-size: 14px; font-weight: 600; color: #c0c0d0; }
.summary-icon { font-size: 16px; }
.summary-body { padding: 16px 20px; }
.summary-row { display: flex; justify-content: space-between; align-items: center; padding: 8px 0; }
.summary-row:not(:last-child) { border-bottom: 1px solid rgba(255,255,255,0.03); }
.slabel { font-size: 14px; color: #808098; }
.svalue { font-size: 14px; color: #c0c0d0; }
.svalue.mono { font-family: 'Courier New', monospace; font-size: 12px; }
.total-row { padding-top: 12px; }
.amount { font-size: 26px; font-weight: 800; color: #2563EB; letter-spacing: -0.5px; }

/* Methods */
.payment-methods { margin-bottom: 8px; }
.methods-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.methods-header h3 { font-size: 16px; font-weight: 600; color: #c0c0d0; }
.secure-badge { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #07c160; background: rgba(7,193,96,0.08); padding: 4px 10px; border-radius: 20px; }

.method-card {
  display: flex; align-items: center; justify-content: space-between;
  background: #1c1c2c; border: 1.5px solid #2a2a3e; border-radius: 14px;
  padding: 16px 20px; margin-bottom: 10px; cursor: pointer;
  transition: all 0.2s ease;
}
.method-card:hover { border-color: #3a3a5e; background: #222238; transform: translateX(2px); }
.method-card.active { border-color: #ff6b35; background: rgba(255,107,53,0.05); box-shadow: 0 0 0 3px rgba(255,107,53,0.08); }
.method-left { display: flex; align-items: center; gap: 16px; }
.method-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.wechat-icon { background: rgba(7,193,96,0.15); color: #07c160; }
.alipay-icon { background: rgba(22,119,255,0.15); color: #1677ff; }
.bank-icon { background: rgba(245,158,11,0.15); color: #f59e0b; }
.method-info { display: flex; flex-direction: column; gap: 2px; }
.method-name { font-size: 15px; font-weight: 600; color: #f0f0f5; }
.method-desc { font-size: 12px; color: #707080; }
.method-right { display: flex; align-items: center; }
.recommend-tag { font-size: 11px; background: rgba(255,107,53,0.12); color: #ff6b35; padding: 2px 10px; border-radius: 10px; font-weight: 600; }
.check-icon { color: #ff6b35; }

/* Bank card form */
.slide-fade-enter-active { transition: all 0.3s ease; }
.slide-fade-enter-from { opacity: 0; transform: translateY(-10px); }
.card-form { margin-top: 4px; margin-bottom: 10px; background: linear-gradient(135deg, #1c1c2c, #22223a); border: 1px solid #2a2a3e; border-radius: 14px; padding: 20px; }
.card-form-header { font-size: 14px; font-weight: 600; color: #a0a0b0; margin-bottom: 14px; }
.card-preview {
  background: linear-gradient(135deg, #1a1a2e 0%, #2d2d50 100%);
  border: 1px solid #3a3a5e; border-radius: 12px; padding: 18px 20px; margin-bottom: 16px;
  display: flex; flex-direction: column; gap: 12px; min-height: 90px;
}
.card-chip { font-size: 28px; }
.card-number-display { font-size: 18px; font-family: 'Courier New', monospace; color: #e0e0f0; letter-spacing: 2px; }
.card-bottom { display: flex; justify-content: space-between; font-size: 13px; color: #8080a0; }
.form-item { margin-bottom: 14px; }
.form-item label { display: block; font-size: 13px; color: #808098; margin-bottom: 6px; font-weight: 500; }
.form-row { display: flex; gap: 14px; }
.flex-1 { flex: 1; }

/* QR Section */
.qr-section { display: flex; flex-direction: column; gap: 16px; }
.qr-panel { background: #1c1c2c; border: 1px solid #2a2a3e; border-radius: 16px; padding: 28px; display: flex; gap: 28px; }
.qr-left { position: relative; flex-shrink: 0; }
.qr-canvas-wrap { position: relative; width: 220px; height: 220px; border-radius: 12px; overflow: hidden; border: 3px solid #07c160; }
.qr-canvas-wrap canvas { display: block; }
.qr-expired-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.75);
  display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px;
  color: #ef4444; font-size: 14px; font-weight: 600;
}
.qr-right { flex: 1; display: flex; flex-direction: column; gap: 12px; }
.qr-method-badge { display: inline-block; padding: 4px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; width: fit-content; }
.wechat-badge { background: rgba(7,193,96,0.12); color: #07c160; }
.qr-amount-large { font-size: 32px; font-weight: 800; color: #ff6b35; }
.qr-steps { display: flex; flex-direction: column; gap: 8px; background: rgba(255,255,255,0.02); border-radius: 10px; padding: 12px; }
.qr-step { display: flex; align-items: center; gap: 10px; font-size: 13px; color: #9090a8; }
.step-num {
  width: 22px; height: 22px; border-radius: 50%; background: rgba(7,193,96,0.12);
  color: #07c160; font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.qr-meta { display: flex; gap: 8px; font-size: 12px; color: #606080; align-items: center; padding-top: 4px; }
.meta-label { color: #505060; }
.meta-value { font-family: 'Courier New', monospace; color: #8080a0; font-size: 11px; }
.qr-countdown-bar {
  position: relative; height: 30px; background: rgba(255,255,255,0.03); border-radius: 8px;
  overflow: hidden; display: flex; align-items: center; justify-content: center;
}
.countdown-progress {
  position: absolute; left: 0; top: 0; height: 100%;
  background: linear-gradient(90deg, rgba(7,193,96,0.15), rgba(7,193,96,0.05));
  transition: width 1s linear;
}
.countdown-text { position: relative; z-index: 1; font-size: 13px; color: #9090a8; display: flex; align-items: center; gap: 6px; }

.qr-status-bar { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 14px; border-radius: 12px; font-size: 14px; font-weight: 500; }
.qr-status-bar:not(.success-bar) { background: rgba(255,107,53,0.06); border: 1px solid rgba(255,107,53,0.15); color: #ff6b35; }
.success-bar { background: rgba(7,193,96,0.06); border: 1px solid rgba(7,193,96,0.15); color: #07c160; }
.status-dot-pulse {
  width: 10px; height: 10px; border-radius: 50%; background: #ff6b35;
  animation: pulse 1.2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(0.8); }
}

.qr-bottom-actions { display: flex; gap: 12px; justify-content: center; padding-top: 4px; }

/* Alipay */
.alipay-section { display: flex; flex-direction: column; gap: 16px; }
.alipay-redirect-card {
  background: #1c1c2c; border: 1px solid #2a2a3e; border-radius: 16px;
  padding: 48px 32px; text-align: center; display: flex; flex-direction: column; align-items: center; gap: 14px;
}
.alipay-brand { margin-bottom: 4px; }
.alipay-loading-dots { display: flex; gap: 6px; }
.alipay-loading-dots .dot {
  width: 8px; height: 8px; border-radius: 50%; background: #1677ff;
  animation: dotBounce 0.6s ease-in-out infinite alternate;
}
.alipay-loading-dots .dot:nth-child(2) { animation-delay: 0.2s; }
.alipay-loading-dots .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotBounce { from { transform: translateY(0); opacity: 0.6; } to { transform: translateY(-8px); opacity: 1; } }
.alipay-text { font-size: 17px; color: #e0e0f0; font-weight: 600; }
.alipay-sub { font-size: 13px; color: #606080; }

/* Pay actions */
.pay-actions { display: flex; gap: 14px; margin-top: 8px; }
.pay-actions .el-button { flex: 1; }
.pay-btn-primary { background: linear-gradient(135deg, #2563EB, #3B82F6); border: none; font-weight: 600; }
.pay-btn-primary:disabled { background: #E2E8F0; color: #94A3B8; }

@media (max-width: 520px) {
  .qr-panel { flex-direction: column; align-items: center; padding: 20px; }
  .qr-left { margin-bottom: 8px; }
  .qr-right { align-items: center; text-align: center; }
  .pay-actions { flex-direction: column; }
}
</style>
