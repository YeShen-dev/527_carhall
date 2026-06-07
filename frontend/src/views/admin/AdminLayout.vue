<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="admin-sidebar">
      <div class="sidebar-brand" @click="$router.push('/')">
        <div class="sb-logo"><svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="white" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><path d="M14.31 8l5.74 9.94H3.95L9.69 8"/></svg></div>
        <div><span class="sb-title">AutoParts</span><span class="sb-sub">管理后台</span></div>
      </div>
      <nav class="sidebar-nav">
        <a class="sb-link" :class="{active:route.path.startsWith('/admin/products')}" @click="router.push('/admin/products')">
          <el-icon><Goods /></el-icon> 商品管理
        </a>
        <a class="sb-link" :class="{active:route.path.startsWith('/admin/orders')}" @click="router.push('/admin/orders')">
          <el-icon><Document /></el-icon> 订单管理
        </a>
        <a class="sb-link" :class="{active:route.path.startsWith('/admin/seckill')}" @click="router.push('/admin/seckill')">
          <el-icon><Lightning /></el-icon> 秒杀管理
        </a>
        <a class="sb-link" :class="{active:route.path.startsWith('/admin/comments')}" @click="router.push('/admin/comments')">
          <el-icon><ChatLineSquare /></el-icon> 评论管理
        </a>
      </nav>
      <div class="sidebar-footer">
        <a class="sb-link" @click="router.push('/')"><el-icon><ArrowLeft /></el-icon> 返回商城</a>
      </div>
    </aside>

    <!-- Content -->
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()
</script>

<style scoped>
.admin-layout { display: flex; min-height: calc(100vh - 140px); }
.admin-sidebar {
  width: 220px; background: #1E293B; display: flex; flex-direction: column;
  border-radius: 16px; overflow: hidden; flex-shrink: 0;
}
.sidebar-brand { display: flex; align-items: center; gap: 10px; padding: 20px 16px; border-bottom: 1px solid rgba(255,255,255,0.06); cursor: pointer; }
.sb-logo { width: 36px; height: 36px; background: linear-gradient(135deg,#2563EB,#3B82F6); border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.sb-title { font-size: 16px; font-weight: 800; color: #F8FAFC; display: block; }
.sb-sub { font-size: 10px; color: #94A3B8; display: block; }
.sidebar-nav { flex: 1; padding: 8px; }
.sb-link { display: flex; align-items: center; gap: 10px; padding: 10px 14px; font-size: 14px; color: #CBD5E1; cursor: pointer; border-radius: 8px; transition: all 0.15s; text-decoration: none; margin-bottom: 2px; }
.sb-link:hover { background: rgba(255,255,255,0.06); color: #fff; }
.sb-link.active { background: rgba(37,99,235,0.2); color: #60A5FA; font-weight: 600; }
.sidebar-footer { padding: 8px; border-top: 1px solid rgba(255,255,255,0.06); }
.admin-main { flex: 1; padding: 8px 0 8px 20px; min-width: 0; }
@media (max-width: 768px) {
  .admin-layout { flex-direction: column; }
  .admin-sidebar { width: 100%; flex-direction: row; border-radius: 12px; padding: 4px; }
  .sidebar-brand, .sidebar-footer { display: none; }
  .sidebar-nav { display: flex; gap: 4px; padding: 4px; }
  .admin-main { padding: 12px 0; }
}
</style>
