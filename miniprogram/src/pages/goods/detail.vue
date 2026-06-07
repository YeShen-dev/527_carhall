<template>
  <view class="gd-page">
    <!-- Image Swiper -->
    <swiper v-if="product" class="gd-swiper" indicator-dots>
      <swiper-item v-if="product.imageUrl">
        <image :src="product.imageUrl" mode="aspectFit" class="gd-image" />
      </swiper-item>
      <swiper-item v-else>
        <view class="gd-img-fb"><text>暂无图片</text></view>
      </swiper-item>
    </swiper>

    <!-- Info -->
    <view class="gd-info" v-if="product">
      <view class="gd-tags">
        <text class="gd-tag primary">{{ product.category }}</text>
        <text class="gd-tag">{{ product.brand }}</text>
      </view>
      <text class="gd-name">{{ product.name }}</text>
      <view class="gd-price-row">
        <text class="gd-price">¥{{ product.price }}</text>
      </view>
      <view class="gd-stock-row">
        <view class="gd-stock-bar"><view class="gd-stock-fill" :class="stockCls" :style="{width:stockPct+'%'}" /></view>
        <text class="gd-stock" :class="stockCls">{{ product.stock>0 ? `库存 ${product.stock} 件` : '暂时缺货' }}</text>
      </view>

      <!-- Specs -->
      <view class="gd-specs">
        <view class="gd-spec"><text class="sl">厂商</text><text class="sv">{{ product.manufacturer || '-' }}</text></view>
        <view class="gd-spec"><text class="sl">规格</text><text class="sv">{{ product.spec || '-' }}</text></view>
      </view>
    </view>

    <!-- Description -->
    <view class="gd-section" v-if="product">
      <text class="gd-section-title">商品描述</text>
      <text class="gd-desc">{{ product.description || '暂无描述' }}</text>
    </view>

    <!-- Comments Section -->
    <view class="gd-section" v-if="product">
      <view class="cm-header">
        <text class="cm-header-title">用户评价</text>
        <text class="cm-header-count" v-if="commentTotal>0">共 {{ commentTotal }} 条</text>
      </view>
      <!-- Summary Stats -->
      <view class="cm-stats" v-if="commentTotal>0">
        <view class="cm-avg"><text class="cm-avg-num">{{ stats.avgRating }}</text><text class="cm-avg-max">/5</text></view>
        <view class="cm-avg-stars">{{ '★'.repeat(Math.round(stats.avgRating)) }}{{ '☆'.repeat(5-Math.round(stats.avgRating)) }}</view>
        <text class="cm-avg-rate">好评率 {{ stats.goodRate }}</text>
      </view>
      <!-- Comment List -->
      <view v-if="comments.length>0" class="cm-list">
        <view v-for="c in comments" :key="c.id" class="cm-card">
          <view class="cmc-top">
            <view class="cmc-avatar" :style="{background:colorHash(c.userId)}">{{ c.username?.charAt(0)||'匿' }}</view>
            <view class="cmc-info">
              <text class="cmc-name">{{ c.username }}</text>
              <text class="cmc-rating">{{ '★'.repeat(c.rating) }}{{ '☆'.repeat(5-c.rating) }}</text>
            </view>
            <text class="cmc-time">{{ fmt(c.createTime) }}</text>
          </view>
          <text class="cmc-content">{{ c.content }}</text>
          <view v-if="c.images&&c.images.length" class="cmc-imgs">
            <image v-for="(img,i) in c.images" :key="i" :src="img" mode="aspectFill" class="cmc-img" @click="previewImg(c.images,i)" />
          </view>
        </view>
      </view>
      <view v-else-if="!commentLoading" class="cm-empty"><text>暂无评价，快来发表第一条评论吧</text></view>
      <view v-if="commentLoading" class="cm-skel">
        <view v-for="i in 2" :key="i" class="cm-skel-item"><view class="skel" style="width:56rpx;height:56rpx;border-radius:50%" /><view style="flex:1"><view class="skel" style="height:28rpx;width:60%;margin-bottom:12rpx" /><view class="skel" style="height:20rpx;width:90%" /></view></view>
      </view>
      <view v-if="hasMore" class="cm-more" @click="loadMoreComments"><text>加载更多</text></view>
    </view>

    <!-- Skeleton -->
    <view v-if="loading" class="skeleton-detail">
      <view class="skeleton" style="width:100%;aspect-ratio:1;border-radius:0" />
      <view style="padding:24rpx"><view class="skeleton" style="height:40rpx;width:60%;margin-bottom:12rpx" /><view class="skeleton" style="height:60rpx;width:40%" /></view>
    </view>

    <!-- Bottom Bar -->
    <view class="gd-bottom" v-if="product">
      <view class="gd-bottom-left" @click="toggleFav">
        <text :style="{color:fav?'#EF4444':'#94A3B8',fontSize:'40rpx'}">{{ fav ? '❤️' : '🤍' }}</text>
        <text style="font-size:20rpx;color:#94A3B8">收藏</text>
      </view>
      <button class="gd-btn-cart" @click="handleAddCart">加入购物车</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getProductDetail } from '../../api/product.js'
