<template>
  <div class="home-view">

    <!-- ===== Hero Banner ===== -->
    <section class="hero-banner">
      <div class="hero-bg-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-tag">PROFESSIONAL AUTO PARTS</div>
        <h1 class="hero-title">汽修配件<span class="hero-highlight">一站购齐</span></h1>
        <p class="hero-desc">覆盖九大核心品类 · 50+ 品牌正品授权 · 极速物流配送 · 7天无忧退换</p>
        <div class="hero-search">
          <el-input v-model="searchKeyword" placeholder="搜索配件名称、品牌、OE号、适用车型..." size="large"
            @keyup.enter="doHeroSearch" class="hero-search-input">
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append>
              <el-button type="primary" @click="doHeroSearch" class="hero-search-btn">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
        </div>
        <div class="hero-hot">
          <span>热门搜索：</span>
          <a v-for="kw in hotKeywords" :key="kw" @click="quickSearch(kw)">{{ kw }}</a>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-item" v-for="s in stats" :key="s.label">
          <span class="stat-num">{{ s.num }}</span>
          <span class="stat-label">{{ s.label }}</span>
        </div>
      </div>
    </section>

    <!-- ===== Seckill Strip ===== -->
    <section class="section-wrap" v-if="seckillActivities.length > 0">
      <div class="section-head">
        <div class="section-title-row">
          <h2><span class="title-icon">⚡</span> 限时秒杀</h2>
          <div class="seckill-countdown" v-if="seckillCountdown > 0">
            <span class="cd-label">距离结束</span>
            <span class="cd-digit">{{ pad2(Math.floor(seckillCountdown / 3600)) }}</span><span class="cd-sep">:</span>
            <span class="cd-digit">{{ pad2(Math.floor(seckillCountdown / 60) % 60) }}</span><span class="cd-sep">:</span>
            <span class="cd-digit">{{ pad2(seckillCountdown % 60) }}</span>
          </div>
        </div>
        <a class="section-more" @click="$router.push('/seckill')">更多秒杀 →</a>
      </div>
      <div class="seckill-strip">
        <div v-for="a in seckillActivities.slice(0, 4)" :key="a.id" class="seckill-strip-card" @click="$router.push('/seckill')">
          <div class="ss-img-wrap">
            <el-image v-if="a.productImage" :src="a.productImage" fit="cover" style="width:100%;aspect-ratio:1" />
            <div v-else class="ss-img-fb"><el-icon :size="32"><PictureFilled /></el-icon></div>
            <span class="ss-badge">秒杀</span>
          </div>
          <div class="ss-info">
            <p class="ss-name">{{ a.productName }}</p>
            <div class="ss-price-row">
              <span class="ss-price">¥{{ a.seckillPrice }}</span>
              <span class="ss-origin" v-if="a.productPrice">¥{{ a.productPrice }}</span>
            </div>
            <div class="ss-bar"><div class="ss-bar-fill" :style="{width: Math.min(100,a.stock/Math.max(a.stock+30,1)*100)+'%'}"></div></div>
            <span class="ss-stock">已抢 {{ a.stock > 0 ? Math.min(85, 100-Math.min(100,a.stock)) : 100 }}%</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== Category Grid ===== -->
    <section class="section-wrap">
      <div class="section-head">
        <h2>配件分类</h2>
        <a class="section-more" @click="scrollToProducts">查看全部 →</a>
      </div>
      <div class="category-grid">
        <div v-for="(cat, idx) in categoriesWithIcons" :key="cat.name" class="cat-card"
          :style="{ animationDelay: idx * 0.04 + 's' }" @click="selectCategory(cat.name)">
          <div class="cat-icon-box" :style="{background: cat.color}">
            <span v-html="cat.svg"></span>
          </div>
          <div class="cat-text">
            <span class="cat-name">{{ cat.name }}</span>
            <span class="cat-count">{{ cat.count }}+ 件在售</span>
          </div>
          <el-icon class="cat-go"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- ===== Brand Showcase ===== -->
    <section class="section-wrap">
      <div class="section-head">
        <h2>品牌专区</h2>
        <span class="section-subtitle">精选大牌 · 正品保障</span>
      </div>
      <div class="brand-row">
        <div v-for="b in brands" :key="b.name" class="brand-item" @click="quickSearch(b.name)">
          <div class="brand-logo" :style="{background: b.gradient}">{{ b.initial }}</div>
          <span class="brand-name">{{ b.name }}</span>
        </div>
      </div>
    </section>

    <!-- ===== Hot Products ===== -->
    <section class="section-wrap" ref="productsRef">
      <div class="section-head">
        <h2>{{ filters.category || '热门推荐' }}</h2>
        <span class="section-count">共 {{ total }} 件</span>
      </div>

      <FilterBar v-model:category="filters.category" v-model:brand="filters.brand" v-model:sort="filters.sort"
        :categories="categories" @search="doSearch" />

      <!-- Skeleton loading -->
      <div v-if="loading" class="product-grid">
        <div v-for="i in 8" :key="i" class="skeleton-card">
          <div class="skeleton skeleton-img"></div>
          <div class="skeleton-body">
            <div class="skeleton skeleton-tag" style="width:50px;height:18px"></div>
            <div class="skeleton skeleton-title"></div>
            <div class="skeleton skeleton-title" style="width:60%"></div>
            <div class="skeleton skeleton-price"></div>
          </div>
        </div>
      </div>

      <div v-else class="product-grid">
        <ProductCard v-for="(p, idx) in products" :key="p.id" :product="p"
          :style="{ animationDelay: idx * 0.05 + 's' }" class="card-anim"
          @click="goDetail(p.id)" @add-to-cart="handleAddToCart" />
      </div>

      <el-empty v-if="!loading && products.length === 0" description="未找到匹配商品">
        <el-button type="primary" round @click="resetFilters">重置筛选</el-button>
      </el-empty>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination v-model:current-page="page" :page-size="size" :total="total"
          layout="prev, pager, next" background @current-change="doSearch" />
      </div>
    </section>

    <!-- ===== Trust Bar ===== -->
    <section class="trust-bar">
      <div class="trust-item" v-for="t in trusts" :key="t.title">
        <div class="trust-icon" :style="{background: t.color}">
          <span v-html="t.svg"></span>
        </div>
        <div class="trust-text"><strong>{{ t.title }}</strong><span>{{ t.desc }}</span></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { listProducts } from '../api/product.js'
