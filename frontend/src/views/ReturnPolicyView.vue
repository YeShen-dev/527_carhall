<template>
  <div class="page">
    <div class="page-hero">
      <el-icon :size="40"><RefreshRight /></el-icon>
      <h1>退换货政策</h1>
      <p>无忧购物 · 售后保障</p>
    </div>

    <div class="content-card">
      <section>
        <h3><el-icon><CircleCheck /></el-icon> 退换货时效</h3>
        <p>自签收商品之日起<strong>7天内</strong>，如商品存在质量问题或与订单不符，可申请退换货。超出7天的订单原则上不予退换，特殊情况请联系客服协商处理。</p>
      </section>

      <el-divider />

      <section>
        <h3><el-icon><CircleCheck /></el-icon> 退换货条件</h3>
        <ul>
          <li>商品存在非人为因素造成的质量问题</li>
          <li>收到的商品与订单描述不符</li>
          <li>商品包装完整、配件齐全，不影响二次销售</li>
          <li>已安装或使用的商品不接受退换货</li>
          <li>定制类商品、电子元件类商品不支持无理由退货</li>
        </ul>
      </section>

      <el-divider />

      <section>
        <h3><el-icon><CircleCheck /></el-icon> 退换货流程</h3>
        <div class="steps">
          <div class="step"><span class="step-num">1</span><span>联系客服提交退换货申请</span></div>
          <div class="step"><span class="step-num">2</span><span>客服确认退换货原因及条件</span></div>
          <div class="step"><span class="step-num">3</span><span>将商品寄回指定地址（请保留运单号）</span></div>
          <div class="step"><span class="step-num">4</span><span>仓库验收商品无误后办理退款或换货</span></div>
        </div>
      </section>

      <el-divider />

      <section>
        <h3><el-icon><CircleCheck /></el-icon> 退款说明</h3>
        <ul>
          <li><strong>退款时效：</strong>商品验收无误后，1-3个工作日内原路退回</li>
          <li><strong>运费承担：</strong>因商品质量问题导致的退换货，运费由本商城承担；因个人原因导致的退换货，运费由买家承担</li>
          <li><strong>退款金额：</strong>退款金额为实际支付金额，不含优惠券及积分抵扣部分</li>
        </ul>
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

.steps { display: flex; flex-direction: column; gap: 12px; }
.step { display: flex; align-items: center; gap: 14px; }
.step-num {
  width: 28px; height: 28px; border-radius: 50%; background: linear-gradient(135deg, #e63946, #ff6b35);
  color: #fff; font-weight: 700; font-size: 13px; display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
</style>
