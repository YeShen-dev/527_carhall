<template>
  <div class="avatar-uploader">
    <el-upload
      class="au-upload"
      action="/api/upload/avatar"
      :headers="uploadHeaders"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      accept="image/*"
    >
      <div class="au-preview" :class="{hover:modelValue}">
        <img v-if="modelValue" :src="modelValue" class="au-img" />
        <div v-else class="au-placeholder">
          <span class="au-initial">{{ initials }}</span>
        </div>
        <div class="au-overlay">
          <el-icon :size="20"><Camera /></el-icon>
        </div>
      </div>
    </el-upload>
    <p class="au-tip">{{ tip }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
  initials: { type: String, default: 'U' },
  tip: { type: String, default: '点击更换头像 (支持 JPG/PNG, 最大 2MB)' }
})
const emit = defineEmits(['update:modelValue'])

const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

function beforeUpload(file) {
  const isImage = /^image\/(jpeg|png|gif|webp)$/.test(file.type)
  if (!isImage) { ElMessage.error('仅支持 JPG/PNG/GIF/WebP 格式'); return false }
  const lt2M = file.size < 2 * 1024 * 1024
  if (!lt2M) { ElMessage.error('图片不能超过 2MB'); return false }
  return true
}

function handleSuccess(res) {
  if (res.code === 200 && res.data) {
    emit('update:modelValue', res.data.url || res.data)
    ElMessage.success('头像更新成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function handleError() {
  ElMessage.error('上传失败，请重试')
}
</script>

<style scoped>
.avatar-uploader { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.au-upload { display: flex; justify-content: center; }
.au-preview {
  width: 100px; height: 100px; border-radius: 50%; overflow: hidden;
  position: relative; cursor: pointer; transition: transform 0.2s;
}
.au-preview:hover { transform: scale(1.05); }
.au-img { width: 100%; height: 100%; object-fit: cover; }
.au-placeholder {
  width: 100%; height: 100%; background: linear-gradient(135deg, #2563EB, #3B82F6);
  display: flex; align-items: center; justify-content: center;
}
.au-initial { font-size: 40px; font-weight: 800; color: #fff; }
.au-overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity 0.2s; color: #fff;
}
.au-preview:hover .au-overlay { opacity: 1; }
.au-tip { font-size: 12px; color: #94A3B8; text-align: center; }
</style>
