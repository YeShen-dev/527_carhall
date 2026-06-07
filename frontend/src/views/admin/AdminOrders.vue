<template>
  <div class="admin-orders">
    <div class="toolbar">
      <el-select v-model="statusFilter" clearable placeholder="订单状态" @change="doSearch" style="width:150px">
        <el-option label="待支付" value="PENDING" />
        <el-option label="待核实" value="VERIFYING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>

    <el-table v-loading="loading" :data="orders" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderNo" label="订单编号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="totalAmount" label="总金额" width="110">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-select
            :model-value="row.status"
            size="small"
            @change="(val) => handleStatusChange(row.id, val)"
          >
            <el-option label="待支付" value="PENDING" />
            <el-option label="待核实" value="VERIFYING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="showDetail(row.id)">详情</el-button>
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

    <!-- Order Detail Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="560px">
      <div v-if="detail">
        <p><strong>订单编号：</strong>{{ detail.orderNo }}</p>
        <p><strong>金额：</strong>¥{{ detail.totalAmount }}</p>
        <p><strong>状态：</strong>{{ detail.status }}</p>
        <p><strong>时间：</strong>{{ formatDate(detail.createdAt) }}</p>
        <el-divider />
        <div v-for="item in detail.items" :key="item.productId" class="detail-item">
          <span>{{ item.productName }}</span>
          <span>x{{ item.quantity }}</span>
          <span>¥{{ item.subtotal }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listAdminOrders, getAdminOrderDetail, updateOrderStatus } from '../../api/admin.js'

const orders = ref([])
const loading = ref(false)
const statusFilter = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

const detailVisible = ref(false)
const detail = ref(null)

function formatDate(d) { return d ? new Date(d).toLocaleString('zh-CN') : '' }

async function doSearch() {
  loading.value = true
  try {
    const params = { page: page.value - 1, size: size.value }
    if (statusFilter.value) params.status = statusFilter.value
    const res = await listAdminOrders(params)
    orders.value = res.data.content
    total.value = res.data.totalElements
  } finally { loading.value = false }
}

async function handleStatusChange(id, status) {
  await updateOrderStatus(id, status)
  ElMessage.success('状态已更新')
  doSearch()
}

async function showDetail(id) {
  const res = await getAdminOrderDetail(id)
  detail.value = res.data
  detailVisible.value = true
}

onMounted(() => doSearch())
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.pagination-wrap { display: flex; justify-content: center; margin-top: 16px; }
.detail-item { display: flex; justify-content: space-between; padding: 6px 0; }
</style>