import { useCartStore } from '../../store/cart.js'
import request from '../../api/request.js'

const cartStore = useCartStore()
const product = ref(null)
const loading = ref(true)
const fav = ref(false)
let productId = null

// Comment state
const comments = ref([])
const commentTotal = ref(0)
const commentLoading = ref(false)
const commentPage = ref(0)
const hasMore = ref(false)
const stats = ref({ avgRating:0, goodRate:'0%' })

const colors = ['#3B82F6','#10B981','#F59E0B','#EF4444','#8B5CF6','#EC4899','#06B6D4','#F97316']
function colorHash(id) { return colors[(id||0)%colors.length] }
function fmt(t) { if(!t) return ''; const d=new Date(t); return d.getMonth()+1+'/'+d.getDate() }

onLoad(opts => { productId = opts.id })

const stockPct = computed(() => product.value ? Math.min(100,(product.value.stock/200)*100) : 0)
const stockCls = computed(() => product.value?.stock<=0 ? 'red' : product.value?.stock<20 ? 'orange' : 'green')

async function fetchDetail() {
  loading.value = true
  try { const r = await getProductDetail(productId); product.value = r.data }
  finally { loading.value = false }
}

async function loadComments() {
  commentLoading.value = true
  try {
    const [cRes, sRes] = await Promise.all([
      request.get(`/comment/product/${productId}`, { page:0, size:5 }),
      request.get(`/comment/statistics/${productId}`)
    ])
    comments.value = cRes.data?.list || []
    commentTotal.value = cRes.data?.total || 0
    hasMore.value = (cRes.data?.list?.length||0) >= 5
    commentPage.value = 0
    stats.value = sRes.data || { avgRating:0, goodRate:'0%' }
  } catch { } finally { commentLoading.value = false }
}

async function loadMoreComments() {
  commentPage.value++
  try {
    const res = await request.get(`/comment/product/${productId}`, { page:commentPage.value, size:5 })
    const more = res.data?.list || []
    comments.value = [...comments.value, ...more]
    hasMore.value = more.length >= 5
  } catch { }
}

function previewImg(urls, idx) { uni.previewImage({ urls, current:idx }) }

function handleAddCart() {
  if (!product.value || product.value.stock <= 0) return uni.showToast({ title:'暂时缺货', icon:'none' })
  cartStore.add(product.value.id, 1)
  uni.showToast({ title:'已加入购物车', icon:'success' })
}

function toggleFav() { fav.value = !fav.value; uni.showToast({ title: fav.value?'已收藏':'已取消', icon:'none' }) }

onMounted(() => { fetchDetail(); loadComments() })
</script>

