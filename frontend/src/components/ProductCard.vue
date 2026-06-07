<template>
  <div class="product-card" @click="$emit('click')">
    <div class="card-img-wrap">
      <el-image v-if="product.imageUrl" :src="product.imageUrl" fit="cover" class="card-img">
        <template #error>
          <div class="img-placeholder"><el-icon :size="40"><PictureFilled /></el-icon></div>
        </template>
      </el-image>
      <div v-else class="img-placeholder"><el-icon :size="40"><PictureFilled /></el-icon></div>

      <!-- Badges -->
      <span v-if="product.stock <= 0" class="badge sold-out">售罄</span>
      <span v-else-if="product.stock < 20" class="badge low-stock">仅剩{{ product.stock }}件</span>
      <span v-if="product.stock > 0 && product.originalPrice" class="badge discount">-{{ discountPercent }}%</span>

      <!-- Favorite heart -->
      <button class="fav-btn" :class="{ liked: fav }" @click.stop="fav = !fav">
        <svg viewBox="0 0 24 24" width="18" height="18" :fill="fav ? '#EF4444' : 'none'" :stroke="fav ? '#EF4444' : '#94A3B8'" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
      </button>
    </div>

    <div class="card-body">
      <div class="card-tags">
        <span class="tag-cat">{{ product.category }}</span>
        <span class="tag-brand">{{ product.brand }}</span>
      </div>
      <h3 class="card-name">{{ product.name }}</h3>
      <p class="card-spec" v-if="product.spec">{{ product.spec }}</p>
      <div class="card-footer">
        <div class="price-area">
          <span class="price-currency">¥</span>
          <span class="price-value">{{ product.price }}</span>
        </div>
        <button class="cart-btn" :disabled="product.stock <= 0" @click.stop="$emit('add-to-cart', product)">
          <el-icon :size="18"><ShoppingCart /></el-icon>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ product: { type: Object, required: true } })
defineEmits(['click', 'add-to-cart'])

const fav = ref(false)
const discountPercent = computed(() => {
  if (!props.product.originalPrice || props.product.originalPrice <= props.product.price) return 0
  return Math.round((1 - props.product.price / props.product.originalPrice) * 100)
})
</script>

<style scoped>
.product-card {
  background: #fff; border-radius: 16px; overflow: hidden;
  cursor: pointer; transition: all 0.3s cubic-bezier(0.4,0,0.2,1);
  border: 1px solid #F1F5F9;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  border-color: #DBEAFE;
}

/* Image */
.card-img-wrap { position: relative; overflow: hidden; background: #F8FAFC; }
.card-img { width: 100%; aspect-ratio: 1; display: block; transition: transform 0.5s cubic-bezier(0.4,0,0.2,1); }
.product-card:hover .card-img { transform: scale(1.08); }
.img-placeholder {
  width: 100%; aspect-ratio: 1; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg,#F1F5F9,#E2E8F0); color: #CBD5E1;
}

/* Badges */
.badge {
  position: absolute; top: 10px; right: 10px; font-size: 11px; font-weight: 700;
  padding: 4px 10px; border-radius: 8px; backdrop-filter: blur(4px);
}
.sold-out { background: rgba(0,0,0,0.75); color: #FCA5A5; }
.low-stock { background: rgba(245,158,11,0.9); color: #fff; }
.discount { top: auto; bottom: 10px; right: 10px; background: rgba(239,68,68,0.9); color: #fff; }

/* Favorite */
.fav-btn {
  position: absolute; top: 8px; left: 8px; width: 32px; height: 32px;
  border: none; background: rgba(255,255,255,0.85); backdrop-filter: blur(4px);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: transform 0.2s; box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.fav-btn:active { transform: scale(1.2); }
.fav-btn.liked { background: #FEE2E2; }

/* Body */
.card-body { padding: 14px 14px 16px; }
.card-tags { display: flex; gap: 6px; margin-bottom: 8px; flex-wrap: wrap; }
.tag-cat { font-size: 10px; font-weight: 600; padding: 3px 8px; border-radius: 6px; background: #EFF6FF; color: #2563EB; }
.tag-brand { font-size: 10px; font-weight: 500; padding: 3px 8px; border-radius: 6px; background: #F1F5F9; color: #64748B; }
.card-name { font-size: 14px; font-weight: 600; line-height: 1.4; color: #0F172A; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-spec { font-size: 12px; color: #94A3B8; margin-bottom: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* Footer */
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.price-area { display: flex; align-items: baseline; gap: 2px; }
.price-currency { font-size: 14px; font-weight: 700; color: #0F172A; }
.price-value { font-size: 22px; font-weight: 800; color: #0F172A; line-height: 1; }

.cart-btn {
  width: 36px; height: 36px; border-radius: 10px;
  background: #EFF6FF; border: none; color: #2563EB;
  cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.cart-btn:hover:not(:disabled) { background: #2563EB; color: #fff; transform: scale(1.1); }
.cart-btn:active:not(:disabled) { transform: scale(0.95); }
.cart-btn:disabled { background: #F1F5F9; color: #CBD5E1; cursor: not-allowed; }
</style>