import { listSeckillActivities } from '../api/seckill.js'
import { useCartStore } from '../stores/cart.js'
import ProductCard from '../components/ProductCard.vue'
import FilterBar from '../components/FilterBar.vue'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const products = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(8)
const total = ref(0)
const searchKeyword = ref('')
const productsRef = ref(null)
const seckillActivities = ref([])
const seckillCountdown = ref(0)
let cdTimer = null

const categories = ['发动机','刹车系统','滤清器','点火系统','底盘系统','传感器','悬挂系统','车身附件','车灯照明']
const hotKeywords = ['刹车片','机油','火花塞','LED大灯','减震器','空滤','雨刮器']
const stats = [
  { num: '2,000+', label: '配件品类' }, { num: '50+', label: '合作品牌' },
  { num: '10w+', label: '服务车主' }, { num: '24H', label: '极速发货' }
]
const trusts = [
  { title:'正品保障', desc:'品牌授权 品质无忧', color:'rgba(37,99,235,0.08)',
    svg:'<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#2563EB" stroke-width="2"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87L18.18 21 12 17.27 5.82 21 7 14.14l-5-4.87 8.91-1.01z"/></svg>'},
  { title:'满299包邮', desc:'全国配送 极速送达', color:'rgba(16,185,129,0.08)',
    svg:'<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#10B981" stroke-width="2"><rect x="1" y="3" width="15" height="13" rx="1"/><polyline points="16 8 20 8 23 11 23 16 16 16"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>'},
  { title:'7天退换', desc:'无忧购物 售后保障', color:'rgba(245,158,11,0.08)',
    svg:'<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#F59E0B" stroke-width="2"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>'},
  { title:'在线客服', desc:'专业咨询 贴心服务', color:'rgba(139,92,246,0.08)',
    svg:'<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#8B5CF6" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>'}
]

