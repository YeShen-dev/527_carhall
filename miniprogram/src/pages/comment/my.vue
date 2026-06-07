<template>
  <view class="page">
    <view class="header">
      <text class="title">我的评论</text>
    </view>

    <view class="tabs">
      <view v-for="(t,i) in tabs" :key="i" class="tab" :class="{active:cur===i}" @click="cur=i">{{t}}</view>
    </view>

    <!-- 已评论 -->
    <view v-if="cur===0">
      <view v-if="loading" class="skel-list">
        <view v-for="i in 2" :key="i" class="skel-card"><view class="skel" style="width:56rpx;height:56rpx;border-radius:50%" /><view style="flex:1"><view class="skel" style="height:28rpx;width:60%;margin-bottom:12rpx" /><view class="skel" style="height:20rpx;width:90%" /></view></view>
      </view>
      <view v-else-if="comments.length===0" class="empty"><text>暂无评论</text></view>
      <view v-else class="list">
        <view v-for="c in comments" :key="c.id" class="card">
          <view class="ctop">
            <view class="cav" :style="{background:colors[(c.userId||0)%colors.length]}">{{ c.username?.charAt(0)||'?' }}</view>
            <view class="cinfo"><text class="cname">{{ c.username }}</text><text class="cstars">{{ '★'.repeat(c.rating) }}{{ '☆'.repeat(5-c.rating) }}</text></view>
            <text class="ctime">{{ fmt(c.createTime) }}</text>
          </view>
          <text class="ccontent">{{ c.content }}</text>
          <view class="cfoot"><text class="cdel" @click="del(c.id)">删除</text></view>
        </view>
      </view>
    </view>

    <!-- 待评论 -->
    <view v-if="cur===1">
      <view v-if="pending.length===0" class="empty"><text>暂无待评价商品</text></view>
      <view v-else class="list">
        <view v-for="p in pending" :key="p.productId+'-'+p.orderId" class="card row">
          <image v-if="p.productImage" :src="p.productImage" mode="aspectFill" class="pimg" />
          <view v-else class="pimg-fb"><text>📦</text></view>
          <view class="pinfo"><text class="pname">{{ p.productName }}</text></view>
          <button class="pbtn" @click="goAdd(p)">评价</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const cur = ref(0)
const tabs = ['已评论','待评论']
const comments = ref([])
const pending = ref([])
const loading = ref(true)
const colors = ['#3B82F6','#10B981','#F59E0B','#EF4444','#8B5CF6','#EC4899','#06B6D4','#F97316']

function fmt(t){ if(!t)return''; const d=new Date(t); return d.getMonth()+1+'/'+d.getDate() }

async function loadData(){
  loading.value = true
  try {
    const token = uni.getStorageSync('token')
    const [cRes,pRes] = await Promise.all([
      new Promise((resolve,reject)=>{ uni.request({ url:'http://localhost:8833/api/comment/my?type=0&page=0&size=50', method:'GET', header:{'Authorization':'Bearer '+token}, success:resolve, fail:reject }) }),
      new Promise((resolve,reject)=>{ uni.request({ url:'http://localhost:8833/api/comment/pending', method:'GET', header:{'Authorization':'Bearer '+token}, success:resolve, fail:reject }) })
    ])
    comments.value = cRes.data?.data?.list || cRes.data?.list || []
    pending.value = pRes.data?.data || pRes.data || []
  } catch { } finally { loading.value = false }
}

function del(id){
  uni.showModal({ title:'确认', content:'删除该评论？', success:async r=>{
    if(!r.confirm) return
    try {
      const token = uni.getStorageSync('token')
      await new Promise((resolve,reject)=>{ uni.request({ url:'http://localhost:8833/api/comment/'+id, method:'DELETE', header:{'Authorization':'Bearer '+token}, success:resolve, fail:reject }) })
      uni.showToast({ title:'已删除', icon:'success' })
      loadData()
    } catch { }
  }})
}

function goAdd(p){ uni.navigateTo({ url:`/pages/comment/add?productId=${p.productId}&orderId=${p.orderId}&productName=${encodeURIComponent(p.productName)}` }) }

onMounted(()=>loadData())
</script>

<style scoped lang="scss">
.page { background:#F8FAFC; min-height:100vh; padding:24rpx; }
.header { margin-bottom:20rpx; }
.title { font-size:36rpx; font-weight:800; color:#111827; }
.tabs { display:flex; background:#fff; border-radius:14rpx; padding:8rpx; margin-bottom:24rpx; }
.tab { flex:1; text-align:center; padding:16rpx; font-size:28rpx; color:#6B7280; border-radius:10rpx; transition:all .2s; }
.tab.active { background:#2563EB; color:#fff; font-weight:700; }
.skel-list { display:flex; flex-direction:column; gap:16rpx; }
.skel-card { display:flex; gap:16rpx; padding:20rpx; background:#fff; border-radius:16rpx; align-items:center; }
.skel { background:linear-gradient(90deg,#F1F5F9 25%,#E5E7EB 50%,#F1F5F9 75%); background-size:200% 100%; border-radius:8rpx; animation:shim 1.5s infinite; }
@keyframes shim { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
.empty { text-align:center; padding:60rpx 0; color:#9CA3AF; font-size:28rpx; }
.list { display:flex; flex-direction:column; gap:16rpx; }
.card { background:#fff; border-radius:16rpx; padding:20rpx; box-shadow:0 2rpx 8rpx rgba(0,0,0,.03); }
.card.row { display:flex; align-items:center; gap:16rpx; }
.ctop { display:flex; align-items:center; gap:12rpx; margin-bottom:12rpx; }
.cav { width:56rpx; height:56rpx; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:24rpx; font-weight:700; color:#fff; flex-shrink:0; }
.cinfo { flex:1; display:flex; flex-direction:column; gap:4rpx; }
.cname { font-size:26rpx; font-weight:600; color:#0F172A; }
.cstars { font-size:20rpx; color:#F59E0B; }
.ctime { font-size:22rpx; color:#9CA3AF; }
.ccontent { font-size:28rpx; color:#374151; line-height:1.6; display:block; }
.cfoot { margin-top:12rpx; display:flex; justify-content:flex-end; }
.cdel { font-size:24rpx; color:#EF4444; }
.pimg { width:80rpx; height:80rpx; border-radius:12rpx; flex-shrink:0; }
.pimg-fb { width:80rpx;height:80rpx;border-radius:12rpx;background:#F1F5F9;display:flex;align-items:center;justify-content:center;font-size:36rpx;flex-shrink:0; }
.pinfo { flex:1; }
.pname { font-size:26rpx; font-weight:600; color:#0F172A; }
.pbtn { background:#10B981; color:#fff; border:none; border-radius:10rpx; padding:10rpx 24rpx; font-size:24rpx; }
</style>
