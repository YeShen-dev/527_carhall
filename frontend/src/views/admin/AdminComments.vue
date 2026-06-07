<template>
  <div class="admin-comments">
    <div class="page-header">
      <h2>评论管理</h2>
    </div>

    <div class="filter-bar">
      <el-select v-model="filters.status" clearable placeholder="审核状态" @change="loadData">
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
      </el-select>
      <el-input v-model="filters.keyword" placeholder="搜索用户名" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户" width="120" />
      <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="评分" width="120">
        <template #default="{ row }">{{ '★'.repeat(row.rating) }}{{ '☆'.repeat(5 - row.rating) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" size="small" @click="review(row.id, 1)">通过</el-button>
          <el-button v-if="row.status === 0" type="warning" size="small" @click="review(row.id, 2)">拒绝</el-button>
          <el-button type="danger" size="small" text @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination v-model:current-page="page" :page-size="size" :total="total" layout="prev,pager,next" background @current-change="loadData" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request.js'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filters = reactive({ status: null, keyword: '' })

function statusLabel(s) { return { 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || s }
function statusType(s) { return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info' }
function formatTime(t) { if (!t) return ''; return new Date(t).toLocaleString('zh-CN') }

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/admin/comment/list', {
      page: page.value - 1, size: size.value,
      status: filters.status, keyword: filters.keyword || undefined
    })
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

async function review(id, status) {
  try {
    await request.post('/admin/comment/review', { id, status })
    ElMessage.success(status === 1 ? '审核通过' : '已拒绝')
    loadData()
  } catch { ElMessage.error('操作失败') }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该评论？', '提示', { type: 'warning' })
    await request.delete(`/admin/comment/${id}`)
    ElMessage.success('已删除')
    loadData()
  } catch { }
}

onMounted(() => loadData())
</script>

<style scoped>
.admin-comments { display: flex; flex-direction: column; gap: 16px; }
.page-header h2 { font-size: 20px; font-weight: 700; color: #0F172A; }
.filter-bar { display: flex; gap: 12px; align-items: center; background: #fff; padding: 16px 20px; border-radius: 12px; border: 1px solid #F1F5F9; }
.pagination-wrapper { display: flex; justify-content: center; padding: 16px 0; }
</style>
