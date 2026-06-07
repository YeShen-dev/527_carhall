<template>
  <div class="comment-list">
    <div v-if="loading" class="cl-skeleton">
      <div v-for="i in 3" :key="i" class="cl-sk-item">
        <div class="skeleton" style="width:40px;height:40px;border-radius:50%" />
        <div style="flex:1">
          <div class="skeleton" style="width:100px;height:16px;margin-bottom:8px" />
          <div class="skeleton" style="width:80%;height:14px;margin-bottom:4px" />
          <div class="skeleton" style="width:60%;height:14px" />
        </div>
      </div>
    </div>

    <div v-else-if="comments.length === 0" class="cl-empty">
      <el-icon :size="40"><ChatLineSquare /></el-icon>
      <p>{{ emptyText }}</p>
    </div>

    <div v-else class="cl-list">
      <div v-for="c in comments" :key="c.id" class="cl-card">
        <div class="cl-card-top">
          <div class="cl-avatar" :style="{background:avatarBg(c.userId)}">{{ c.username?.charAt(0) || '匿' }}</div>
          <div class="cl-info">
            <div class="cl-user-row">
              <span class="cl-username">{{ c.username }}</span>
              <span class="cl-rating">{{ '★'.repeat(c.rating) }}{{ '☆'.repeat(5 - c.rating) }}</span>
            </div>
            <span class="cl-time">{{ formatTime(c.createTime) }}</span>
          </div>
          <div v-if="showEdit && c.userId === currentUserId" class="cl-actions">
            <el-button text size="small" @click="$emit('edit', c)"><el-icon><Edit /></el-icon></el-button>
            <el-popconfirm title="确认删除?" @confirm="$emit('delete', c.id)"><template #reference><el-button text size="small" type="danger"><el-icon><Delete /></el-icon></el-button></template></el-popconfirm>
          </div>
        </div>
        <p class="cl-content">{{ c.content }}</p>
        <div v-if="c.images?.length" class="cl-images">
          <el-image v-for="(img,i) in c.images" :key="i" :src="img" fit="cover" :preview-src-list="c.images"
            style="width:80px;height:80px;border-radius:8px;cursor:pointer" />
        </div>
        <div class="cl-card-footer">
          <slot name="actions" :comment="c">
            <span class="cl-like" :class="{ liked: c.liked }" @click="$emit('like', c)">
              <el-icon><CaretTop /></el-icon> {{ c.likeCount || 0 }}
            </span>
          </slot>
          <el-tag v-if="c.isAnonymous" size="small" type="info" effect="plain">匿名</el-tag>
        </div>
      </div>
    </div>

    <div v-if="hasMore" class="cl-more" @click="$emit('loadMore')">{{ moreLoading ? '加载中...' : '加载更多' }}</div>
  </div>
</template>

<script setup>
defineProps({
  comments: { type: Array, default: () => [] },
  currentUserId: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  hasMore: { type: Boolean, default: false },
  moreLoading: { type: Boolean, default: false },
  showEdit: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无评论' }
})
defineEmits(['edit', 'delete', 'like', 'loadMore'])

const colors = ['#2563EB','#10B981','#F59E0B','#EF4444','#8B5CF6','#EC4899','#06B6D4']
function avatarBg(id) {
  return colors[(id || 0) % colors.length]
}
function formatTime(t) {
  if (!t) return ''; return new Date(t).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.cl-skeleton { display: flex; flex-direction: column; gap: 16px; }
.cl-sk-item { display: flex; gap: 12px; padding: 16px; background: #fff; border-radius: 12px; }
.cl-empty { display: flex; flex-direction: column; align-items: center; padding: 48px 0; color: #94A3B8; gap: 12px; font-size: 14px; }
.cl-list { display: flex; flex-direction: column; gap: 14px; }
.cl-card { background: #fff; border-radius: 16px; padding: 20px; border: 1px solid #F1F5F9; box-shadow: 0 1px 3px rgba(0,0,0,.04); transition: box-shadow 0.2s; }
.cl-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,.06); }
.cl-card-top { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.cl-avatar { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; color: #fff; flex-shrink: 0; }
.cl-info { flex: 1; }
.cl-user-row { display: flex; align-items: center; gap: 8px; }
.cl-username { font-size: 14px; font-weight: 600; color: #0F172A; }
.cl-rating { font-size: 13px; color: #F59E0B; }
.cl-time { font-size: 12px; color: #94A3B8; }
.cl-actions { display: flex; gap: 2px; }
.cl-content { font-size: 14px; color: #334155; line-height: 1.7; white-space: pre-wrap; }
.cl-images { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; }
.cl-card-footer { display: flex; align-items: center; gap: 12px; margin-top: 12px; padding-top: 10px; border-top: 1px solid #F8FAFC; }
.cl-like { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #94A3B8; cursor: pointer; transition: color 0.2s; }
.cl-like.liked { color: #2563EB; }
.cl-more { text-align: center; padding: 16px; color: #2563EB; font-size: 13px; cursor: pointer; }
</style>
