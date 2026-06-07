<template>
  <div class="ai-widget" :class="{ open: isOpen }">
    <div v-if="isOpen" class="chat-panel">
      <div class="chat-header">
        <span><el-icon><ChatDotRound /></el-icon> AI 配件助手</span>
        <el-button text size="small" @click="isOpen = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>

      <div class="chat-body" ref="chatBodyRef">
        <div v-if="messages.length === 0" class="chat-hint">
          <el-icon :size="36"><Cpu /></el-icon>
          <p>告诉我你的车型和故障症状<br/>我会推荐合适的配件给您</p>
          <div class="hint-examples">
            <el-tag
              v-for="q in quickQuestions"
              :key="q"
              @click="send(q)"
              class="quick-tag"
            >{{ q }}</el-tag>
          </div>
        </div>

        <div v-for="(msg, idx) in messages" :key="idx" :class="['message', msg.role]">
          <div class="bubble">{{ msg.content }}</div>
        </div>

        <div v-if="sending" class="message assistant">
          <div class="bubble typing">正在思考...</div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="input"
          placeholder="输入车型和故障描述..."
          @keyup.enter="send(input)"
          :disabled="sending"
        >
          <template #append>
            <el-button :icon="Promotion" @click="send(input)" :disabled="sending" />
          </template>
        </el-input>
      </div>
    </div>

    <div v-if="!isOpen" class="chat-trigger" @click="isOpen = true">
      <el-badge :value="unread" :hidden="unread === 0">
        <el-button type="primary" circle size="large">
          <el-icon :size="24"><ChatDotRound /></el-icon>
        </el-button>
      </el-badge>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { aiChat } from '../api/ai.js'

const isOpen = ref(false)
const input = ref('')
const messages = ref([])
const sending = ref(false)
const unread = ref(0)
const chatBodyRef = ref(null)

const quickQuestions = [
  '2018款奥迪A4L 刹车异响',
  '丰田凯美瑞 空调不制冷',
  '大众迈腾 发动机抖动'
]

async function send(text) {
  const msg = text || input.value
  if (!msg.trim() || sending.value) return

  messages.value.push({ role: 'user', content: msg })
  input.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const res = await aiChat(msg)
    messages.value.push({ role: 'assistant', content: res.data })
    if (!isOpen.value) unread.value++
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，AI服务暂时不可用，请稍后再试。' })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}

watch(isOpen, (val) => {
  if (val) {
    unread.value = 0
    nextTick(() => scrollToBottom())
  }
})
</script>

<style scoped>
.ai-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 999;
}

.chat-trigger {
  cursor: pointer;
}

.chat-trigger .el-button {
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 16px rgba(74,158,255,0.4);
}

.chat-panel {
  width: 380px;
  height: 520px;
  background: #16213e;
  border-radius: 14px;
  border: 1px solid #2d3a50;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px rgba(0,0,0,0.4);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #1f2b47;
  border-bottom: 1px solid #2d3a50;
  font-weight: 600;
  font-size: 15px;
  color: #4a9eff;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-hint {
  text-align: center;
  color: #a0a0b0;
  margin: auto;
  padding: 20px;
}

.chat-hint p {
  margin: 10px 0 14px;
  font-size: 14px;
  line-height: 1.6;
}

.hint-examples {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.quick-tag {
  cursor: pointer;
  font-size: 12px;
}

.quick-tag:hover {
  background-color: #4a9eff;
  color: white;
}

.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.user .bubble {
  background: #4a9eff;
  color: white;
  border-radius: 14px 14px 4px 14px;
}

.message.assistant .bubble {
  background: #1f2b47;
  border: 1px solid #2d3a50;
  border-radius: 14px 14px 14px 4px;
}

.bubble {
  max-width: 85%;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.bubble.typing {
  color: #a0a0b0;
  font-style: italic;
}

.chat-input {
  padding: 12px;
  border-top: 1px solid #2d3a50;
}
</style>
