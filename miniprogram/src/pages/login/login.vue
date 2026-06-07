<template>
  <view class="login-page">
    <view class="login-card">
      <view class="lc-logo"><view class="lcl-icon"><text>🔧</text></view><text class="lcl-title">汽修配件商城</text><text class="lcl-sub">专业汽车配件在线商城</text></view>

      <!-- Tab switch -->
      <view class="lc-tabs">
        <view class="lct" :class="{active:tab===0}" @click="tab=0">密码登录</view>
        <view class="lct" :class="{active:tab===1}" @click="tab=1">验证码登录</view>
      </view>

      <!-- Password Login -->
      <view v-if="tab===0" class="lc-form">
        <view class="lcf-item"><text class="lcf-label">用户名</text><input v-model="username" class="lcf-input" placeholder="请输入用户名" /></view>
        <view class="lcf-item"><text class="lcf-label">密码</text><input v-model="password" class="lcf-input" type="password" placeholder="请输入密码" /></view>
        <button class="lcf-btn" :loading="loadingPwd" @click="handlePwdLogin">登录</button>
      </view>

      <!-- SMS Login -->
      <view v-if="tab===1" class="lc-form">
        <view class="lcf-item"><text class="lcf-label">手机号</text><input v-model="phone" class="lcf-input" type="number" placeholder="请输入11位手机号" maxlength="11" /></view>
        <view class="lcf-item"><text class="lcf-label">验证码</text><view class="lcf-row"><input v-model="smsCode" class="lcf-input flex1" type="number" placeholder="请输入6位验证码" maxlength="6" /><text class="lcf-send" @click="sendSms">{{ smsText }}</text></view></view>
        <button class="lcf-btn" :loading="loadingSms" @click="handleSmsLogin">登录</button>
      </view>

      <view class="lc-bottom"><text>还没有账号？</text><text class="lcb-link" @click="goRegister">注册</text></view>
      <text class="lc-hint">测试账号：admin / 123456</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '../../store/user.js'
import request from '../../api/request.js'

const userStore = useUserStore()
const tab = ref(0)

// Password login
const username = ref('')
const password = ref('')
const loadingPwd = ref(false)

// SMS login
const phone = ref('')
const smsCode = ref('')
const loadingSms = ref(false)
const smsText = ref('获取验证码')
const smsing = ref(false)
let timer = null

function sendSms() {
  if (smsing.value) return
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  smsing.value = true
  request.post('/auth/sendCode', { phone: phone.value }).then(() => {
    uni.showToast({ title: '验证码已发送，请查看控制台日志', icon: 'none' })
    let s = 60; smsText.value = s + 's'
    timer = setInterval(() => { s--; smsText.value = s + 's'; if (s <= 0) { clearInterval(timer); smsText.value = '重新获取'; smsing.value = false } }, 1000)
  }).catch(e => { uni.showToast({ title: e?.message || '发送失败', icon: 'none' }); smsing.value = false })
}

async function handlePwdLogin() {
  if (!username.value) return uni.showToast({ title: '请输入用户名', icon: 'none' })
  if (!password.value) return uni.showToast({ title: '请输入密码', icon: 'none' })
  loadingPwd.value = true
  try {
    await userStore.login(username.value, password.value)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 1000)
  } catch { } finally { loadingPwd.value = false }
}

async function handleSmsLogin() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  if (!smsCode.value) return uni.showToast({ title: '验证码不能为空', icon: 'none' })
  loadingSms.value = true
  try {
    await userStore.loginByPhone(phone.value, smsCode.value)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 1000)
  } catch { } finally { loadingSms.value = false }
}

function goRegister() { uni.navigateTo({ url: '/pages/login/register' }) }
</script>

<style scoped lang="scss">
.login-page { min-height:100vh; background:linear-gradient(180deg,#EFF6FF,#F8FAFC 40%); display:flex; align-items:center; justify-content:center; padding:48rpx; }
.login-card { width:100%; background:#fff; border-radius:32rpx; padding:48rpx 40rpx; box-shadow:0 8rpx 40rpx rgba(0,0,0,.06); }
.lc-logo { display:flex;flex-direction:column;align-items:center;margin-bottom:32rpx; }
.lcl-icon { width:88rpx;height:88rpx;border-radius:24rpx;background:#EFF6FF;display:flex;align-items:center;justify-content:center;font-size:44rpx; }
.lcl-title { font-size:34rpx;font-weight:800;color:#111827;margin-top:16rpx; }
.lcl-sub { font-size:24rpx;color:#9CA3AF;margin-top:6rpx; }
.lc-tabs { display:flex;background:#F1F5F9;border-radius:14rpx;padding:6rpx;margin-bottom:28rpx; }
.lct { flex:1;text-align:center;padding:16rpx;font-size:28rpx;color:#6B7280;border-radius:12rpx;transition:all .2s; }
.lct.active { background:#fff;color:#2563EB;font-weight:700;box-shadow:0 2rpx 8rpx rgba(0,0,0,.06); }
.lc-form { display:flex;flex-direction:column;gap:24rpx; }
.lcf-label { font-size:26rpx;font-weight:600;color:#374151;display:block;margin-bottom:10rpx; }
.lcf-input { background:#F1F5F9;border-radius:14rpx;padding:20rpx;font-size:28rpx; }
.flex1 { flex:1; }
.lcf-row { display:flex;gap:16rpx;align-items:center; }
.lcf-send { font-size:26rpx;color:#2563EB;font-weight:600;white-space:nowrap;min-width:140rpx;text-align:center; }
.lcf-btn { width:100%;background:linear-gradient(135deg,#2563EB,#1D4ED8);color:#fff;border:none;border-radius:16rpx;font-size:30rpx;font-weight:700;padding:22rpx;margin-top:12rpx; }
.lc-bottom { display:flex;justify-content:center;gap:8rpx;margin-top:28rpx;font-size:26rpx;color:#6B7280; }
.lcb-link { color:#2563EB;font-weight:600; }
.lc-hint { text-align:center;font-size:22rpx;color:#D1D5DB;margin-top:20rpx; }
</style>
