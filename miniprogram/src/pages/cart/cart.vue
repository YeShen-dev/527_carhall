<template>
  <view class="cart-page">
    <!-- Login prompt -->
    <view v-if="!userStore.isLoggedIn" class="cart-login-hint">
      <text class="hint-text">登录后查看购物车</text>
      <button class="hint-btn" @click="goLogin">去登录</button>
    </view>

    <!-- Empty cart -->
    <view v-else-if="!loading && cartStore.items.length === 0" class="empty-cart">
      <text class="empty-icon">🛒</text>
      <text class="empty-text">购物车是空的</text>
      <button class="empty-btn" @click="goHome">去逛逛</button>
    </view>

    <!-- Cart list -->
    <template v-else>
      <scroll-view scroll-y class="cart-list">
        <view v-for="item in cartStore.items" :key="item.productId" class="cart-item">
          <view class="ci-check" @click="cartStore.toggleCheck(item.productId)">
            <text :style="{color:item.checked?'#2563EB':'#CBD5E1',fontSize:'40rpx'}">{{ item.checked ? '✅' : '⭕' }}</text>
          </view>
          <image v-if="item.productImage" :src="item.productImage" mode="aspectFill" class="ci-img" />
          <view v-else class="ci-img-fb"><text>🛒</text></view>
          <view class="ci-info">
            <text class="ci-name">{{ item.productName }}</text>
            <text class="ci-price">¥{{ item.price }}</text>
            <view class="ci-qty">
              <view class="qty-btn" @click="handleQty(item.productId, item.quantity-1)"><text>-</text></view>
              <text class="qty-val">{{ item.quantity }}</text>
              <view class="qty-btn" @click="handleQty(item.productId, item.quantity+1)"><text>+</text></view>
            </view>
          </view>
          <view class="ci-del" @click="cartStore.remove(item.productId)"><text>🗑️</text></view>
        </view>
      </scroll-view>

      <!-- Bottom bar -->
      <view class="cart-bottom">
        <view class="cb-left" @click="cartStore.toggleAll()">
          <text :style="{color:cartStore.allChecked?'#2563EB':'#CBD5E1',fontSize:'36rpx'}">{{ cartStore.allChecked ? '✅' : '⭕' }}</text>
          <text>全选</text>
        </view>
        <view class="cb-info">
          <text>合计: </text><text class="cb-total">¥{{ cartStore.checkedAmount }}</text>
        </view>
        <button class="cb-btn" :disabled="cartStore.checkedItems.length===0" @click="handleCheckout">
          结算({{ cartStore.checkedItems.length }})
        </button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useCartStore } from '../../store/cart.js'
import { useUserStore } from '../../store/user.js'
import { placeOrder } from '../../api/cart.js'

const cartStore = useCartStore()
const userStore = useUserStore()
const loading = ref(true)

async function loadCart() {
  if (!userStore.isLoggedIn) { loading.value = false; return }
  await cartStore.fetchCart()
  loading.value = false
}

function handleQty(pid, qty) { if (qty < 1) return cartStore.remove(pid); cartStore.update(pid, qty) }

async function handleCheckout() {
  try {
    await uni.showModal({ title:'结算确认', content:'确认提交订单？' })
    const res = await placeOrder()
    await cartStore.fetchCart()
    uni.navigateTo({ url: `/pages/payment/pay?orderId=${res.data.id}&amount=${res.data.totalAmount}` })
  } catch { /* cancelled */ }
}

function goLogin() { uni.navigateTo({ url: '/pages/login/login' }) }
function goHome() { uni.switchTab({ url: '/pages/index/index' }) }

onMounted(() => loadCart())
</script>

<style scoped lang="scss">
.cart-page { background: #F8FAFC; min-height: 100vh; display: flex; flex-direction: column; }
.cart-login-hint { display:flex;flex-direction:column;align-items:center;padding:100rpx 0;gap:24rpx; }
.hint-text { font-size:30rpx;color:#64748B; }
.hint-btn { background:#2563EB;color:#fff;border:none;border-radius:40rpx;padding:16rpx 48rpx;font-size:28rpx; }
.empty-cart { display:flex;flex-direction:column;align-items:center;padding:120rpx 0;gap:16rpx; }
.empty-icon { font-size:80rpx; } .empty-text { font-size:28rpx;color:#94A3B8; }
.empty-btn { background:#2563EB;color:#fff;border:none;border-radius:40rpx;padding:16rpx 48rpx;font-size:28rpx;margin-top:16rpx; }
.cart-list { flex:1; padding:16rpx 24rpx; }
.cart-item { display:flex;align-items:center;gap:16rpx;background:#fff;border-radius:16rpx;padding:16rpx;margin-bottom:12rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.ci-check { width:48rpx;display:flex;align-items:center;justify-content:center; }
.ci-img { width:120rpx;height:120rpx;border-radius:12rpx; }
.ci-img-fb { width:120rpx;height:120rpx;border-radius:12rpx;background:#F1F5F9;display:flex;align-items:center;justify-content:center;font-size:48rpx; }
.ci-info { flex:1;display:flex;flex-direction:column;gap:8rpx; }
.ci-name { font-size:28rpx;font-weight:600;color:#0F172A;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
.ci-price { font-size:32rpx;font-weight:700;color:#EF4444; }
.ci-qty { display:flex;align-items:center;gap:12rpx; }
.qty-btn { width:44rpx;height:44rpx;border-radius:8rpx;background:#F1F5F9;display:flex;align-items:center;justify-content:center;font-size:28rpx;font-weight:700;color:#475569; }
.qty-val { font-size:28rpx;font-weight:600;min-width:40rpx;text-align:center; }
.ci-del { width:48rpx;display:flex;align-items:center;justify-content:center; }
.cart-bottom { background:rgba(255,255,255,.95);backdrop-filter:blur(20rpx);padding:16rpx 24rpx;display:flex;align-items:center;gap:16rpx;border-top:1rpx solid #F1F5F9;padding-bottom:calc(16rpx + env(safe-area-inset-bottom)); }
.cb-left { display:flex;align-items:center;gap:6rpx;font-size:26rpx;color:#475569; }
.cb-info { flex:1;font-size:26rpx;color:#475569; }
.cb-total { font-size:36rpx;font-weight:800;color:#EF4444; }
.cb-btn { background:linear-gradient(135deg,#2563EB,#3B82F6);color:#fff;border:none;border-radius:40rpx;padding:16rpx 32rpx;font-size:28rpx;font-weight:600; }
.cb-btn[disabled] { background:#CBD5E1; }
</style>
