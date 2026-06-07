<template>
  <view class="addr-page">
    <view class="addr-list" v-if="list.length>0">
      <view v-for="a in list" :key="a.id" class="addr-card" @click="selectAddr(a)">
        <view class="ac-left">
          <text class="ac-name">{{ a.receiverName }}  {{ a.phone }}</text>
          <text class="ac-detail">{{ a.province }}{{ a.city }}{{ a.district }} {{ a.detailAddress }}</text>
        </view>
        <view class="ac-right">
          <text v-if="a.isDefault===1" class="ac-default">默认</text>
          <text class="ac-edit" @click.stop="openForm(a)">编辑</text>
          <text class="ac-del" @click.stop="delAddr(a.id)">删除</text>
        </view>
      </view>
    </view>
    <EmptyState v-else icon="📍" text="暂无收货地址" />

    <button class="addr-add" @click="openForm()">+ 添加收货地址</button>

    <!-- Form Popup -->
    <view class="addr-mask" v-if="showForm" @click="showForm=false"></view>
    <view class="addr-form" v-if="showForm">
      <text class="af-title">{{ editingId ? '编辑地址' : '新增地址' }}</text>
      <input v-model="form.receiverName" class="af-input" placeholder="收货人姓名" />
      <input v-model="form.phone" class="af-input" type="number" placeholder="联系电话" maxlength="11" />
      <input v-model="form.province" class="af-input" placeholder="省" />
      <input v-model="form.city" class="af-input" placeholder="市" />
      <input v-model="form.district" class="af-input" placeholder="区" />
      <input v-model="form.detailAddress" class="af-input" placeholder="详细地址" />
      <view class="af-default" @click="form.isDefault=form.isDefault===1?0:1">
        <text :style="{color:form.isDefault===1?'#2563EB':'#9CA3AF'}">{{ form.isDefault===1 ? '● 默认地址' : '○ 设为默认地址' }}</text>
      </view>
      <view class="af-actions">
        <button class="af-cancel" @click="showForm=false">取消</button>
        <button class="af-save" @click="saveAddr">{{ editingId ? '保存' : '添加' }}</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import EmptyState from '../../components/EmptyState.vue'

const list = ref([])
const showForm = ref(false)
const editingId = ref(null)
const form = reactive({ receiverName:'', phone:'', province:'', city:'', district:'', detailAddress:'', isDefault:0 })

async function loadList() {
  try {
    const token = uni.getStorageSync('token')
    const res = await new Promise((resolve,reject)=>{
      uni.request({ url:'http://localhost:8833/api/address', method:'GET', header:{'Authorization':'Bearer '+token}, success:resolve, fail:reject })
    })
    if (res.data.code===200) list.value = res.data.data || []
  } catch { }
}

function openForm(a) {
  if (a) {
    editingId.value = a.id
    form.receiverName = a.receiverName || ''
    form.phone = a.phone || ''
    form.province = a.province || ''
    form.city = a.city || ''
    form.district = a.district || ''
    form.detailAddress = a.detailAddress || ''
    form.isDefault = a.isDefault || 0
  } else {
    editingId.value = null
    form.receiverName = ''; form.phone = ''; form.province = ''; form.city = ''
    form.district = ''; form.detailAddress = ''; form.isDefault = 0
  }
  showForm.value = true
}

async function saveAddr() {
  if (!form.receiverName || !form.phone || !form.province || !form.city || !form.detailAddress) {
    return uni.showToast({ title:'请填写完整', icon:'none' })
  }
  try {
    const token = uni.getStorageSync('token')
    const method = editingId.value ? 'PUT' : 'POST'
    const url = editingId.value
      ? 'http://localhost:8833/api/address/' + editingId.value
      : 'http://localhost:8833/api/address'
    await new Promise((resolve,reject)=>{
      uni.request({ url, method, data: form, header:{'Authorization':'Bearer '+token}, success:resolve, fail:reject })
    })
    uni.showToast({ title: editingId.value ? '已更新' : '已添加', icon:'success' })
    showForm.value = false
    loadList()
  } catch { uni.showToast({ title:'操作失败', icon:'none' }) }
}

function delAddr(id) {
  uni.showModal({ title:'确认', content:'删除该地址？', success:async r=>{
    if (!r.confirm) return
    try {
      const token = uni.getStorageSync('token')
      await new Promise((resolve,reject)=>{
        uni.request({ url:'http://localhost:8833/api/address/'+id, method:'DELETE', header:{'Authorization':'Bearer '+token}, success:resolve, fail:reject })
      })
      uni.showToast({ title:'已删除', icon:'success' })
      loadList()
    } catch { }
  }})
}

function selectAddr(a) { uni.showToast({ title:'已选择: '+a.receiverName, icon:'none' }) }

onMounted(() => loadList())
</script>

<style scoped lang="scss">
.addr-page { background:#F8FAFC;min-height:100vh;padding:24rpx; }
.addr-card { background:#fff;border-radius:16rpx;padding:24rpx;margin-bottom:16rpx;display:flex;justify-content:space-between;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.ac-left { flex:1; }
.ac-name { font-size:30rpx;font-weight:600;color:#0F172A;display:block;margin-bottom:6rpx; }
.ac-detail { font-size:26rpx;color:#64748B;display:block; }
.ac-right { display:flex;flex-direction:column;align-items:flex-end;gap:8rpx; }
.ac-default { font-size:22rpx;background:#EFF6FF;color:#2563EB;padding:4rpx 12rpx;border-radius:8rpx; }
.ac-edit { font-size:24rpx;color:#2563EB; }
.ac-del { font-size:24rpx;color:#EF4444; }
.addr-add { background:linear-gradient(135deg,#2563EB,#3B82F6);color:#fff;border:none;border-radius:16rpx;padding:24rpx;font-size:30rpx;margin-top:32rpx; }
.addr-mask { position:fixed;inset:0;background:rgba(0,0,0,.4);z-index:100; }
.addr-form { position:fixed;bottom:0;left:0;right:0;background:#fff;border-radius:24rpx 24rpx 0 0;padding:32rpx 24rpx;z-index:101; }
.af-title { font-size:32rpx;font-weight:700;color:#0F172A;text-align:center;display:block;margin-bottom:24rpx; }
.af-input { background:#F1F5F9;border-radius:12rpx;padding:20rpx;font-size:28rpx;margin-bottom:16rpx; }
.af-default { padding:16rpx 0; }
.af-actions { display:flex;gap:16rpx;margin-top:12rpx; }
.af-cancel { flex:1;background:#F1F5F9;color:#475569;border:none;border-radius:12rpx;padding:20rpx;font-size:28rpx; }
.af-save { flex:1;background:linear-gradient(135deg,#2563EB,#3B82F6);color:#fff;border:none;border-radius:12rpx;padding:20rpx;font-size:28rpx; }
</style>
