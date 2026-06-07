<template>
  <el-drawer
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="购物车"
    size="420px"
    direction="rtl"
  >
    <div v-if="cartStore.items.length === 0" class="empty-cart">
      <el-empty description="购物车是空的" />
      <el-button type="primary" @click="$emit('update:modelValue', false)">去逛逛</el-button>
    </div>

    <div v-else class="cart-content">
      <div class="cart-items">
        <div v-for="item in cartStore.items" :key="item.productId" class="cart-item">
          <el-image
            v-if="item.productImage"
            :src="item.productImage"
            fit="cover"
            style="width:64px;height:64px;border-radius:8px"
          />
          <div class="item-body">
            <span class="item-name">{{ item.productName }}</span>
            <span class="item-price">¥{{ item.price }}</span>
            <div class="item-actions">
              <el-input-number
                :model-value="item.quantity"
                :min="1"
                :max="item.stock"
                size="small"
                controls-position="right"
                @change="(val) => handleQuantity(item.productId, val)"
              />
              <el-button text type="danger" size="small" @click="handleRemove(item.productId)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="cart-footer">
        <div class="cart-summary">
          <span>共计 {{ cartStore.totalCount }} 件</span>
          <span class="cart-total">¥{{ cartStore.totalAmount }}</span>
        </div>
        <div class="cart-actions">
          <el-button @click="handleClear">清空购物车</el-button>
          <el-button type="primary" size="large" @click="handleCheckout">去结算</el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart.js'
import { useUserStore } from '../stores/user.js'
import { placeOrder } from '../api/order.js'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

async function handleQuantity(productId, val) {
  if (val) await cartStore.update(productId, val)
}

async function handleRemove(productId) {
  await cartStore.remove(productId)
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确认清空购物车？', '提示', { type: 'warning' })
    await cartStore.clear()
    ElMessage.success('已清空')
  } catch { /* cancelled */ }
}

async function handleCheckout() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再结算')
    router.push('/login')
    emit('update:modelValue', false)
    return
  }
  try {
    await ElMessageBox.confirm('确认提交订单？', '结算确认', { type: 'info' })
    const res = await placeOrder()
    await cartStore.fetchCart()
    ElMessage.success('订单已生成，请选择支付方式')
    emit('update:modelValue', false)
    router.push(`/payment/${res.data.id}`)
  } catch { /* cancelled or error */ }
}
</script>

<style scoped>
.cart-content { display: flex; flex-direction: column; height: 100%; }
.cart-items { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; padding-bottom: 16px; }
.cart-item {
  display: flex; gap: 12px; background: #F8FAFC;
  border-radius: 14px; padding: 12px; border: 1px solid #F1F5F9;
}
.item-body { flex: 1; display: flex; flex-direction: column; gap: 4px; min-width: 0; }
.item-name { font-weight: 500; color: #0F172A; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-price { font-weight: 600; color: #2563EB; }
.item-actions { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.empty-cart { display: flex; flex-direction: column; align-items: center; gap: 16px; padding-top: 60px; }
.cart-footer { border-top: 1px solid #E2E8F0; padding-top: 16px; }
.cart-summary { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; font-size: 14px; color: #64748B; }
.cart-total { font-size: 22px; font-weight: 700; color: #0F172A; }
.cart-actions { display: flex; gap: 10px; }
.cart-actions .el-button:last-child { flex: 1; }
</style>
