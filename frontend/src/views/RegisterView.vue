<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo"><svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="white" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M14.31 8l5.74 9.94H3.95L9.69 8"/></svg></div>
        <h2>创建账号</h2>
        <p>加入汽修配件商城，享受优质服务</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" maxlength="11" />
        </el-form-item>
        <!-- SMS code -->
        <el-form-item label="短信验证码" prop="smsCode">
          <div class="code-row">
            <el-input v-model="form.smsCode" placeholder="请输入验证码" size="large" maxlength="6" style="flex:1" />
            <el-button :type="cd>0?'info':'primary'" :disabled="cd>0||sending" size="large" @click="sendSms" class="code-btn">
              {{ cd>0 ? cd+'s' : sending ? '发送中...' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（至少6位）" show-password size="large" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" size="large" class="submit-btn">注册</el-button>
        </el-form-item>
      </el-form>
      <p class="auth-link">已有账号？<router-link to="/login">立即登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user.js'
import request from '../api/request.js'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const sending = ref(false)
const cd = ref(0)
let timer = null

const form = reactive({ username:'', phone:'', smsCode:'', password:'', confirmPassword:'' })
const rules = {
  username: [{ required:true, message:'请输入用户名', trigger:'blur' }],
  phone: [{ required:true, pattern:/^1[3-9]\d{9}$/, message:'请输入正确的手机号', trigger:'blur' }],
  smsCode: [{ required:true, message:'请输入验证码', trigger:'blur' }],
  password: [{ required:true, min:6, message:'密码至少6位', trigger:'blur' }],
  confirmPassword: [{ required:true, validator:(r,v,cb)=>{ if(v!==form.password) cb(new Error('两次密码不一致')); else cb() }, trigger:'blur' }]
}

async function sendSms() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) return ElMessage.warning('请输入正确的手机号')
  sending.value = true
  try {
    await request.post('/auth/sendCode', { phone: form.phone })
    ElMessage.success('验证码已发送')
    cd.value = 60
    timer = setInterval(() => { if(--cd.value<=0) clearInterval(timer) }, 1000)
  } catch (e) { ElMessage.error(e?.response?.data?.message || '发送失败') }
  finally { sending.value = false }
}

async function handleRegister() {
  loading.value = true
  try {
    // step1: 验证短信验证码
    await request.post('/auth/registerByPhone', { phone:form.phone, smsCode:form.smsCode, password:form.password })
    // step2: 注册 (clientType=miniapp 跳过图片验证码)
    await userStore.register({ username:form.username, password:form.password, phone:form.phone, clientType:'miniapp' })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) { ElMessage.error(e?.response?.data?.message || '注册失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { min-height:calc(100vh - 160px); display:flex; align-items:center; justify-content:center; padding:24px; }
.auth-card { width:440px; background:#fff; border:1px solid #F1F5F9; border-radius:24px; padding:40px 36px; box-shadow:0 4px 24px rgba(0,0,0,.06); }
.auth-header { text-align:center; margin-bottom:28px; }
.auth-logo { width:60px;height:60px;background:linear-gradient(135deg,#2563EB,#3B82F6);border-radius:16px;display:flex;align-items:center;justify-content:center;margin:0 auto 16px;box-shadow:0 8px 24px rgba(37,99,235,.25); }
.auth-header h2 { font-size:22px;font-weight:700;color:#0F172A;margin-bottom:6px; }
.auth-header p { font-size:13px;color:#94A3B8; }
.submit-btn { width:100%;font-weight:700;height:44px;border-radius:12px;font-size:15px; }
.auth-link { text-align:center;font-size:13px;color:#94A3B8;margin-top:8px; }
.auth-link a { color:#2563EB;font-weight:600; }
.code-row { display:flex;gap:12px;align-items:center; }
.code-btn { min-width:120px;white-space:nowrap; }
</style>
