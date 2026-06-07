<template>
  <div class="admin-products">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称/分类/品牌/厂商"
        clearable
        style="width:320px"
        @keyup.enter="doSearch"
        @clear="doSearch"
      />
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon> 添加商品
      </el-button>
    </div>

    <el-table v-loading="loading" :data="products" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column label="图片" width="80">
        <template #default="{ row }">
          <el-image v-if="row.imageUrl" :src="row.imageUrl" fit="cover" style="width:48px;height:48px;border-radius:6px" />
          <span v-else style="color:#666">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="90" />
      <el-table-column prop="brand" label="品牌" width="100" />
      <el-table-column prop="spec" label="规格" width="100" />
      <el-table-column prop="manufacturer" label="厂商" width="80" />
      <el-table-column prop="price" label="价格" width="90">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 'ON'"
            active-text="上架"
            inactive-text="下架"
            inline-prompt
            size="small"
            @change="(val) => handleToggleStatus(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="showEditDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="doSearch"
      />
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑商品' : '添加商品'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="form.spec" /></el-form-item>
        <el-form-item label="厂商"><el-input v-model="form.manufacturer" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
        <el-form-item label="上架状态">
          <el-switch
            v-model="form.statusOn"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
        <el-form-item label="商品图片">
          <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap">
            <el-input v-model="form.imageUrl" placeholder="图片URL或点击上传" style="flex:1" />
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleFileChange"
              accept="image/*"
            >
              <el-button type="primary" :loading="uploading">
                <el-icon><Upload /></el-icon> 上传
              </el-button>
            </el-upload>
          </div>
          <el-image
            v-if="form.imageUrl"
            :src="form.imageUrl"
            fit="cover"
            style="width:120px;height:120px;border-radius:8px;margin-top:8px"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ editing ? '保存' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAdminProducts, addProduct, updateProduct, deleteProduct, uploadFile } from '../../api/admin.js'

const products = ref([])
const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const editing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editingId = ref(null)
const form = reactive({
  name: '', category: '', brand: '', spec: '', manufacturer: '',
  price: 0, stock: 0, imageUrl: '', description: '', statusOn: true
})

function resetForm() {
  Object.assign(form, {
    name: '', category: '', brand: '', spec: '', manufacturer: '',
    price: 0, stock: 0, imageUrl: '', description: '', statusOn: true
  })
}

async function doSearch() {
  loading.value = true
  try {
    const res = await listAdminProducts({ keyword: keyword.value, page: page.value - 1, size: size.value })
    products.value = res.data.content
    total.value = res.data.totalElements
  } finally { loading.value = false }
}

function showAddDialog() {
  editing.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function showEditDialog(row) {
  editing.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name, category: row.category, brand: row.brand,
    spec: row.spec || '', manufacturer: row.manufacturer || '',
    price: row.price, stock: row.stock, imageUrl: row.imageUrl || '',
    description: row.description || '', statusOn: row.status === 'ON'
  })
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const data = {
      name: form.name, category: form.category, brand: form.brand,
      spec: form.spec, manufacturer: form.manufacturer,
      price: form.price, stock: form.stock,
      imageUrl: form.imageUrl, description: form.description,
      status: form.statusOn ? 'ON' : 'OFF'
    }
    if (editing.value) {
      await updateProduct(editingId.value, data)
      ElMessage.success('更新成功')
    } else {
      await addProduct(data)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    doSearch()
  } finally { saving.value = false }
}

async function handleDelete(id) {
  await deleteProduct(id)
  ElMessage.success('已删除')
  doSearch()
}

async function handleToggleStatus(row, val) {
  await updateProduct(row.id, { ...row, status: val ? 'ON' : 'OFF' })
  ElMessage.success(val ? '已上架' : '已下架')
  doSearch()
}

async function handleFileChange(file) {
  if (!file.raw) return
  uploading.value = true
  try {
    const res = await uploadFile(file.raw)
    form.imageUrl = res.data
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

onMounted(() => doSearch())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }
</style>
