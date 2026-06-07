<template>
  <view class="ai-page">
    <!-- Messages -->
    <scroll-view scroll-y class="ai-messages" :scroll-top="scrollTop" :scroll-with-animation="true">
      <view v-for="(m,i) in messages" :key="i" class="ai-msg" :class="m.role==='user'?'msg-user':'msg-bot'">
        <view class="ai-bubble" :class="m.role==='user'?'bubble-user':'bubble-bot'">
          <text>{{ m.content }}</text>
        </view>
      </view>
      <view v-if="thinking" class="ai-msg msg-bot">
        <view class="ai-bubble bubble-bot thinking"><text class="dot-pulse">...</text></view>
      </view>
    </scroll-view>

    <!-- Quick questions -->
    <view class="ai-quick" v-if="messages.length===0">
      <text class="aq-title">你可以问我：</text>
      <text v-for="q in quickQs" :key="q" class="aq-item" @click="sendQuick(q)">{{ q }}</text>
    </view>

    <!-- Input -->
    <view class="ai-input-bar">
      <input v-model="input" placeholder="输入你的问题..." class="ai-input" @confirm="send" />
      <button class="ai-send" :disabled="!input.trim()||thinking" @click="send">发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { chat } from '../../api/ai.js'

const messages = ref([])
const input = ref('')
const thinking = ref(false)
const scrollTop = ref(0)
const quickQs = ['推荐适合的机油','刹车片多久更换','如何判断火花塞需要更换','LED大灯和卤素灯哪个好','减震器异响怎么办']

async function send() {
  const text = input.value.trim()
  if(!text || thinking.value) return
  input.value = ''
  messages.value.push({ role:'user', content:text })
  thinking.value = true
  scrollToBottom()
  try {
    const res = await chat(text)
    messages.value.push({ role:'bot', content:res.data?.reply || res.data?.content || '抱歉，我暂时无法回答这个问题' })
  } catch {
    messages.value.push({ role:'bot', content:'网络错误，请稍后重试' })
  } finally {
    thinking.value = false
    scrollToBottom()
  }
}

function sendQuick(q) { input.value = q; send() }
function scrollToBottom() { nextTick(()=>{ scrollTop.value = 99999 }) }
</script>

<style scoped lang="scss">
.ai-page { background:#F8FAFC;min-height:100vh;display:flex;flex-direction:column; }
.ai-messages { flex:1;padding:24rpx; }
.ai-msg { margin-bottom:20rpx;display:flex; }
.msg-user { justify-content:flex-end; }
.msg-bot { justify-content:flex-start; }
.ai-bubble { max-width:80%;padding:16rpx 20rpx;border-radius:20rpx;font-size:28rpx;line-height:1.6; }
.bubble-user { background:linear-gradient(135deg,#2563EB,#3B82F6);color:#fff;border-radius:20rpx 4rpx 20rpx 20rpx; }
.bubble-bot { background:#fff;color:#0F172A;border-radius:4rpx 20rpx 20rpx 20rpx;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.thinking { width:80rpx;text-align:center; }
.dot-pulse { animation:dotPulse 1.4s infinite; }
@keyframes dotPulse { 0%,80%,100%{opacity:.3} 40%{opacity:1} }
.ai-quick { padding:24rpx; }
.aq-title { font-size:28rpx;font-weight:600;color:#0F172A;display:block;margin-bottom:16rpx; }
.aq-item { display:inline-block;background:#fff;border-radius:20rpx;padding:12rpx 20rpx;font-size:24rpx;color:#2563EB;margin:0 12rpx 12rpx 0;box-shadow:0 2rpx 8rpx rgba(0,0,0,.04); }
.ai-input-bar { display:flex;align-items:center;gap:12rpx;padding:16rpx 24rpx;background:#fff;border-top:1rpx solid #F1F5F9;padding-bottom:calc(16rpx + env(safe-area-inset-bottom)); }
.ai-input { flex:1;background:#F1F5F9;border-radius:40rpx;padding:16rpx 24rpx;font-size:28rpx; }
.ai-send { background:#2563EB;color:#fff;border:none;border-radius:40rpx;padding:16rpx 32rpx;font-size:28rpx;font-weight:600; }
.ai-send[disabled] { background:#CBD5E1; }
</style>
