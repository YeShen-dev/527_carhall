<template>
  <view class="home-page">
    <view class="nav">
      <view class="nav-search" @click="goSearch"><text class="ns-icon">🔍</text><text class="ns-placeholder">搜索配件、品牌、车型...</text></view>
    </view>

    <swiper class="banner" indicator-dots autoplay circular interval="4000" indicator-active-color="#fff">
      <swiper-item v-for="b in banners" :key="b.id" @click="bannerClick(b)">
        <image :src="b.image" mode="aspectFill" class="banner-img" @error="b.image=b.fallback" />
        <view class="banner-mask" /><view class="banner-text"><text class="bt-title">{{b.title}}</text><text class="bt-sub">{{b.sub}}</text></view>
      </swiper-item>
    </swiper>

    <view class="cats">
      <view v-for="c in cats" :key="c.name" class="cat-item" @click="goList(c.name)"><text class="cat-icon">{{c.icon}}</text><text class="cat-name">{{c.name}}</text></view>
    </view>

    <view class="block" v-if="seckillList.length">
      <view class="block-head"><text class="bh-title">⚡限时秒杀</text><text class="bh-more" @click="goSeckill">全部→</text></view>
      <scroll-view scroll-x class="sk-scroll">
        <view v-for="a in seckillList" :key="a.id" class="sk-card" @click="goSeckill">
          <image :src="a.productImage||'https://picsum.photos/seed/sk'+a.id+'/200/200'" mode="aspectFill" class="sk-img" />
          <text class="sk-price">¥{{a.seckillPrice}}</text>
        </view>
      </scroll-view>
    </view>

    <view class="block">
      <view class="block-head"><text class="bh-title">热门推荐</text></view>
      <view v-if="loading" class="grid2"><view v-for="i in 4" :key="i" class="skel"><view class="skel-img" /><view class="skel-t" /><view class="skel-p" /></view></view>
      <view v-else class="grid2">
        <view v-for="p in products" :key="p.id" class="pcard" @click="goDetail(p.id)">
          <image :src="p.imageUrl||fallbackImg(p.id)" mode="aspectFill" class="pimg" lazy-load @error="onImgErr(p,$event)" />
          <view class="pbody"><text class="pname">{{p.name}}</text>
            <view class="prow"><text class="pprice">¥{{p.price}}</text><text class="pbtn" @click.stop="addCart(p)">🛒</text></view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listProducts } from '../../api/product.js'
import { listSeckillActivities } from '../../api/seckill.js'
import { useCartStore } from '../../store/cart.js'
const cart=useCartStore(); const products=ref([]); const seckillList=ref([]); const loading=ref(true)

const banners=[
  {id:1,title:'汽修配件一站购齐',sub:'正品保障·极速配送',image:'https://picsum.photos/seed/banner1/750/400',fallback:'https://picsum.photos/seed/banner-f1/750/400'},
  {id:2,title:'限时秒杀低至5折',sub:'优质配件限时抢购',image:'https://picsum.photos/seed/banner2/750/400',fallback:'https://picsum.photos/seed/banner-f2/750/400'},
  {id:3,title:'AI智能配件推荐',sub:'输入车型自动匹配',image:'https://picsum.photos/seed/banner3/750/400',fallback:'https://picsum.photos/seed/banner-f3/750/400',link:'/pages/ai/index'}
]
const cats=[
  {name:'发动机',icon:'🔧'},{name:'刹车系统',icon:'🛑'},{name:'滤清器',icon:'🔍'},
  {name:'点火系统',icon:'⚡'},{name:'底盘系统',icon:'🔩'},{name:'传感器',icon:'📡'},
  {name:'悬挂系统',icon:'🏗️'},{name:'车身附件',icon:'🚗'},{name:'车灯照明',icon:'💡'}
]

function fallbackImg(id){ const seeds=['auto','motor','car','tire','engine','brake','oil','light','filter','shock']; return `https://picsum.photos/seed/${seeds[id%10]}${id}/400/400` }
function onImgErr(p,e){ p.imageUrl=fallbackImg(p.id||1) }

