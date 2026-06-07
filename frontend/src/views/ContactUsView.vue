<template>
  <div class="contact-page">
    <div class="ch">
      <span class="ch-icon">📞</span>
      <h2>联系我们</h2>
      <p>专业咨询 · 贴心服务</p>
    </div>

    <div class="cg">
      <div class="cc"><span class="ci">📞</span><strong>客服热线</strong><span class="cv">18029538913</span><span class="cd">周一至周日 9:00-21:00</span></div>
      <div class="cc"><span class="ci">📧</span><strong>电子邮箱</strong><span class="cv">chouj357@gmail.com</span><span class="cd">工作日4小时内回复</span></div>
      <div class="cc"><span class="ci">💬</span><strong>微信客服</strong><span class="cv">AutoParts-Mall</span><span class="cd">扫码添加企业微信</span></div>
      <div class="cc"><span class="ci">📍</span><strong>公司地址</strong><span class="cv">广州运营中心</span><span class="cd">广东省广州市白云区汽配产业园A座3层</span></div>
    </div>

    <div class="cc-form">
      <h3><el-icon><Edit /></el-icon> 在线留言</h3>
      <el-form :model="msg" label-width="70px">
        <el-form-item label="联系人"><el-input v-model="msg.name" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="msg.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="留言内容"><el-input v-model="msg.content" type="textarea" :rows="4" placeholder="请描述您的问题或需求" /></el-form-item>
        <el-form-item><el-button type="primary" @click="submitMsg">提交留言</el-button></el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
const msg = reactive({ name:'', phone:'', content:'' })
function submitMsg(){ if(!msg.name||!msg.phone||!msg.content) return ElMessage.warning('请填写所有必填项'); ElMessage.success('留言已提交，客服将在24小时内与您联系'); msg.name=''; msg.phone=''; msg.content='' }
</script>

<style scoped>
.contact-page { max-width:800px; margin:0 auto; }
.ch { text-align:center; padding:40px 20px; background:linear-gradient(135deg,#1E3A8A,#2563EB); border-radius:20px; color:#fff; margin-bottom:24px; }
.ch h2 { font-size:28px; font-weight:800; margin:12px 0 6px; }
.ch p { color:rgba(255,255,255,.7); font-size:15px; }
.ch-icon { font-size:40px; }
.cg { display:grid; grid-template-columns:repeat(2,1fr); gap:14px; margin-bottom:24px; }
.cc { background:#fff; border:1px solid #F1F5F9; border-radius:14px; padding:22px; display:flex; flex-direction:column; gap:6px; transition:all .2s; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.cc:hover { box-shadow:0 8px 24px rgba(0,0,0,.08); transform:translateY(-2px); }
.ci { font-size:28px; }
.cc strong { color:#0F172A; font-size:15px; }
.cv { color:#2563EB; font-weight:700; font-size:17px; }
.cd { color:#94A3B8; font-size:12px; }
.cc-form { background:#fff; border:1px solid #F1F5F9; border-radius:16px; padding:28px; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.cc-form h3 { display:flex; align-items:center; gap:8px; font-size:16px; font-weight:700; color:#0F172A; margin-bottom:20px; }
@media (max-width:600px) { .cg { grid-template-columns:1fr; } }
</style>
