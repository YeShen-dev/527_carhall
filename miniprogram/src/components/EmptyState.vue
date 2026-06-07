<template>
  <view class="empty-state">
    <text class="empty-icon">{{ icon }}</text>
    <text class="empty-text">{{ text }}</text>
    <slot />
  </view>
</template>
<script setup>
defineProps({ icon:{type:String,default:'📭'}, text:{type:String,default:'暂无数据'} })
</script>
<style scoped>
.empty-state { display:flex;flex-direction:column;align-items:center;padding:80rpx 0; }
.empty-icon { font-size:80rpx; }
.empty-text { font-size:28rpx;color:#94A3B8;margin-top:12rpx; }
</style>
