<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo"><svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="white" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M14.31 8l5.74 9.94H3.95L9.69 8"/></svg></div>
        <h2>欢迎回来</h2>
        <p>登录您的汽修配件商城账号</p>
      </div>

      <!-- Tab switch -->
      <div class="lc-tabs">
        <view class="lct" :class="{active:tab===0}" @click="tab=0">密码登录</view>
        <view class="lct" :class="{active:tab===1}" @click="tab=1">验证码登录</view>
      </div>

      <!-- Password login -->
      <el-form v-if="tab===0" ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password size="large" />
        </el-form-item>
        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="请输入验证码" size="large" />
            <span class="captcha-text" @click="fetchCaptcha">{{ captchaCode }}</span>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" size="large" class="submit-btn">登录</el-button>
        </el-form-item>
      </el-form>

      <!-- SMS login -->
      <div v-if="tab===1" class="sms-login">
        <div class="sms-item"><label>手机号</label><el-input v-model="phone" placeholder="请输入手机号" size="large" maxlength="11" /></div>
        <div class="sms-item"><label>验证码</label><div class="sms-row"><el-input v-model="smsCode" placeholder="请输入验证码" size="large" maxlength="6" style="flex:1" /><el-button :type="cd>0?'info':'primary'" :disabled="cd>0||sending" size="large" @click="sendSms" style="min-width:120px">{{ cd>0?cd+'s':sending?'发送中...':'获取验证码' }}</el-button></div></div>
        <el-button type="primary" :loading="loading2" size="large" class="submit-btn" @click="handleSmsLogin">登录</el-button>
      </div>

      <p class="auth-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
      <p class="auth-hint">测试账号：admin / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user.js'
import { getSessionId } from '../utils/session.js'
import { getCaptcha } from '../api/captcha.js'
import request from '../api/request.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const loading2 = ref(false)
const captchaUuid = ref('')
const captchaCode = ref('')
const tab = ref(0)
const phone = ref('')
const smsCode = ref('')
const cd = ref(0)
const sending = ref(false)
let timer = null

const form = reactive({ username:'', password:'', captchaCode:'' })
const rules = {
  username: [{ required:true, message:'请输入用户名', trigger:'blur' }],
  password: [{ required:true, message:'请输入密码', trigger:'blur' }],
  captchaCode: [{ required:true, message:'请输入验证码', trigger:'blur' }]
}

async function fetchCaptcha() {
  try { const res = await getCaptcha(); captchaUuid.value = res.data.uuid; captchaCode.value = res.data.code } catch { }
}

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login(form.username, form.password, getSessionId(), captchaUuid.value, form.captchaCode)
    router.push(route.query.redirect || '/')
  } catch { fetchCaptcha() } finally { loading.value = false }
}

function sendSms() {
  if (sending.value) return
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return ElMessage.warning('请输入正确的手机号')
  sending.value = true
  request.post('/auth/sendCode', { phone: phone.value }).then(() => {
    ElMessage.success('验证码已发送')
    cd.value = 60; timer = setInterval(() => { cd.value--; if(cd.value<=0) clearInterval(timer) }, 1000)
  }).catch(e => ElMessage.error(e?.response?.data?.message||'发送失败'))
  .finally(() => { sending.value = false })
}

async function handleSmsLogin() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return ElMessage.warning('请输入正确的手机号')
  if (!smsCode.value) return ElMessage.warning('请输入验证码')
  loading2.value = true
  try {
    await userStore.loginByPhone(phone.value, smsCode.value)
    router.push(route.query.redirect || '/')
  } catch { } finally { loading2.value = false }
}

onMounted(() => fetchCaptcha())
</script>

<style scoped>
.auth-page { min-height:calc(100vh - 160px); display:flex; align-items:center; justify-content:center; padding:24px; }
.auth-card { width:440px; background:#fff; border:1px solid #F1F5F9; border-radius:24px; padding:40px 36px; box-shadow:0 4px 24px rgba(0,0,0,.06); }
.auth-header { text-align:center; margin-bottom:24px; }
.auth-logo { width:60px;height:60px;background:linear-gradient(135deg,#2563EB,#3B82F6);border-radius:16px;display:flex;align-items:center;justify-content:center;margin:0 auto 16px;box-shadow:0 8px 24px rgba(37,99,235,.25); }
.auth-header h2 { font-size:22px;font-weight:700;color:#0F172A;margin-bottom:6px; }
.auth-header p { font-size:13px;color:#94A3B8; }
.lc-tabs { display:flex;background:#F1F5F9;border-radius:14px;padding:6px;margin-bottom:24px; }
.lct { flex:1;text-align:center;padding:14px;font-size:14px;color:#6B7280;border-radius:10px;cursor:pointer;transition:all .2s; }
.lct.active { background:#fff;color:#2563EB;font-weight:700;box-shadow:0 2px 8px rgba(0,0,0,.06); }
.submit-btn { width:100%;font-weight:700;height:44px;border-radius:12px;font-size:15px; }
.auth-link { text-align:center;font-size:13px;color:#94A3B8;margin-top:20px; }
.auth-link a { color:#2563EB;font-weight:600; }
.auth-hint { text-align:center;font-size:11px;color:#CBD5E1;margin-top:10px; }
.captcha-row { display:flex;gap:10px;align-items:center; }
.captcha-text { font-size:20px;font-weight:700;color:#2563EB;background:#EFF6FF;border:1px solid #DBEAFE;padding:6px 14px;border-radius:10px;cursor:pointer;letter-spacing:3px;user-select:none;white-space:nowrap;flex-shrink:0; }
.sms-login { display:flex;flex-direction:column;gap:16px; }
.sms-item label { display:block;font-size:13px;font-weight:600;color:#374151;margin-bottom:6px; }
.sms-row { display:flex;gap:12px;align-items:center; }
</style>
