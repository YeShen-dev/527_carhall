<template>
  <div class="page">
    <div class="page-hero">
      <el-icon :size="40"><QuestionFilled /></el-icon>
      <h1>帮助中心</h1>
      <p>常见问题与使用指南</p>
    </div>

    <div class="faq-list">
      <div v-for="(item, i) in faqs" :key="i" class="faq-card">
        <div class="faq-q" @click="item.open = !item.open">
          <span>{{ i + 1 }}. {{ item.q }}</span>
          <el-icon><ArrowDown v-if="!item.open" /><ArrowUp v-else /></el-icon>
        </div>
        <div v-show="item.open" class="faq-a">{{ item.a }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const faqs = reactive([
  { q: '如何注册账号？', a: '点击页面右上角「登录 / 注册」按钮，填写用户名、密码、手机号、邮箱和验证码即可完成注册。', open: false },
  { q: '如何搜索配件？', a: '您可以通过首页顶部搜索栏输入配件名称、品牌或车型关键词进行搜索，也可以点击下方分类和品牌快速筛选。', open: false },
  { q: '如何下单购买？', a: '找到所需商品后，点击进入详情页，选择购买数量，点击「加入购物车」，然后在购物车中点击「去结算」提交订单。', open: false },
  { q: '支持哪些支付方式？', a: '目前支持在线支付，订单提交后即视为确认购买，请确保账户信息准确。', open: false },
  { q: '如何查看订单状态？', a: '登录后点击导航栏「我的订单」，可查看所有订单及其状态（待处理、已发货、已完成、已取消）。', open: false },
  { q: '收到商品有质量问题怎么办？', a: '请查看「退换货政策」页面了解详细流程，或直接联系客服 400-888-9999 处理。', open: false },
  { q: '如何修改个人信息？', a: '登录后点击右上角用户名进入「个人中心」，可修改手机号、邮箱等信息，也可修改登录密码。', open: false },
  { q: '忘记密码怎么办？', a: '目前请联系客服 400-888-9999 协助重置密码，我们后续将上线自助找回密码功能。', open: false }
])
</script>

<style scoped>
.page { max-width: 800px; margin: 0 auto; }
.page-hero {
  text-align: center; padding: 40px 20px; margin-bottom: 30px;
  background: linear-gradient(135deg, #1c1c2c, #242438);
  border-radius: 20px; border: 1px solid #2a2a3e;
}
.page-hero h1 { font-size: 28px; font-weight: 800; margin: 12px 0 6px; }
.page-hero p { color: #9898a8; font-size: 15px; }
.page-hero .el-icon { color: #e63946; }

.faq-list { display: flex; flex-direction: column; gap: 10px; }
.faq-card {
  background: #1c1c2c; border: 1px solid #2a2a3e; border-radius: 14px; overflow: hidden;
  transition: border-color 0.2s;
}
.faq-card:hover { border-color: #e63946; }
.faq-q {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; cursor: pointer; font-weight: 600;
  font-size: 14px; user-select: none;
}
.faq-q .el-icon { color: #e63946; flex-shrink: 0; margin-left: 12px; }
.faq-a {
  padding: 0 20px 18px 20px; color: #9898a8; font-size: 13px; line-height: 1.8;
  border-top: 1px solid #2a2a3e; margin: 0 20px; padding-top: 14px;
}
</style>
