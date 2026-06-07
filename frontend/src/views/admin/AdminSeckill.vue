<template>
  <div class="admin-seckill">
    <div class="toolbar">
      <div class="toolbar-left">
        <h3>秒杀活动管理</h3>
      </div>
      <el-button type="primary" @click="openAdd">
        <el-icon><Plus /></el-icon> 新增秒杀活动
      </el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="productName" label="商品名称" min-width="140" />
      <el-table-column prop="productId" label="商品ID" width="80" />
      <el-table-column prop="seckillPrice" label="秒杀价" width="100">
        <template #default="{ row }">
          <span style="color:#ff6b35;font-weight:700">¥{{ row.seckillPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="startTime" label="开始时间" width="170">
        <template #default="{ row }">{{ fmtDisplay(row.startTime) }}</template>
      </el-table-column>
      <el-table-column prop="endTime" label="结束时间" width="170">
        <template #default="{ row }">{{ fmtDisplay(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row)" size="small">{{ statusText(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button text type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchData"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑秒杀活动' : '新增秒杀活动'"
      width="480px"
      destroy-on-close
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="商品ID" required>
          <el-input-number v-model="form.productId" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="秒杀价格" required>
          <el-input-number v-model="form.seckillPrice" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="秒杀库存" required>
          <el-input-number v-model="form.stock" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="开始时间" required>
          <input
            v-model="form.startTime"
            type="datetime-local"
            class="native-dt"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <input
            v-model="form.endTime"
            type="datetime-local"
            class="native-dt"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ editing ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAdminSeckill, createSeckill, updateSeckill, deleteSeckill } from '../../api/seckill.js'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editing = ref(false)
const editingId = ref(null)

function pad(n) { return String(n).padStart(2, '0') }

function toDatetimeLocal(d) {
  if (!d || !(d instanceof Date) || isNaN(d.getTime())) d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function fromDatetimeLocal(s) {
  return s ? new Date(s) : new Date()
}

function fmtDisplay(s) {
  if (!s) return ''
  return new Date(s).toLocaleString('zh-CN')
}

function statusType(row) {
  const now = Date.now()
  if (now < new Date(row.startTime).getTime()) return 'info'
  if (now > new Date(row.endTime).getTime()) return ''
  return 'success'
}

function statusText(row) {
  const now = Date.now()
  if (now < new Date(row.startTime).getTime()) return '未开始'
  if (now > new Date(row.endTime).getTime()) return '已结束'
  return '进行中'
}

const form = reactive({
  productId: undefined,
  seckillPrice: undefined,
  stock: undefined,
  startTime: '',
  endTime: ''
})

async function fetchData() {
  loading.value = true
  try {
    const res = await listAdminSeckill(page.value - 1, size.value)
    list.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editing.value = false
  editingId.value = null
  form.productId = undefined
  form.seckillPrice = undefined
  form.stock = undefined
  form.startTime = toDatetimeLocal(new Date())
  form.endTime = toDatetimeLocal(new Date(Date.now() + 3600000))
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = true
  editingId.value = row.id
  form.productId = row.productId
  form.seckillPrice = row.seckillPrice
  form.stock = row.stock
  form.startTime = row.startTime ? toDatetimeLocal(new Date(row.startTime)) : toDatetimeLocal(new Date())
  form.endTime = row.endTime ? toDatetimeLocal(new Date(row.endTime)) : toDatetimeLocal(new Date())
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.productId || !form.seckillPrice || form.stock == null) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  if (!form.startTime || !form.endTime) {
    ElMessage.warning('请选择开始和结束时间')
    return
  }
  saving.value = true
  try {
    const data = {
      productId: form.productId,
      seckillPrice: form.seckillPrice,
      stock: form.stock,
      startTime: new Date(form.startTime).toISOString().slice(0, 19),
      endTime: new Date(form.endTime).toISOString().slice(0, 19)
    }
    if (editing.value) {
      await updateSeckill(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createSeckill(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteSeckill(id)
    ElMessage.success('已删除')
    await fetchData()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.admin-seckill { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.toolbar-left h3 { font-size: 18px; font-weight: 700; }
.pagination-wrapper { display: flex; justify-content: center; padding: 16px 0; }

.native-dt {
  width: 100%;
  background: #1c1c2c;
  border: 1px solid #2a2a3e;
  border-radius: 10px;
  padding: 10px 14px;
  color: #f0f0f5;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition: border-color 0.2s;
}
.native-dt:focus { border-color: #e63946; }
.native-dt::-webkit-calendar-picker-indicator {
  filter: invert(0.7);
  cursor: pointer;
}
</style>