async function load(){ loading.value=true; try{ const r=await listProducts({page:0,size:10}); products.value=r.data?.content||[] }catch{} finally{loading.value=false} }
onMounted(async()=>{ load(); try{const r=await listSeckillActivities();seckillList.value=(r.data||[]).slice(0,4)}catch{} })

function bannerClick(b){ if(b.link) uni.navigateTo({url:b.link}) }
function goSearch(){ uni.navigateTo({url:'/pages/search/search'}) }
function goList(cat){ uni.navigateTo({url:`/pages/goods/list?category=${cat}`}) }
function goSeckill(){ uni.navigateTo({url:'/pages/seckill/list'}) }
function goDetail(id){ uni.navigateTo({url:`/pages/goods/detail?id=${id}`}) }
function addCart(p){ cart.add(p.id,1); uni.showToast({title:'已加入购物车',icon:'success'}) }
</script>

<style scoped lang="scss">
.home-page{ background:#F8FAFC; min-height:100vh; }
.nav{ padding:20rpx 24rpx; background:#fff; }
.nav-search{ display:flex;align-items:center;gap:12rpx;background:#F1F5F9;border-radius:40rpx;padding:16rpx 24rpx; }
.ns-icon{ font-size:28rpx; } .ns-placeholder{ font-size:26rpx;color:#9CA3AF; }
.banner{ height:360rpx;margin:20rpx 24rpx;border-radius:20rpx;overflow:hidden;position:relative; }
.banner-img{ width:100%;height:100%; }
.banner-mask{ position:absolute;bottom:0;left:0;right:0;height:50%;background:linear-gradient(transparent,rgba(0,0,0,.4)); }
.banner-text{ position:absolute;bottom:32rpx;left:32rpx; }
.bt-title{ font-size:38rpx;font-weight:900;color:#fff;display:block; }
.bt-sub{ font-size:24rpx;color:rgba(255,255,255,.7); }
.cats{ display:grid;grid-template-columns:repeat(5,1fr);gap:8rpx;padding:24rpx; }
.cat-item{ display:flex;flex-direction:column;align-items:center;gap:8rpx; }
.cat-icon{ font-size:40rpx;width:88rpx;height:88rpx;background:#fff;border-radius:22rpx;display:flex;align-items:center;justify-content:center;box-shadow:0 2rpx 12rpx rgba(0,0,0,.04); }
.cat-name{ font-size:22rpx;color:#4B5563; }
.block{ padding:0 24rpx;margin-bottom:32rpx; }
.block-head{ display:flex;justify-content:space-between;align-items:center;margin-bottom:20rpx; }
.bh-title{ font-size:32rpx;font-weight:700;color:#111827; }
.bh-more{ font-size:24rpx;color:#06B6D4; }
.sk-scroll{ white-space:nowrap; }
.sk-card{ display:inline-flex;flex-direction:column;align-items:center;width:170rpx;margin-right:14rpx;background:#fff;border-radius:16rpx;padding:14rpx;box-shadow:0 2rpx 12rpx rgba(0,0,0,.04); }
.sk-img{ width:120rpx;height:120rpx;border-radius:12rpx; }
.sk-price{ font-size:26rpx;font-weight:800;color:#EF4444;margin-top:8rpx; }
.grid2{ display:grid;grid-template-columns:repeat(2,1fr);gap:14rpx; }
.pcard{ background:#fff;border-radius:16rpx;overflow:hidden;box-shadow:0 2rpx 12rpx rgba(0,0,0,.04); }
.pimg{ width:100%;aspect-ratio:1;display:block; }
.pbody{ padding:16rpx; }
.pname{ font-size:26rpx;font-weight:600;color:#1F2937;display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;margin-bottom:10rpx; }
.prow{ display:flex;justify-content:space-between;align-items:center; }
.pprice{ font-size:30rpx;font-weight:800;color:#111827; }
.pbtn{ font-size:32rpx; }
.skel{ background:#fff;border-radius:16rpx;padding:16rpx; }
.skel-img{ width:100%;aspect-ratio:1;background:#E5E7EB;border-radius:12rpx; }
.skel-t{ height:24rpx;background:#E5E7EB;border-radius:6rpx;margin-top:12rpx;width:80%; }
.skel-p{ height:32rpx;background:#E5E7EB;border-radius:6rpx;margin-top:8rpx;width:50%; }
</style>
