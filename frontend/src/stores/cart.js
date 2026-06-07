import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  listCart as apiListCart,
  addToCart as apiAddToCart,
  updateCartItem as apiUpdateCartItem,
  removeCartItem as apiRemoveCartItem,
  clearCart as apiClearCart
} from '../api/cart.js'
import {
  listCartTemp,
  addToCartTemp,
  updateCartItemTemp,
  removeCartItemTemp,
  clearCartTemp
} from '../api/cart-temp.js'
import { useUserStore } from './user.js'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loading = ref(false)

  const totalCount = computed(() => items.value.reduce((sum, i) => sum + i.quantity, 0))
  const totalAmount = computed(() =>
    items.value.reduce((sum, i) => sum + i.price * i.quantity, 0).toFixed(2)
  )

  function isLoggedIn() {
    const userStore = useUserStore()
    return userStore.isLoggedIn
  }

  async function fetchCart() {
    loading.value = true
    try {
      const res = isLoggedIn() ? await apiListCart() : await listCartTemp()
      items.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function add(productId, quantity = 1) {
    if (isLoggedIn()) {
      await apiAddToCart(productId, quantity)
    } else {
      await addToCartTemp(productId, quantity)
    }
    await fetchCart()
  }

  async function update(productId, quantity) {
    if (isLoggedIn()) {
      await apiUpdateCartItem(productId, quantity)
    } else {
      await updateCartItemTemp(productId, quantity)
    }
    await fetchCart()
  }

  async function remove(productId) {
    if (isLoggedIn()) {
      await apiRemoveCartItem(productId)
    } else {
      await removeCartItemTemp(productId)
    }
    await fetchCart()
  }

  async function clear() {
    if (isLoggedIn()) {
      await apiClearCart()
    } else {
      await clearCartTemp()
    }
    items.value = []
  }

  return { items, loading, totalCount, totalAmount, fetchCart, add, update, remove, clear }
})
