<template>
  <div class="comment-input">
    <div class="ci-header">
      <span class="ci-label">{{ isEdit ? '编辑评论' : '发表评论' }}</span>
      <slot name="extra" />
    </div>

    <!-- Rating -->
    <div class="ci-rating">
      <span class="ci-rating-label">评分</span>
      <span v-for="s in 5" :key="s" class="ci-star" :class="{ active: s <= rating }"
        @click="rating = s" @mouseenter="hoverStar = s" @mouseleave="hoverStar = 0">
        {{ (hoverStar || rating) >= s ? '★' : '☆' }}
      </span>
    </div>

    <!-- Content -->
    <textarea v-model="content" :placeholder="placeholder" :maxlength="maxLength" class="ci-textarea" />
    <div class="ci-footer">
      <div class="ci-options">
        <label class="ci-opt"><input type="checkbox" v-model="anonymous" /> 匿名发布</label>
      </div>
      <span class="ci-count">{{ content.length }}/{{ maxLength }}</span>
    </div>

    <div class="ci-actions">
      <el-button v-if="isEdit && !readonly" @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit" :disabled="!valid">
        {{ isEdit ? '保存修改' : '发布评论' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  initialContent: { type: String, default: '' },
  initialRating: { type: Number, default: 5 },
  initialAnonymous: { type: Boolean, default: false },
  isEdit: { type: Boolean, default: false },
  placeholder: { type: String, default: '分享你的使用体验 (至少10个字)' },
  maxLength: { type: Number, default: 500 },
  readonly: { type: Boolean, default: false }
})
const emit = defineEmits(['submit', 'cancel'])

const loading = ref(false)
const rating = ref(props.initialRating)
const content = ref(props.initialContent)
const anonymous = ref(props.initialAnonymous)
const hoverStar = ref(0)

const valid = computed(() => content.value.trim().length >= 10)

async function handleSubmit() {
  if (!valid.value) return ElMessage.warning('评论内容至少10个字')
  loading.value = true
  try {
    await emit('submit', { rating: rating.value, content: content.value.trim(), anonymous: anonymous.value })
  } finally { loading.value = false }
}
</script>

<style scoped>
.comment-input { background: #fff; border-radius: 16px; padding: 24px; border: 1px solid #F1F5F9; box-shadow: 0 1px 3px rgba(0,0,0,.04); }
.ci-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.ci-label { font-size: 16px; font-weight: 700; color: #0F172A; }
.ci-rating { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; }
.ci-rating-label { font-size: 13px; color: #64748B; margin-right: 6px; }
.ci-star { font-size: 28px; color: #E2E8F0; cursor: pointer; transition: transform 0.15s, color 0.15s; user-select: none; }
.ci-star.active { color: #F59E0B; transform: scale(1.12); }
.ci-star:hover { transform: scale(1.2); }
.ci-textarea { width: 100%; height: 120px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; padding: 14px; font-size: 14px; color: #0F172A; resize: vertical; font-family: inherit; outline: none; transition: border-color 0.2s; box-sizing: border-box; }
.ci-textarea:focus { border-color: #2563EB; }
.ci-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.ci-options { display: flex; gap: 12px; }
.ci-opt { font-size: 13px; color: #64748B; display: flex; align-items: center; gap: 4px; cursor: pointer; }
.ci-count { font-size: 12px; color: #94A3B8; }
.ci-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 16px; }
</style>