<style scoped lang="scss">
.gd-page { background: #F8FAFC; min-height: 100vh; padding-bottom: 120rpx; }
.gd-swiper { width: 100%; height: 600rpx; background: #fff; }
.gd-image { width: 100%; height: 100%; }
.gd-img-fb { width:100%;height:100%;display:flex;align-items:center;justify-content:center;color:#CBD5E1;font-size:32rpx; }
.gd-info { padding: 24rpx; background: #fff; margin-bottom: 16rpx; }
.gd-tags { display: flex; gap: 12rpx; margin-bottom: 14rpx; }
.gd-tag { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 8rpx; background: #F1F5F9; color: #64748B; }
.gd-tag.primary { background: #EFF6FF; color: #2563EB; }
.gd-name { font-size: 36rpx; font-weight: 700; color: #0F172A; display: block; margin-bottom: 16rpx; line-height: 1.3; }
.gd-price-row { margin-bottom: 16rpx; }
.gd-price { font-size: 48rpx; font-weight: 800; color: #EF4444; }
.gd-stock-row { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
.gd-stock-bar { flex:1; height:10rpx; background:#F1F5F9; border-radius:5rpx; overflow:hidden; }
.gd-stock-fill { height:100%; border-radius:5rpx; }
.gd-stock-fill.green { background:linear-gradient(90deg,#10B981,#34D399); }
.gd-stock-fill.orange { background:linear-gradient(90deg,#F59E0B,#FBBF24); }
.gd-stock-fill.red { background:#EF4444; }
.gd-stock { font-size: 24rpx; }
.gd-stock.green { color:#10B981; } .gd-stock.orange { color:#F59E0B; } .gd-stock.red { color:#EF4444; }
.gd-specs { display:flex; gap:48rpx; background:#F8FAFC; border-radius:12rpx; padding:20rpx; }
.gd-spec { display:flex;flex-direction:column;gap:4rpx; }
.sl { font-size:22rpx;color:#94A3B8; } .sv { font-size:28rpx;font-weight:600;color:#0F172A; }
.gd-section { background:#fff;padding:24rpx;margin-bottom:16rpx; }
.gd-section-title { font-size:30rpx;font-weight:700;color:#0F172A;display:block;margin-bottom:12rpx; }
.gd-desc { font-size:28rpx;color:#475569;line-height:1.8; }
/* Comments */
.cm-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:12rpx; }
.cm-header-title { font-size:30rpx;font-weight:700;color:#0F172A; }
.cm-header-count { font-size:24rpx;color:#9CA3AF; }
.cm-stats { display:flex;align-items:center;gap:16rpx;background:#F8FAFC;border-radius:14rpx;padding:20rpx;margin-bottom:20rpx; }
.cm-avg { display:flex;align-items:baseline;gap:2rpx; }
.cm-avg-num { font-size:48rpx;font-weight:900;color:#F59E0B; }
.cm-avg-max { font-size:24rpx;color:#9CA3AF; }
.cm-avg-stars { font-size:24rpx;color:#F59E0B; }
.cm-avg-rate { font-size:24rpx;color:#6B7280; }
.cm-list { display:flex;flex-direction:column;gap:16rpx; }
.cm-card { background:#fff;border-radius:16rpx;padding:20rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.03); }
.cmc-top { display:flex;align-items:center;gap:14rpx;margin-bottom:14rpx; }
.cmc-avatar { width:56rpx;height:56rpx;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:24rpx;font-weight:700;color:#fff;flex-shrink:0; }
.cmc-info { flex:1;display:flex;flex-direction:column;gap:4rpx; }
.cmc-name { font-size:26rpx;font-weight:600;color:#0F172A; }
.cmc-rating { font-size:20rpx;color:#F59E0B; }
.cmc-time { font-size:22rpx;color:#9CA3AF; }
.cmc-content { font-size:28rpx;color:#374151;line-height:1.7;display:block; }
.cmc-imgs { display:flex;gap:10rpx;flex-wrap:wrap;margin-top:12rpx; }
.cmc-img { width:160rpx;height:160rpx;border-radius:10rpx; }
.cm-empty { text-align:center;padding:40rpx 0;color:#9CA3AF;font-size:26rpx; }
.cm-skel { display:flex;flex-direction:column;gap:16rpx; }
.cm-skel-item { display:flex;gap:16rpx;padding:20rpx;background:#fff;border-radius:16rpx;align-items:center; }
.cm-more { text-align:center;padding:20rpx;color:#3B82F6;font-size:26rpx; }
.skel { background:linear-gradient(90deg,#F1F5F9 25%,#E5E7EB 50%,#F1F5F9 75%);background-size:200% 100%;border-radius:8rpx; }
.gd-bottom { position:fixed;bottom:0;left:0;right:0;background:rgba(255,255,255,.95);backdrop-filter:blur(20rpx);padding:16rpx 24rpx;display:flex;align-items:center;gap:20rpx;border-top:1rpx solid #F1F5F9;padding-bottom:calc(16rpx + env(safe-area-inset-bottom)); }
.gd-bottom-left { display:flex;flex-direction:column;align-items:center;width:80rpx; }
.gd-btn-cart { flex:1;background:linear-gradient(135deg,#2563EB,#3B82F6);color:#fff;border:none;border-radius:40rpx;font-size:30rpx;font-weight:600;padding:22rpx;text-align:center; }
.skeleton-detail { background:#fff; }
</style>
