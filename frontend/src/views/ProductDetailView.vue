<template>
  <div class="pd-page">
    <!-- Skeleton -->
    <template v-if="loading">
      <div class="pd-layout">
        <div class="skeleton pd-skeleton-img"></div>
        <div class="pd-info">
          <div class="skeleton" style="width:120px;height:24px"></div>
          <div class="skeleton" style="width:80%;height:34px;margin-top:12px"></div>
          <div class="skeleton" style="width:40%;height:40px;margin-top:12px"></div>
          <div class="skeleton" style="width:100%;height:60px;margin-top:16px"></div>
          <div class="skeleton" style="width:100%;height:48px;margin-top:16px"></div>
        </div>
      </div>
    </template>

    <!-- Product -->
    <template v-else-if="product">
      <div class="pd-layout">
        <!-- Image with zoom -->
        <div class="pd-image-col">
          <div class="pd-image-wrap" ref="imageWrap"
               @mousemove="handleMouseMove" @mouseenter="zoomVisible = true" @mouseleave="zoomVisible = false">
            <el-image v-if="product.imageUrl" :src="product.imageUrl" fit="cover"
              class="pd-image" :style="imageStyle" />
            <div v-else class="pd-img-fb">
              <el-icon :size="80"><PictureFilled /></el-icon>
              <span>暂无图片</span>
            </div>
            <!-- Zoom lens -->
            <div v-if="zoomVisible && product.imageUrl" class="zoom-lens"
              :style="{ left: lensX + 'px', top: lensY + 'px' }"></div>
          </div>
          <!-- Zoom result -->
          <div v-if="zoomVisible && product.imageUrl" class="zoom-result"
            :style="{ backgroundImage: 'url(' + product.imageUrl + ')', backgroundPosition: bgPos, backgroundSize: zoomBgSize }"></div>
        </div>

        <!-- Info -->
        <div class="pd-info">
          <div class="pd-breadcrumb">
            <a @click="$router.push('/')">首页</a> /
            <a @click="$router.push(`/?category=${product.category}`)">{{ product.category }}</a> /
            <span>{{ product.name }}</span>
          </div>

          <div class="pd-tags">
            <span class="pd-tag primary">{{ product.category }}</span>
            <span class="pd-tag">{{ product.brand }}</span>
            <span class="pd-tag" v-if="product.spec">{{ product.spec }}</span>
          </div>

          <h1 class="pd-name">{{ product.name }}</h1>

          <div class="pd-price-row">
            <span class="pd-price">¥{{ product.price }}</span>
            <span class="pd-price-original" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
          </div>

          <!-- Stock bar -->
          <div class="pd-stock-row">
            <div class="pd-stock-bar">
              <div class="pd-stock-fill" :class="stockColor" :style="{width: stockPercent+'%'}"></div>
            </div>
            <span class="pd-stock-text" :class="{ 'text-green': product.stock>=20, 'text-orange': product.stock>0 && product.stock<20, 'text-red': product.stock<=0 }">
              {{ product.stock > 0 ? `库存 ${product.stock} 件` : '暂时缺货' }}
            </span>
          </div>

          <!-- Specs -->
          <div class="pd-specs">
            <div class="pd-spec"><span class="sl">厂商</span><span class="sv">{{ product.manufacturer || '-' }}</span></div>
            <div class="pd-spec"><span class="sl">规格</span><span class="sv">{{ product.spec || '-' }}</span></div>
          </div>

          <!-- Actions -->
          <div class="pd-actions">
            <el-input-number v-model="qty" :min="1" :max="Math.max(1, product.stock)" size="large"
              :disabled="product.stock <= 0" class="pd-qty" />
            <el-button type="primary" size="large" :disabled="product.stock <= 0"
              @click="handleAddToCart" class="pd-cart-btn">
              <el-icon><ShoppingCart /></el-icon>
              {{ product.stock > 0 ? '加入购物车' : '暂时缺货' }}
            </el-button>
          </div>

          <!-- Meta info -->
          <div class="pd-meta">
            <div class="pd-meta-item">
              <el-icon><CircleCheck /></el-icon> 正品保障
            </div>
            <div class="pd-meta-item">
              <el-icon><Van /></el-icon> 满¥299包邮
            </div>
            <div class="pd-meta-item">
              <el-icon><Clock /></el-icon> 7天退换
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs below -->
      <div class="pd-tabs-section">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品描述" name="desc">
            <div class="pd-desc-card">
              <p>{{ product.description || '暂无描述信息' }}</p>
            </div>
          </el-tab-pane>
          <el-tab-pane label="用户评价" name="reviews">
            <div class="pd-desc-card comment-section">
              <!-- Stats header -->
              <div class="cm-stats" v-if="commentStats">
                <div class="cm-stats-left">
                  <span class="cm-avg">{{ commentStats.avgRating || 0 }}</span>
                  <div class="cm-stars"><span v-for="s in 5" :key="s" :style="{color:s<=Math.round(commentStats.avgRating||0)?'#F59E0B':'#E2E8F0'}">★</span></div>
                  <span class="cm-rate">好评率 {{ commentStats.goodRate || '0%' }}</span>
                </div>
                <div class="cm-stats-right">
                  <span>共 {{ commentStats.totalCount || 0 }} 条评论</span>
                </div>
              </div>
              <!-- Filter tabs -->
              <div class="cm-filters">
                <span :class="{ active: commentRating === null }" @click="commentRating=null;loadComments()">全部</span>
                <span :class="{ active: commentRating === 5 }" @click="commentRating=5;loadComments()">好评 ({{ commentStats?.goodCount || 0 }})</span>
                <span :class="{ active: commentRating === 3 }" @click="commentRating=3;loadComments()">中评 ({{ commentStats?.middleCount || 0 }})</span>
                <span :class="{ active: commentRating === 1 }" @click="commentRating=1;loadComments()">差评 ({{ commentStats?.badCount || 0 }})</span>
              </div>
              <!-- Comment list -->
              <div v-if="comments.length > 0" class="cm-list">
                <div v-for="c in comments" :key="c.id" class="cm-card">
                  <div class="cm-card-header">
                    <el-avatar :size="36">{{ c.username?.charAt(0) || 'U' }}</el-avatar>
                    <div>
                      <span class="cm-username">{{ c.username }}</span>
                      <span class="cm-stars-sm">{{ '★'.repeat(c.rating) }}{{ '☆'.repeat(5-c.rating) }}</span>
                    </div>
                    <span class="cm-time">{{ formatTime(c.createTime) }}</span>
                  </div>
                  <p class="cm-content">{{ c.content }}</p>
                  <div v-if="c.images?.length" class="cm-images">
                    <el-image v-for="(img,i) in c.images" :key="i" :src="img" fit="cover"
                      :preview-src-list="c.images" style="width:80px;height:80px;border-radius:8px;cursor:pointer" />
                  </div>
                  <div class="cm-card-footer">
                    <span class="cm-like" :class="{ liked: c.liked }" @click="handleLike(c)">
                      <el-icon><CaretTop /></el-icon> {{ c.likeCount || 0 }}
                    </span>
                    <span class="cm-reply-btn" @click="showReplyInput(c)">回复</span>
                    <span v-if="c.isAppend" class="cm-append-tag">追评</span>
                  </div>
                  <!-- Replies -->
                  <div v-if="c.replies?.length" class="cm-replies">
                    <div v-for="r in c.replies" :key="r.id" class="cm-reply">
                      <span class="cm-r-user">{{ r.username }}</span>
                      <span v-if="r.isMerchant" class="cm-merchant-tag">商家</span>
                      <span class="cm-r-content">{{ r.content }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无评价" :image-size="60" />
              <div v-if="commentTotal > comments.length" class="cm-more" @click="loadMoreComments">加载更多</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>

    <el-empty v-else-if="!loading" description="商品不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '../api/product.js'
import { useCartStore } from '../stores/cart.js'
import { useUserStore } from '../stores/user.js'
import { getComments, getCommentStats, toggleLike } from '../api/comment.js'

const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()
const product = ref(null)
const loading = ref(false)
const qty = ref(1)
const activeTab = ref('desc')
// Comment state
const comments = ref([])
const commentStats = ref(null)
const commentRating = ref(null)
const commentPage = ref(0)
const commentTotal = ref(0)

async function loadComments() {
  try {
    const [cRes, sRes] = await Promise.all([
      getComments(route.params.id, { rating: commentRating.value || undefined, page: 0, size: 5 }),
      getCommentStats(route.params.id)
    ])
    comments.value = cRes.data.list || []
    commentTotal.value = cRes.data.total || 0
    commentStats.value = sRes.data
  } catch { }
}
async function loadMoreComments() {
  commentPage.value++
  try {
    const res = await getComments(route.params.id, { rating: commentRating.value || undefined, page: commentPage.value, size: 5 })
    comments.value = [...comments.value, ...(res.data.list || [])]
    commentTotal.value = res.data.total || 0
  } catch { }
}
async function handleLike(c) {
  if (!userStore.isLoggedIn) return ElMessage.warning('请先登录')
  try {
    const res = await toggleLike(c.id); c.liked = res.data.liked
    c.likeCount += res.data.liked ? 1 : -1
  } catch { }
}
function formatTime(t) { if(!t) return ''; return new Date(t).toLocaleDateString('zh-CN') }
function showReplyInput() { ElMessage.info('回复功能开发中') }

// Zoom state
const zoomVisible = ref(false)
const lensX = ref(0)
const lensY = ref(0)
const mouseX = ref(0)
const mouseY = ref(0)
const imageWrap = ref(null)

const LENS_SIZE = 120
const ZOOM_SCALE = 2.5

const imageStyle = computed(() => {
  if (!zoomVisible.value) return {}
  const rect = imageWrap.value?.getBoundingClientRect()
  if (!rect) return {}
  return { transformOrigin: `${mouseX.value - rect.left}px ${mouseY.value - rect.top}px`, transform: 'scale(1.5)' }
})

const bgPos = computed(() => {
  const rect = imageWrap.value?.getBoundingClientRect()
  if (!rect) return '0% 0%'
  const xPct = ((mouseX.value - rect.left) / rect.width) * 100
  const yPct = ((mouseY.value - rect.top) / rect.height) * 100
  return `${xPct}% ${yPct}%`
})
const zoomBgSize = computed(() => `${ZOOM_SCALE * 100}%`)

function handleMouseMove(e) {
  const rect = imageWrap.value?.getBoundingClientRect()
  if (!rect) return
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  lensX.value = Math.min(Math.max(0, e.clientX - rect.left - LENS_SIZE/2), rect.width - LENS_SIZE)
  lensY.value = Math.min(Math.max(0, e.clientY - rect.top - LENS_SIZE/2), rect.height - LENS_SIZE)
}

const stockPercent = computed(() => {
  if (!product.value) return 0
  return Math.min(100, (product.value.stock / 200) * 100)
})
const stockColor = computed(() => {
  if (!product.value) return ''
  if (product.value.stock <= 0) return 'fill-red'
  if (product.value.stock < 20) return 'fill-orange'
  return 'fill-green'
})

async function fetchDetail() {
  loading.value = true
  try {
    const r = await getProductDetail(route.params.id)
    product.value = r.data
    loadComments()
  } finally { loading.value = false }
}

async function handleAddToCart() {
  try {
    await cartStore.add(product.value.id, qty.value)
    ElMessage.success(`已添加 "${product.value.name}" ×${qty.value} 到购物车`)
  } catch { ElMessage.error('添加失败') }
}

onMounted(() => fetchDetail())
</script>

<style scoped>
.pd-page { max-width: 1200px; margin: 0 auto; }
.pd-layout { display: grid; grid-template-columns: 1fr 1fr; gap: 48px; align-items: start; margin-bottom: 36px; }

/* Image */
.pd-image-col { position: relative; }
.pd-image-wrap { position: relative; border-radius: 20px; overflow: hidden; background: #F8FAFC; cursor: crosshair; border: 1px solid #F1F5F9; }
.pd-image { width: 100%; aspect-ratio: 1; display: block; transition: transform 0.1s ease; }
.pd-img-fb { width: 100%; aspect-ratio: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; background: #F1F5F9; color: #CBD5E1; }

.zoom-lens {
  position: absolute; width: 120px; height: 120px;
  border: 2px solid #2563EB; border-radius: 8px;
  background: rgba(37,99,235,0.08); pointer-events: none;
  box-shadow: 0 0 0 9999px rgba(255,255,255,0.3);
}
.zoom-result {
  position: absolute; top: 0; left: calc(100% + 16px);
  width: 100%; aspect-ratio: 1; border-radius: 16px;
  border: 1px solid #E2E8F0; background-repeat: no-repeat;
  box-shadow: 0 12px 36px rgba(0,0,0,0.12); z-index: 50;
}

/* Info */
.pd-breadcrumb { font-size: 13px; color: #94A3B8; margin-bottom: 12px; }
.pd-breadcrumb a { color: #2563EB; cursor: pointer; }
.pd-tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 14px; }
.pd-tag { font-size: 11px; font-weight: 600; padding: 4px 12px; border-radius: 8px; background: #F1F5F9; color: #64748B; }
.pd-tag.primary { background: #EFF6FF; color: #2563EB; }

.pd-name { font-size: 28px; font-weight: 700; color: #0F172A; line-height: 1.3; margin-bottom: 16px; }

.pd-price-row { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.pd-price { font-size: 36px; font-weight: 800; color: #EF4444; }
.pd-price-original { font-size: 16px; color: #94A3B8; text-decoration: line-through; }

/* Stock */
.pd-stock-row { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.pd-stock-bar { flex: 1; height: 8px; background: #F1F5F9; border-radius: 4px; overflow: hidden; }
.pd-stock-fill { height: 100%; border-radius: 4px; transition: width 0.6s; }
.fill-green { background: linear-gradient(90deg,#10B981,#34D399); }
.fill-orange { background: linear-gradient(90deg,#F59E0B,#FBBF24); }
.fill-red { background: #EF4444; }
.pd-stock-text { font-size: 13px; font-weight: 600; white-space: nowrap; }
.text-green { color: #10B981; }
.text-orange { color: #F59E0B; }
.text-red { color: #EF4444; }

/* Specs */
.pd-specs { display: flex; gap: 32px; margin-bottom: 24px; background: #F8FAFC; border-radius: 12px; padding: 14px 20px; }
.pd-spec { display: flex; flex-direction: column; gap: 4px; }
.sl { font-size: 12px; color: #94A3B8; }
.sv { font-size: 15px; font-weight: 600; color: #0F172A; }

/* Actions */
.pd-actions { display: flex; gap: 12px; margin-bottom: 20px; }
.pd-qty :deep(.el-input__wrapper) { height: 48px; }
.pd-cart-btn { flex: 1; height: 48px; font-size: 16px; font-weight: 700; border-radius: 14px; }

/* Meta */
.pd-meta { display: flex; gap: 20px; }
.pd-meta-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #64748B; }
.pd-meta-item .el-icon { color: #10B981; }

/* Tabs */
.pd-tabs-section { background: #fff; border-radius: 16px; padding: 20px 24px; border: 1px solid #F1F5F9; box-shadow: 0 1px 2px rgba(0,0,0,0.04); }
.pd-desc-card { padding: 16px 0; font-size: 14px; color: #475569; line-height: 1.8; }
.text-muted { color: #94A3B8; }

/* Skeleton */
.pd-skeleton-img { width: 100%; aspect-ratio: 1; border-radius: 20px; }

/* Comment section */
.comment-section { padding: 0; }
.cm-stats { display: flex; justify-content: space-between; align-items: center; padding: 16px 0; border-bottom: 1px solid #F1F5F9; margin-bottom: 12px; }
.cm-stats-left { display: flex; align-items: center; gap: 12px; }
.cm-avg { font-size: 36px; font-weight: 800; color: #F59E0B; }
.cm-stars { font-size: 18px; color: #F59E0B; }
.cm-rate { font-size: 13px; color: #64748B; }
.cm-stats-right { font-size: 13px; color: #94A3B8; }
.cm-filters { display: flex; gap: 8px; margin-bottom: 16px; }
.cm-filters span { padding: 6px 16px; border-radius: 20px; font-size: 13px; cursor: pointer; color: #64748B; background: #F1F5F9; transition: all 0.2s; }
.cm-filters span.active { background: #2563EB; color: #fff; }
.cm-list { display: flex; flex-direction: column; gap: 16px; }
.cm-card { background: #F8FAFC; border-radius: 12px; padding: 16px; }
.cm-card-header { display: flex; align-items: center; gap: 10px; }
.cm-username { font-size: 14px; font-weight: 600; color: #0F172A; display: block; }
.cm-stars-sm { font-size: 12px; color: #F59E0B; }
.cm-time { margin-left: auto; font-size: 12px; color: #94A3B8; }
.cm-content { font-size: 14px; color: #334155; line-height: 1.6; margin: 10px 0; }
.cm-images { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
.cm-card-footer { display: flex; align-items: center; gap: 16px; }
.cm-like { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #94A3B8; cursor: pointer; }
.cm-like.liked { color: #2563EB; }
.cm-reply-btn { font-size: 13px; color: #94A3B8; cursor: pointer; }
.cm-append-tag { font-size: 11px; background: #FEF2F2; color: #EF4444; padding: 2px 8px; border-radius: 4px; }
.cm-replies { margin-top: 12px; padding-top: 12px; border-top: 1px solid #E2E8F0; }
.cm-reply { padding: 6px 0; font-size: 13px; display: flex; gap: 6px; }
.cm-r-user { color: #2563EB; font-weight: 600; }
.cm-merchant-tag { background: #F59E0B; color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 4px; }
.cm-r-content { color: #475569; }
.cm-more { text-align: center; padding: 12px; color: #2563EB; font-size: 13px; cursor: pointer; }

@media (max-width: 768px) {
  .pd-layout { grid-template-columns: 1fr; gap: 24px; }
  .pd-name { font-size: 22px; }
  .pd-price { font-size: 28px; }
  .zoom-result { display: none; }
  .zoom-lens { display: none; }
}
</style>
