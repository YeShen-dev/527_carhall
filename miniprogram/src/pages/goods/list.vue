<template>
  <view class="goods-list-page">
    <!-- Filter Bar -->
    <view class="filter-bar">
      <view class="filter-item" :class="{ active:activeFilter==='category' }" @click="toggleFilter('category')">
        <text>{{ currentCat || '全部分类' }}</text>
      </view>
      <view class="filter-item" :class="{ active:activeFilter==='sort' }" @click="toggleFilter('sort')">
        <text>{{ sortLabel }}</text>
      </view>
    </view>

    <!-- Filter dropdowns -->
    <view v-if="activeFilter==='category'" class="filter-dropdown">
      <view v-for="c in cats" :key="c" class="fd-item" @click="selectCat(c)">{{ c }}</view>
    </view>
    <view v-if="activeFilter==='sort'" class="filter-dropdown">
      <view v-for="s in sorts" :key="s.value" class="fd-item" @click="selectSort(s)">{{ s.label }}</view>
    </view>

    <!-- Product Grid -->
    <view v-if="loading" class="product-grid">
      <view v-for="i in 6" :key="i" class="product-skel">
        <view class="skeleton" style="aspect-ratio:1;border-radius:12rpx" />
        <view class="skeleton" style="height:28rpx;width:80%;margin-top:12rpx" />
        <view class="skeleton" style="height:36rpx;width:50%;margin-top:8rpx" />
      </view>
    </view>
    <view v-else class="product-grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p" @click="goDetail(p.id)" @add="handleAdd(p)" />
    </view>

    <view v-if="!loading && products.length===0" class="empty-state">
      <text class="empty-icon">📦</text><text class="empty-text">暂无商品</text>
    </view>

    <view v-if="hasMore" class="load-more" @click="loadMore"><text>{{loadingMore?'加载中...':'加载更多'}}</text></view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { listProducts } from '../../api/product.js'
import { useCartStore } from '../../store/cart.js'
import ProductCard from '../../components/ProductCard.vue'

const cartStore = useCartStore()
const products = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const page = ref(0)
const hasMore = ref(true)
const activeFilter = ref('')
const currentCat = ref('')
const currentSort = ref('default')
const cats = ['发动机','刹车系统','滤清器','点火系统','底盘系统','传感器','悬挂系统','车身附件','车灯照明']
const sorts = [{label:'默认排序',value:'default'},{label:'价格低→高',value:'price_asc'},{label:'价格高→低',value:'price_desc'}]
const sortLabel = ref('默认排序')

onLoad(opts => { if(opts.category) { currentCat.value = opts.category } })

function toggleFilter(f) { activeFilter.value = activeFilter.value===f ? '' : f }
function selectCat(c) { currentCat.value = c; activeFilter.value = ''; fetchProducts(true) }
function selectSort(s) { currentSort.value = s.value; sortLabel.value = s.label; activeFilter.value = ''; fetchProducts(true) }

async function fetchProducts(reset=true) {
  if(reset){ page.value=0; loading.value=true }else loadingMore.value=true
  try {
    const res = await listProducts({ page:page.value, size:10, category:currentCat.value||undefined, sort:currentSort.value })
    const list = res.data?.content || []
    if(reset) products.value = list
    else products.value = [...products.value, ...list]
    hasMore.value = list.length>=10; page.value++
  } finally { loading.value=false; loadingMore.value=false }
}
function goDetail(id) { uni.navigateTo({ url:`/pages/goods/detail?id=${id}` }) }
function handleAdd(p) { cartStore.add(p.id,1); uni.showToast({ title:'已加入购物车', icon:'success' }) }
function loadMore() { fetchProducts(false) }

onMounted(() => fetchProducts())
</script>

<style scoped lang="scss">
.goods-list-page { background: #F8FAFC; min-height: 100vh; }
.filter-bar { display: flex; background: #fff; padding: 16rpx 24rpx; gap: 16rpx; border-bottom: 1rpx solid #F1F5F9; }
.filter-item { padding: 10rpx 24rpx; border-radius: 8rpx; background: #F1F5F9; font-size: 26rpx; color: #475569; }
.filter-item.active { background: #EFF6FF; color: #2563EB; }
.filter-dropdown { background: #fff; padding: 16rpx 24rpx; border-bottom: 1rpx solid #F1F5F9; }
.fd-item { padding: 16rpx 0; font-size: 28rpx; color: #475569; border-bottom: 1rpx solid #F8FAFC; }
.fd-item:active { color: #2563EB; }
.product-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; padding: 16rpx 24rpx; }
.product-skel { background: #fff; border-radius: 16rpx; padding: 16rpx; }
.empty-state { display: flex; flex-direction: column; align-items: center; padding: 100rpx 0; color: #94A3B8; }
.empty-icon { font-size: 64rpx; } .empty-text { font-size: 28rpx; margin-top: 12rpx; }
.load-more { text-align: center; padding: 24rpx; color: #2563EB; font-size: 28rpx; }
</style>
