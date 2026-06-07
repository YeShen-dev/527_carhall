<template>
  <div class="seckill-page">

    <!-- ===== Hero with Countdown ===== -->
    <section class="sk-hero">
      <div class="sk-hero-content">
        <span class="sk-hero-badge">⚡ 限时秒杀</span>
        <h1>超值配件 限时抢购</h1>
        <p>优质汽车配件低至5折，手慢无！</p>
        <div class="sk-hero-countdown" v-if="globalCountdown > 0">
          <div class="cd-block"><span class="cd-num">{{ pad2(Math.floor(globalCountdown/3600)) }}</span><span class="cd-unit">时</span></div>
          <span class="cd-colon">:</span>
          <div class="cd-block"><span class="cd-num">{{ pad2(Math.floor(globalCountdown/60)%60) }}</span><span class="cd-unit">分</span></div>
          <span class="cd-colon">:</span>
          <div class="cd-block"><span class="cd-num">{{ pad2(globalCountdown%60) }}</span><span class="cd-unit">秒</span></div>
        </div>
      </div>
    </section>

    <!-- ===== Skeleton Loading ===== -->
    <div v-if="loading" class="sk-grid">
      <div v-for="i in 4" :key="i" class="sk-skeleton">
        <div class="skeleton sk-img"></div>
        <div class="sk-body">
          <div class="skeleton" style="width:100%;height:18px"></div>
          <div class="skeleton" style="width:60%;height:28px;margin-top:8px"></div>
          <div class="skeleton" style="width:100%;height:6px;margin-top:8px"></div>
        </div>
      </div>
    </div>

    <!-- ===== Seckill Grid ===== -->
    <div v-else class="sk-grid">
      <div v-for="a in activities" :key="a.id" class="sk-card" :class="{
        'sk-ending': a.stock > 0 && a.stock < 20,
        'sk-soldout': a.stock <= 0
      }">
        <div class="sk-img-wrap">
          <el-image v-if="a.productImage" :src="a.productImage" fit="cover" style="width:100%;aspect-ratio:1" />
          <div v-else class="sk-img-fb"><el-icon :size="48"><PictureFilled /></el-icon></div>
          <span class="sk-tag" :class="a.stock <= 0 ? 'tag-sold' : 'tag-live'">
            {{ a.stock <= 0 ? '已抢光' : '秒杀中' }}
          </span>
        </div>

        <div class="sk-body">
          <h3 class="sk-name">{{ a.productName }}</h3>
          <div class="sk-prices">
            <span class="sk-price">¥{{ a.seckillPrice }}</span>
            <span class="sk-origin" v-if="a.productPrice">¥{{ a.productPrice }}</span>
          </div>

          <!-- Progress bar -->
          <div class="sk-progress">
            <div class="sk-progress-bar">
              <div class="sk-progress-fill" :class="a.stock <= 0 ? 'fill-gray' : ''"
                :style="{ width: progressPct(a) + '%' }"></div>
            </div>
            <span class="sk-progress-text">{{ progressPct(a) }}% 已抢</span>
          </div>

          <div class="sk-actions">
            <span class="sk-stock-text" :class="a.stock < 20 ? 'text-red' : ''">
              {{ a.stock <= 0 ? '已售罄' : '剩余 ' + a.stock + ' 件' }}
            </span>
            <el-button :type="a.stock <= 0 ? 'info' : 'danger'" :disabled="a.stock <= 0"
              size="large" round @click="handleSeckill(a)" class="sk-btn">
              {{ a.stock <= 0 ? '已抢光' : '立即秒杀' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && activities.length === 0" description="暂无秒杀活动，敬请期待" />

    <!-- ===== Success Modal ===== -->
    <el-dialog v-model="showSuccess" width="400px" :show-close="false" center class="success-dialog">
      <div class="success-content">
        <div class="success-check">
          <svg viewBox="0 0 52 52" width="64" height="64">
            <circle cx="26" cy="26" r="25" fill="none" stroke="#10B981" stroke-width="2" stroke-dasharray="157" stroke-dashoffset="157">
              <animate attributeName="stroke-dashoffset" from="157" to="0" dur="0.5s" fill="freeze"/>
            </circle>
            <polyline points="14,27 22,35 38,17" fill="none" stroke="#10B981" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" stroke-dasharray="36" stroke-dashoffset="36">
              <animate attributeName="stroke-dashoffset" from="36" to="0" dur="0.3s" begin="0.4s" fill="freeze"/>
            </polyline>
          </svg>
        </div>
        <h2>秒杀成功！</h2>
        <p class="success-sub">订单已生成，请尽快完成支付</p>
        <div class="success-actions">
          <el-button size="large" @click="showSuccess = false">继续抢购</el-button>
          <el-button type="primary" size="large" @click="goToOrder">查看订单</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listSeckillActivities, executeSeckill } from '../api/seckill.js'

const router = useRouter()
const activities = ref([])
const loading = ref(false)
const showSuccess = ref(false)
const lastOrderId = ref(null)
const globalCountdown = ref(0)
let cdTimer = null

function pad2(n) { return String(n).padStart(2,'0') }

const initialStock = ref({})
function progressPct(a) {
  const init = initialStock.value[a.id] || 100
  if (init <= 0) return 0
  return Math.max(0, Math.min(99, Math.round((1 - a.stock / init) * 100)))
}
function refreshInitialStock() {
  for (const a of activities.value) {
    if (!(a.id in initialStock.value)) initialStock.value[a.id] = a.stock
  }
}

async function fetchActivities() {
  loading.value = true
  try {
    const res = await listSeckillActivities()
    activities.value = res.data || []
    refreshInitialStock()
    if (activities.value.length > 0) {
      const endTime = new Date(activities.value[0].endTime).getTime()
      globalCountdown.value = Math.max(0, Math.floor((endTime - Date.now()) / 1000))
    }
  } finally { loading.value = false }
}

async function handleSeckill(activity) {
  try {
    const res = await executeSeckill(activity.id)
    lastOrderId.value = res.data.orderId
    showSuccess.value = true
    await fetchActivities()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '秒杀失败')
    await fetchActivities()
  }
}

