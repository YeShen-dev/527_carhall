<template>
  <view class="skeleton-comp" :style="{ width, height, borderRadius }" />
</template>
<script setup>
defineProps({ width:{type:String,default:'100%'}, height:{type:String,default:'32rpx'}, borderRadius:{type:String,default:'8rpx'} })
</script>
<style scoped>
.skeleton-comp {
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}
@keyframes shimmer { 0%{background-position:200% 0} 100%{background-position:-200% 0} }
</style>
