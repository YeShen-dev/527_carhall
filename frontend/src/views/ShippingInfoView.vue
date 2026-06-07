<template>
  <div class="page">
    <div class="page-hero">
      <el-icon :size="40"><Van /></el-icon>
      <h1>配送说明</h1>
      <p>全国配送 · 极速送达</p>
    </div>

    <div class="content-card">
      <section>
        <h3><el-icon><CircleCheck /></el-icon> 配送范围</h3>
        <p>本商城支持<strong>全国配送</strong>（不含港澳台地区），覆盖县级以上城市。偏远地区（新疆、西藏、青海等）配送时效可能延长，具体请咨询客服。</p>
      </section>

      <el-divider />

      <section>
        <h3><el-icon><CircleCheck /></el-icon> 配送方式</h3>
        <div class="delivery-grid">
          <div class="delivery-card">
            <span class="d-icon">📦</span>
            <strong>普通快递</strong>
            <span class="d-desc">中通/圆通/韵达</span>
            <span class="d-time">3-7个工作日</span>
          </div>
          <div class="delivery-card">
            <span class="d-icon">🚚</span>
            <strong>加急物流</strong>
            <span class="d-desc">顺丰速运</span>
            <span class="d-time">1-3个工作日</span>
          </div>
          <div class="delivery-card">
            <span class="d-icon">✈️</span>
            <strong>特快专递</strong>
            <span class="d-desc">EMS/航空件</span>
            <span class="d-time">1-2个工作日</span>
          </div>
        </div>
      </section>

      <el-divider />

      <section>
        <h3><el-icon><CircleCheck /></el-icon> 运费说明</h3>
        <ul>
          <li><strong>订单满¥299包邮</strong> — 普通快递免费配送</li>
          <li>订单不满¥299，普通快递运费¥10起</li>
          <li>加急物流：¥20起，按实际包裹重量计费</li>
          <li>特快专递：¥30起，按实际包裹重量计费</li>
          <li>大件商品（如轮胎、保险杠）运费另计，下单前请咨询客服</li>
        </ul>
      </section>

      <el-divider />

      <section>
        <h3><el-icon><CircleCheck /></el-icon> 发货时效</h3>
        <ul>
          <li><strong>工作日：</strong>当日16:00前下单，当天发货；16:00后下单，次日发货</li>
          <li><strong>周末及节假日：</strong>订单顺延至下一个工作日发货</li>
          <li><strong>预售商品：</strong>按照商品页面标注的发货日期发货</li>
        </ul>
      </section>

      <el-divider />

      <section>
        <h3><el-icon><CircleCheck /></el-icon> 订单追踪</h3>
        <p>发货后您将收到短信通知（含运单号），也可登录商城在「我的订单」中查看物流信息。</p>
      </section>
    </div>
  </div>
</template>

<script setup>
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

.content-card {
  background: #1c1c2c; border: 1px solid #2a2a3e; border-radius: 16px;
  padding: 30px 32px; line-height: 1.9; font-size: 14px; color: #c0c0d0;
}
.content-card h3 { font-size: 16px; font-weight: 700; margin-bottom: 10px; display: flex; align-items: center; gap: 8px; color: #f0f0f5; }
.content-card h3 .el-icon { color: #2ec4a0; }
.content-card strong { color: #ff6b35; }
.content-card ul { padding-left: 20px; }
.content-card li { margin-bottom: 6px; }

.delivery-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.delivery-card {
  background: #141420; border: 1px solid #2a2a3e; border-radius: 12px;
  padding: 20px 14px; text-align: center; display: flex; flex-direction: column; gap: 6px;
  transition: border-color 0.2s;
}
.delivery-card:hover { border-color: #e63946; }
.d-icon { font-size: 32px; }
.delivery-card strong { color: #f0f0f5; }
.d-desc { color: #9898a8; font-size: 12px; }
.d-time { color: #ff6b35; font-size: 13px; font-weight: 600; }

@media (max-width: 600px) { .delivery-grid { grid-template-columns: 1fr; } }
</style>