const categoriesWithIcons = [
  { name:'发动机', count:320, color:'#EFF6FF',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#2563EB" stroke-width="1.5"><rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="3"/><line x1="6" y1="4" x2="6" y2="6"/><line x1="18" y1="4" x2="18" y2="6"/></svg>'},
  { name:'刹车系统', count:180, color:'#FFF7ED',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#F59E0B" stroke-width="1.5"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="4"/></svg>'},
  { name:'滤清器', count:150, color:'#EFF6FF',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#3B82F6" stroke-width="1.5"><rect x="4" y="4" width="16" height="16" rx="4"/></svg>'},
  { name:'点火系统', count:120, color:'#FFF7ED',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#EF4444" stroke-width="1.5"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10"/></svg>'},
  { name:'底盘系统', count:200, color:'#F5F3FF',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#8B5CF6" stroke-width="1.5"><rect x="3" y="8" width="18" height="8" rx="2"/></svg>'},
  { name:'传感器', count:95, color:'#ECFEFF',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#06B6D4" stroke-width="1.5"><path d="M12 2v4m0 12v4M4.93 4.93l2.83 2.83m8.48 8.48l2.83 2.83M2 12h4m12 0h4"/></svg>'},
  { name:'悬挂系统', count:130, color:'#F0FDF4',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#10B981" stroke-width="1.5"><path d="M8 2v6c0 2 2 3 4 3s4-1 4-3V2"/><path d="M12 11v11"/></svg>'},
  { name:'车身附件', count:250, color:'#F8FAFC',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#64748B" stroke-width="1.5"><rect x="3" y="5" width="18" height="14" rx="2"/></svg>'},
  { name:'车灯照明', count:110, color:'#FFFBEB',
    svg:'<svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="#F59E0B" stroke-width="1.5"><circle cx="12" cy="12" r="5"/><path d="M12 2v4m0 12v4"/></svg>'}
]

const brands = [
  { name:'博世', initial:'B', gradient:'linear-gradient(135deg,#ef4444,#dc2626)' },
  { name:'美孚', initial:'M', gradient:'linear-gradient(135deg,#3b82f6,#1d4ed8)' },
  { name:'曼牌', initial:'曼', gradient:'linear-gradient(135deg,#10b981,#059669)' },
  { name:'NGK', initial:'N', gradient:'linear-gradient(135deg,#f97316,#ea580c)' },
  { name:'SKF', initial:'S', gradient:'linear-gradient(135deg,#8b5cf6,#6d28d9)' },
  { name:'盖茨', initial:'盖', gradient:'linear-gradient(135deg,#f59e0b,#d97706)' },
  { name:'KYB', initial:'K', gradient:'linear-gradient(135deg,#06b6d4,#0891b2)' },
  { name:'飞利浦', initial:'飞', gradient:'linear-gradient(135deg,#6366f1,#4f46e5)' },
]

const filters = reactive({ keyword:'', category:'', brand:'', sort:'default' })

function quickSearch(k) { searchKeyword.value = k; doHeroSearch() }
function doHeroSearch() { if(searchKeyword.value.trim()){ filters.keyword=searchKeyword.value.trim(); filters.category=''; filters.brand=''; page.value=1; doSearch(); scrollToProducts() } }
function scrollToProducts() { productsRef.value?.scrollIntoView({behavior:'smooth',block:'start'}) }
function selectCategory(c) { filters.keyword=''; filters.category=c; filters.brand=''; page.value=1; doSearch(); scrollToProducts() }
function resetFilters() { filters.keyword=''; filters.category=''; filters.brand=''; filters.sort='default'; searchKeyword.value=''; page.value=1; doSearch() }
function pad2(n) { return String(n).padStart(2,'0') }

async function fetchSeckill() {
  try { const r = await listSeckillActivities(); seckillActivities.value = r.data || []
    if (seckillActivities.value.length > 0) {
      const endTime = new Date(seckillActivities.value[0].endTime).getTime()
      seckillCountdown.value = Math.max(0, Math.floor((endTime - Date.now())/1000))
      cdTimer = setInterval(() => { if(seckillCountdown.value>0) seckillCountdown.value-- }, 1000)
    }
  } catch { /* */ }
}

async function doSearch() {
  loading.value = true
  try {
    const res = await listProducts({ page:page.value-1, size:size.value, keyword:filters.keyword||undefined, category:filters.category||undefined, brand:filters.brand||undefined, sort:filters.sort })
    products.value = res.data.content || []; total.value = res.data.totalElements || 0
  } finally { loading.value = false }
}
async function handleAddToCart(p) { try { await cartStore.add(p.id,1); ElMessage.success(`已添加 "${p.name}"`) } catch { } }
function goDetail(id) { router.push(`/products/${id}`) }

onMounted(() => {
  if(route.query.category) filters.category = route.query.category
  if(route.query.keyword) { searchKeyword.value = route.query.keyword; filters.keyword = route.query.keyword }
  doSearch(); fetchSeckill()
})
onUnmounted(() => { if(cdTimer) clearInterval(cdTimer) })
</script>

<style scoped>
.home-view { display: flex; flex-direction: column; gap: 36px; padding-bottom: 8px; }

/* ===== Hero ===== */
.hero-banner {
  position: relative; background: linear-gradient(135deg, #1E3A8A 0%, #2563EB 40%, #3B82F6 70%, #60A5FA 100%);
  border-radius: 24px; padding: 56px 56px 40px; overflow: hidden;
}
.hero-bg-shapes { position: absolute; inset: 0; pointer-events: none; }
.shape { position: absolute; border-radius: 50%; background: rgba(255,255,255,0.06); }
.shape-1 { width: 400px; height: 400px; top: -100px; right: -80px; }
.shape-2 { width: 200px; height: 200px; bottom: -40px; left: 10%; }
.shape-3 { width: 100px; height: 100px; top: 30%; right: 30%; }
.hero-content { position: relative; z-index: 1; max-width: 680px; }
.hero-tag { display: inline-block; background: rgba(255,255,255,0.15); color: #fff; font-size: 11px; font-weight: 700; letter-spacing: 2px; padding: 5px 16px; border-radius: 20px; margin-bottom: 18px; backdrop-filter: blur(8px); }
.hero-title { font-size: 48px; font-weight: 900; color: #fff; line-height: 1.15; margin-bottom: 14px; letter-spacing: -1px; }
.hero-highlight { background: linear-gradient(135deg, #FDE68A, #F59E0B); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.hero-desc { font-size: 15px; color: rgba(255,255,255,0.75); margin-bottom: 28px; }
.hero-search { margin-bottom: 18px; }
.hero-search-input :deep(.el-input__wrapper) { border-radius: 16px !important; padding: 6px 6px 6px 18px; background: rgba(255,255,255,0.95) !important; border: none !important; box-shadow: 0 8px 32px rgba(0,0,0,0.15) !important; height: 52px; }
.hero-search-input :deep(.el-input__wrapper:hover) { background: #fff !important; }
.hero-search-input :deep(.el-input.is-focus .el-input__wrapper) { box-shadow: 0 0 0 3px rgba(255,255,255,0.3), 0 8px 32px rgba(0,0,0,0.15) !important; }
.hero-search-input :deep(.el-input-group__append) { background: none; border: none; padding: 0; }
.hero-search-btn { border-radius: 12px; font-weight: 600; padding: 14px 28px; font-size: 15px; background: #1D4ED8; border-color: #1D4ED8; }
.hero-hot { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; font-size: 13px; color: rgba(255,255,255,0.6); }
.hero-hot a { color: rgba(255,255,255,0.85); cursor: pointer; padding: 4px 14px; border-radius: 14px; background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.15); transition: all 0.2s; }
.hero-hot a:hover { background: rgba(255,255,255,0.2); color: #fff; }
.hero-stats { display: flex; gap: 24px; margin-top: 36px; position: relative; z-index: 1; }
.stat-item { display: flex; flex-direction: column; gap: 2px; }
.stat-num { font-size: 26px; font-weight: 800; color: #fff; }
.stat-label { font-size: 12px; color: rgba(255,255,255,0.65); }

/* ===== Section ===== */
.section-wrap { display: flex; flex-direction: column; gap: 16px; }
.section-head { display: flex; align-items: center; justify-content: space-between; }
.section-head h2 { font-size: 20px; font-weight: 700; color: #0F172A; display: flex; align-items: center; gap: 8px; }
.title-icon { font-size: 22px; }
.section-title-row { display: flex; align-items: center; gap: 16px; }
.section-subtitle { font-size: 13px; color: #94A3B8; }
.section-more { font-size: 13px; color: #2563EB; cursor: pointer; font-weight: 500; }
.section-more:hover { text-decoration: underline; }
.section-count { font-size: 13px; color: #94A3B8; }

/* Seckill countdown */
.seckill-countdown { display: flex; align-items: center; gap: 4px; background: #FEF2F2; padding: 6px 14px; border-radius: 10px; }
.cd-label { font-size: 11px; color: #EF4444; margin-right: 6px; }
.cd-digit { background: #EF4444; color: #fff; padding: 2px 6px; border-radius: 4px; font-size: 14px; font-weight: 700; font-family: 'SF Mono','Courier New',monospace; min-width: 24px; text-align: center; }
.cd-sep { color: #EF4444; font-weight: 700; font-size: 14px; }

/* Seckill strip */
.seckill-strip { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.seckill-strip-card { background: #fff; border-radius: 14px; overflow: hidden; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.06); border: 1px solid #F1F5F9; transition: all 0.25s; }
.seckill-strip-card:hover { transform: translateY(-4px); box-shadow: 0 12px 28px rgba(0,0,0,0.1); }
.ss-img-wrap { position: relative; }
.ss-img-fb { width: 100%; aspect-ratio: 1; display: flex; align-items: center; justify-content: center; background: #F1F5F9; color: #CBD5E1; }
.ss-badge { position: absolute; top: 0; left: 0; background: linear-gradient(135deg,#EF4444,#F97316); color: #fff; font-size: 11px; font-weight: 700; padding: 4px 12px; border-radius: 0 0 10px 0; }
.ss-info { padding: 12px 14px; }
.ss-name { font-size: 14px; font-weight: 600; color: #0F172A; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 6px; }
.ss-price-row { display: flex; align-items: baseline; gap: 6px; margin-bottom: 8px; }
.ss-price { font-size: 22px; font-weight: 800; color: #EF4444; }
.ss-origin { font-size: 12px; color: #94A3B8; text-decoration: line-through; }
.ss-bar { height: 5px; background: #F1F5F9; border-radius: 3px; overflow: hidden; margin-bottom: 4px; }
.ss-bar-fill { height: 100%; background: linear-gradient(90deg,#EF4444,#F97316); border-radius: 3px; transition: width 0.6s; }
.ss-stock { font-size: 11px; color: #EF4444; }

/* Category grid */
.category-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.cat-card { background: #fff; border: 1px solid #F1F5F9; border-radius: 16px; padding: 18px 20px; display: flex; align-items: center; gap: 14px; cursor: pointer; transition: all 0.25s; animation: fadeInUp 0.5s ease both; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.cat-card:hover { transform: translateX(4px); border-color: #2563EB; box-shadow: 0 8px 24px rgba(37,99,235,0.1); }
.cat-icon-box { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.cat-text { flex: 1; }
.cat-name { font-size: 15px; font-weight: 600; color: #0F172A; display: block; }
.cat-count { font-size: 12px; color: #94A3B8; }
.cat-go { color: #CBD5E1; font-size: 14px; }

/* Brand row */
.brand-row { display: grid; grid-template-columns: repeat(8, 1fr); gap: 12px; }
.brand-item { background: #fff; border: 1px solid #F1F5F9; border-radius: 14px; padding: 16px 8px; display: flex; flex-direction: column; align-items: center; gap: 8px; cursor: pointer; transition: all 0.25s; }
.brand-item:hover { transform: translateY(-4px); border-color: #2563EB; box-shadow: 0 8px 24px rgba(37,99,235,0.08); }
.brand-logo { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 20px; font-weight: 800; }
.brand-name { font-size: 12px; font-weight: 600; color: #334155; }

/* Product grid */
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.card-anim { animation: fadeInUp 0.5s ease both; }

/* Skeleton */
.skeleton-card { background: #fff; border-radius: 16px; overflow: hidden; border: 1px solid #F1F5F9; }
.skeleton-img { width: 100%; aspect-ratio: 1; }
.skeleton-body { padding: 14px; display: flex; flex-direction: column; gap: 8px; }
.skeleton-tag { border-radius: 6px; }
.skeleton-title { width: 80%; height: 16px; }
.skeleton-price { width: 40%; height: 22px; }

.pagination-wrapper { display: flex; justify-content: center; padding: 28px 0 8px; }

/* Trust bar */
.trust-bar { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; background: #fff; border-radius: 20px; padding: 24px; border: 1px solid #F1F5F9; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.trust-item { display: flex; align-items: center; gap: 12px; }
.trust-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.trust-text { display: flex; flex-direction: column; gap: 2px; }
.trust-text strong { font-size: 14px; color: #0F172A; }
.trust-text span { font-size: 11px; color: #94A3B8; }

@media (max-width: 1200px) { .product-grid,.seckill-strip { grid-template-columns: repeat(3,1fr); } .brand-row { grid-template-columns: repeat(4,1fr); } }
@media (max-width: 900px) { .product-grid,.seckill-strip { grid-template-columns: repeat(2,1fr); } .brand-row { grid-template-columns: repeat(3,1fr); } .category-grid { grid-template-columns: repeat(2,1fr); } .hero-banner { padding: 32px 24px; } .hero-title { font-size: 30px; } .hero-stats { flex-wrap: wrap; gap: 16px; } .trust-bar { grid-template-columns: repeat(2,1fr); } }
@media (max-width: 520px) { .product-grid,.seckill-strip { grid-template-columns: 1fr; } .brand-row { grid-template-columns: repeat(2,1fr); } .category-grid { grid-template-columns: 1fr; } .trust-bar { grid-template-columns: 1fr; } .hero-title { font-size: 24px; } }
</style>
