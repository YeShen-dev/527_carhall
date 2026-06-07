<template>
  <view class="col-page">
    <view v-if="!loading && list.length>0" class="product-grid">
      <ProductCard v-for="p in list" :key="p.id" :product="p" @click="goDetail(p.id)" @add="handleAdd(p)" />
    </view>
    <EmptyState v-else-if="!loading" icon="❤️" text="暂无收藏" />
    <view v-else class="product-grid">
      <view v-for="i in 4" :key="i" class="skeleton" style="aspect-ratio:1;border-radius:16rpx" />
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCollections } from '../../api/user.js'
import { useCartStore } from '../../store/cart.js'
import ProductCard from '../../components/ProductCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const list = ref([]); const loading = ref(true); const cartStore = useCartStore()

onMounted(async ()=>{
  try{ const r=await getCollections(); list.value=r.data||[] }catch{}finally{loading.value=false}
})

function goDetail(id) { uni.navigateTo({ url:`/pages/goods/detail?id=${id}` }) }
function handleAdd(p) { cartStore.add(p.id,1); uni.showToast({title:'已加入购物车',icon:'success'}) }
</script>

<style scoped lang="scss">
.col-page { background:#F8FAFC;min-height:100vh; }
.product-grid { display:grid;grid-template-columns:repeat(2,1fr);gap:16rpx;padding:24rpx; }
</style>