function goToOrder() {
  showSuccess.value = false
  if (lastOrderId.value) router.push(`/orders/${lastOrderId.value}`)
}

onMounted(() => {
  fetchActivities()
  cdTimer = setInterval(() => { if (globalCountdown.value > 0) globalCountdown.value-- }, 1000)
})
onUnmounted(() => { if (cdTimer) clearInterval(cdTimer) })
</script>

<style scoped>
.seckill-page { display: flex; flex-direction: column; gap: 24px; }

/* ===== Hero ===== */
.sk-hero {
  background: linear-gradient(135deg, #7C2D12 0%, #9A3412 30%, #C2410C 60%, #EA580C 100%);
  border-radius: 24px; padding: 48px; text-align: center; position: relative; overflow: hidden;
}
.sk-hero::before {
  content: ''; position: absolute; top: -30%; right: -10%;
  width: 400px; height: 400px; border-radius: 50%;
  background: rgba(255,255,255,0.05);
}
.sk-hero-content { position: relative; z-index: 1; }
.sk-hero-badge { display: inline-block; background: rgba(255,255,255,0.18); color: #fff; font-size: 15px; font-weight: 700; padding: 6px 24px; border-radius: 24px; margin-bottom: 16px; backdrop-filter: blur(8px); }
.sk-hero h1 { font-size: 36px; font-weight: 900; color: #fff; margin-bottom: 8px; }
.sk-hero p { font-size: 15px; color: rgba(255,255,255,0.7); margin-bottom: 24px; }

/* Countdown */
.sk-hero-countdown { display: flex; align-items: center; justify-content: center; gap: 6px; }
.cd-block { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.cd-num { background: rgba(0,0,0,0.25); color: #fff; font-size: 36px; font-weight: 900; font-family: 'SF Mono','Courier New',monospace; padding: 6px 14px; border-radius: 10px; min-width: 56px; text-align: center; backdrop-filter: blur(4px); animation: countPulse 1s infinite; }
.cd-unit { font-size: 12px; color: rgba(255,255,255,0.65); }
.cd-colon { font-size: 30px; font-weight: 700; color: rgba(255,255,255,0.8); margin: 0 2px; align-self: center; padding-bottom: 16px; }
@keyframes countPulse { 0%,100%{transform:scale(1)} 50%{transform:scale(1.05)} }

/* Grid */
.sk-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
.sk-skeleton { background: #fff; border-radius: 16px; overflow: hidden; border: 1px solid #F1F5F9; }
.sk-img { width: 100%; aspect-ratio: 1; }
.sk-skeleton .sk-body { padding: 14px; }

/* Card */
.sk-card { background: #fff; border-radius: 16px; overflow: hidden; border: 1px solid #F1F5F9; transition: all 0.3s; box-shadow: 0 1px 3px rgba(0,0,0,0.04); position: relative; }
.sk-card:hover { transform: translateY(-6px); box-shadow: 0 16px 36px rgba(0,0,0,0.1); }
.sk-card.sk-ending { border-color: #FCD34D; box-shadow: 0 0 0 2px rgba(252,211,77,0.3); animation: skPulseGlow 2s infinite; }
.sk-card.sk-soldout { opacity: 0.7; }
@keyframes skPulseGlow { 0%,100%{box-shadow:0 0 0 2px rgba(252,211,77,0.2)} 50%{box-shadow:0 0 0 4px rgba(252,211,77,0.4)} }

.sk-img-wrap { position: relative; }
.sk-img-fb { width: 100%; aspect-ratio: 1; display: flex; align-items: center; justify-content: center; background: #F1F5F9; color: #CBD5E1; }
.sk-tag { position: absolute; top: 0; left: 0; font-size: 12px; font-weight: 700; padding: 5px 14px; border-radius: 0 0 12px 0; }
.tag-live { background: linear-gradient(135deg,#EF4444,#F97316); color: #fff; }
.tag-sold { background: rgba(0,0,0,0.7); color: #CBD5E1; }

.sk-body { padding: 16px; }
.sk-name { font-size: 15px; font-weight: 600; color: #0F172A; margin-bottom: 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sk-prices { display: flex; align-items: baseline; gap: 8px; margin-bottom: 12px; }
.sk-price { font-size: 28px; font-weight: 800; color: #EF4444; }
.sk-origin { font-size: 14px; color: #94A3B8; text-decoration: line-through; }

/* Progress */
.sk-progress { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.sk-progress-bar { flex: 1; height: 6px; background: #F1F5F9; border-radius: 3px; overflow: hidden; }
.sk-progress-fill { height: 100%; background: linear-gradient(90deg,#EF4444,#F97316); border-radius: 3px; transition: width 0.5s ease; }
.fill-gray { background: #CBD5E1; }
.sk-progress-text { font-size: 12px; color: #EF4444; font-weight: 600; white-space: nowrap; }

.sk-actions { display: flex; justify-content: space-between; align-items: center; }
.sk-stock-text { font-size: 13px; color: #64748B; }
.text-red { color: #EF4444; font-weight: 600; }
.sk-btn { font-weight: 600; min-width: 100px; }

/* Success dialog */
.success-dialog :deep(.el-dialog) { border-radius: 20px; }
.success-content { text-align: center; padding: 20px 0; }
.success-check { margin-bottom: 16px; }
.success-content h2 { font-size: 22px; font-weight: 700; color: #0F172A; margin-bottom: 6px; }
.success-sub { font-size: 14px; color: #64748B; margin-bottom: 24px; }
.success-actions { display: flex; gap: 12px; justify-content: center; }

@media (max-width: 1200px) { .sk-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .sk-grid { grid-template-columns: repeat(2, 1fr); } .sk-hero { padding: 32px 20px; } .sk-hero h1 { font-size: 26px; } .cd-num { font-size: 24px; min-width: 40px; } }
@media (max-width: 480px) { .sk-grid { grid-template-columns: 1fr; } }
</style>
