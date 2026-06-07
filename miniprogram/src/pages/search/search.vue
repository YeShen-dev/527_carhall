<template>
  <view class="search-page">
    <view class="search-bar">
      <input v-model="keyword" placeholder="搜索配件、品牌..." class="search-input" @confirm="doSearch" focus />
      <text class="search-cancel" @click="goBack">取消</text>
    </view>

    <!-- Hot searches -->
    <view v-if="!searched" class="search-hot">
      <text class="sh-title">热门搜索</text>
      <view class="sh-tags">
        <text v-for="k in hotKeys" :key="k" class="sh-tag" @click="quickSearch(k)">{{ k }}</text>
      </view>
    </view>

    <view v-if="!searched" class="search-history">
      <text class="sh-title">搜索历史</text>
      <view class="sh-tags">
        <text v-for="k in history" :key="k" class="sh-tag history" @click="quickSearch(k)">{{ k }}</text>
      </view>
    </view>

    <!-- Results -->
    <view v-if="searched" class="search-results">
      <text class="sr-count">找到 {{ results.length }} 个商品</text>
      <view class="product-grid">
        <ProductCard v-for="p in results" :key="p.id" :product="p" @click="goDetail(p.id)" @add="handleAdd(p)" />
      </view>
      <view v-if="results.length===0" class="empty-state"><text>未找到相关商品</text></view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { searchProducts } from '../../api/product.js'
import { useCartStore } from '../../store/cart.js'
import ProductCard from '../../components/ProductCard.vue'

const cartStore = useCartStore()
const keyword = ref('')
const searched = ref(false)
const results = ref([])
const hotKeys = ['刹车片','机油','火花塞','LED大灯','减震器','空滤']
const history = ref(JSON.parse(uni.getStorageSync('searchHistory')||'[]'))

async function doSearch() {
  if(!keyword.value.trim()) return
  saveHistory(keyword.value)
  try { const r = await searchProducts(keyword.value); results.value = r.data?.content || [] }
  finally { searched.value = true }
}

function quickSearch(k) { keyword.value = k; doSearch() }
function saveHistory(k) {
  let h = history.value.filter(i => i !== k)
  h.unshift(k); if(h.length>10) h.pop()
  history.value = h; uni.setStorageSync('searchHistory', JSON.stringify(h))
}
function goDetail(id) { uni.navigateTo({ url:`/pages/goods/detail?id=${id}` }) }
function handleAdd(p) { cartStore.add(p.id,1); uni.showToast({title:'已加入购物车',icon:'success'}) }
function goBack() { uni.navigateBack() }
</script>

<style scoped lang="scss">
.search-page { background:#F8FAFC;min-height:100vh; }
.search-bar { display:flex;align-items:center;gap:16rpx;padding:16rpx 24rpx;background:#fff; }
.search-input { flex:1;background:#F1F5F9;border-radius:40rpx;padding:16rpx 24rpx;font-size:28rpx; }
.search-cancel { font-size:28rpx;color:#64748B;white-space:nowrap; }
.search-hot, .search-history { padding:24rpx; }
.sh-title { font-size:28rpx;font-weight:600;color:#0F172A;display:block;margin-bottom:16rpx; }
.sh-tags { display:flex;flex-wrap:wrap;gap:12rpx; }
.sh-tag { padding:10rpx 24rpx;border-radius:20rpx;background:#fff;font-size:24rpx;color:#475569;box-shadow:0 2rpx 6rpx rgba(0,0,0,.03); }
.sh-tag.history { background:#F1F5F9; }
.search-results { padding:24rpx; }
.sr-count { font-size:24rpx;color:#94A3B8;margin-bottom:16rpx;display:block; }
.product-grid { display:grid;grid-template-columns:repeat(2,1fr);gap:16rpx; }
.empty-state { text-align:center;padding:80rpx 0;color:#94A3B8; }
</style>
