import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCart, addToCart, updateCartItem, removeCartItem, clearCart as apiClear } from '../api/cart.js'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const loaded = ref(false)

  const totalCount = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
  const totalAmount = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2))
  const checkedItems = computed(() => items.value.filter(i => i.checked))
  const checkedAmount = computed(() => checkedItems.value.reduce((s, i) => s + i.price * i.quantity, 0).toFixed(2))
  const allChecked = computed(() => items.value.length > 0 && items.value.every(i => i.checked))

  async function fetchCart() {
    try {
      const res = await getCart()
      items.value = (res.data || []).map(i => ({ ...i, checked: true }))
      loaded.value = true
    } catch { items.value = [] }
  }

  async function add(productId, quantity = 1) {
    await addToCart(productId, quantity)
    await fetchCart()
  }

  async function update(productId, quantity) {
    if (quantity < 1) return
    await updateCartItem(productId, quantity)
    const idx = items.value.findIndex(i => i.productId === productId)
    if (idx >= 0) items.value[idx].quantity = quantity
  }

  async function remove(productId) {
    await removeCartItem(productId)
    items.value = items.value.filter(i => i.productId !== productId)
  }

  async function clear() {
    await apiClear()
    items.value = []
  }

  function toggleCheck(productId) {
    const idx = items.value.findIndex(i => i.productId === productId)
    if (idx >= 0) items.value[idx].checked = !items.value[idx].checked
  }

  function toggleAll() {
    const checked = !allChecked.value
    items.value.forEach(i => { i.checked = checked })
  }

  return { items, loaded, totalCount, totalAmount, checkedItems, checkedAmount, allChecked, fetchCart, add, update, remove, clear, toggleCheck, toggleAll }
})
