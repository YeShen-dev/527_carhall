<template>
  <view class="product-card" @click="$emit('click')">
    <view class="pc-img-wrap">
      <image v-if="product.imageUrl" :src="product.imageUrl" mode="aspectFill" class="pc-img" lazy-load />
      <view v-else class="pc-img-fb"><text>📦</text></view>
      <view v-if="product.stock<=0" class="pc-badge sold">售罄</view>
      <view v-else-if="product.stock<20" class="pc-badge low">仅剩{{ product.stock }}件</view>
    </view>
    <view class="pc-body">
      <text class="pc-name">{{ product.name }}</text>
      <view class="pc-footer">
        <view class="pc-price-wrap"><text class="pc-price-yen">¥</text><text class="pc-price">{{ product.price }}</text></view>
        <view class="pc-cart-btn" @click.stop="$emit('add', product)"><text>🛒</text></view>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({ product:{ type:Object, required:true } })
defineEmits(['click','add'])
</script>

<style scoped lang="scss">
.product-card { background:#fff;border-radius:16rpx;overflow:hidden;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.pc-img-wrap { position:relative; }
.pc-img { width:100%;aspect-ratio:1;display:block; }
.pc-img-fb { width:100%;aspect-ratio:1;background:#F1F5F9;display:flex;align-items:center;justify-content:center;font-size:56rpx; }
.pc-badge { position:absolute;top:8rpx;right:8rpx;font-size:20rpx;font-weight:600;padding:4rpx 12rpx;border-radius:8rpx; }
.pc-badge.sold { background:rgba(0,0,0,.7);color:#FCA5A5; }
.pc-badge.low { background:rgba(245,158,11,.9);color:#fff; }
.pc-body { padding:14rpx 14rpx 18rpx; }
.pc-name { font-size:26rpx;font-weight:600;color:#0F172A;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;line-height:1.3;margin-bottom:10rpx; }
.pc-footer { display:flex;justify-content:space-between;align-items:center; }
.pc-price-wrap { display:flex;align-items:baseline;gap:2rpx; }
.pc-price-yen { font-size:22rpx;font-weight:700;color:#0F172A; }
.pc-price { font-size:36rpx;font-weight:800;color:#0F172A;line-height:1; }
.pc-cart-btn { width:52rpx;height:52rpx;border-radius:12rpx;background:#EFF6FF;display:flex;align-items:center;justify-content:center;font-size:28rpx; }
</style>
