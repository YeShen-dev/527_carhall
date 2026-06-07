<template>
  <div class="app-container">

    <!-- ===== Top Bar ===== -->
    <div class="top-bar">
      <div class="top-bar-inner">
        <span class="top-contact"><el-icon><Phone /></el-icon> 18029538913</span>
        <span class="top-promise">满¥299包邮 · 7天退换 · 正品保障</span>
        <span v-if="!userStore.isLoggedIn" class="top-auth">
          <a @click="$router.push('/login')">登录</a>
          <span class="sep">|</span>
          <a @click="$router.push('/register')">注册</a>
        </span>
        <span v-else class="top-auth">
          欢迎回来，<strong>{{ userStore.userInfo?.username }}</strong>
        </span>
      </div>
    </div>

    <!-- ===== Header ===== -->
    <header class="main-header">
      <div class="header-inner">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M14.31 8l5.74 9.94H3.95L9.69 8"/></svg>
          </div>
          <div class="logo-text">
            <span class="logo-title">Auto<span class="logo-accent">Parts</span></span>
            <span class="logo-sub">汽修配件商城</span>
          </div>
        </div>

        <!-- Search (desktop) -->
        <div class="header-search">
          <el-input
            v-model="searchText"
            placeholder="搜索配件、品牌、车型、OE号..."
            size="large"
            @keyup.enter="doSearch"
            class="search-input"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
            <template #append>
              <el-button type="primary" @click="doSearch" class="search-btn">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
        </div>

        <!-- Actions -->
        <div class="header-actions">
          <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount" class="action-badge" :offset="[-2, 4]">
            <div class="action-btn" @click="drawerVisible = true">
              <el-icon :size="22"><ShoppingCart /></el-icon>
              <span class="action-label">购物车</span>
            </div>
          </el-badge>

          <template v-if="userStore.isLoggedIn">
            <div class="action-btn" @click="$router.push('/orders')">
              <el-icon :size="22"><Document /></el-icon>
              <span class="action-label">订单</span>
            </div>
            <el-dropdown trigger="click" placement="bottom-end">
              <div class="action-btn avatar-btn">
                <el-avatar :size="32" :icon="UserFilled" />
                <span class="action-label">{{ userStore.userInfo?.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">
                    <el-icon><User /></el-icon> 个人中心
                  </el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/orders')">
                    <el-icon><Document /></el-icon> 我的订单
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" @click="$router.push('/admin')" divided>
                    <el-icon><Setting /></el-icon> 管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon> 退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" size="large" round @click="$router.push('/login')">
              登录 / 注册
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- ===== Navigation ===== -->
    <nav class="main-nav">
      <div class="nav-inner">
        <div class="nav-categories" @mouseenter="showCats = true" @mouseleave="showCats = false">
          <el-button type="primary" size="large">
            <el-icon><Menu /></el-icon> 全部分类
          </el-button>
          <transition name="fade-scale">
            <div v-show="showCats" class="cat-dropdown">
              <div v-for="cat in categories" :key="cat" class="cat-item"
                   @click="goCategory(cat); showCats = false">
                <span class="cat-emoji">{{ catIcons[cat] }}</span>
                <span>{{ cat }}</span>
                <el-icon class="cat-arrow"><ArrowRight /></el-icon>
              </div>
            </div>
          </transition>
        </div>
        <div class="nav-links">
          <a class="nav-link" :class="{ active: $route.path === '/' }" @click="$router.push('/')">首页</a>
          <a class="nav-link seckill-link" :class="{ active: $route.path === '/seckill' }" @click="$router.push('/seckill')">
            限时秒杀 <span class="hot-dot"></span>
          </a>
          <a class="nav-link" v-if="userStore.isLoggedIn" @click="$router.push('/orders')">我的订单</a>
          <a class="nav-link" v-if="userStore.isAdmin" @click="$router.push('/admin')">管理后台</a>
        </div>
      </div>
    </nav>

    <!-- ===== Main Content ===== -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- ===== Footer ===== -->
    <footer class="main-footer">
      <div class="footer-inner">
        <div class="footer-col">
          <h4>关于我们</h4>
          <p class="about-text">汽修配件商城 — 专业汽车配件在线商城，覆盖发动机、刹车系统、滤清器、点火系统等全品类。正品保障，极速配送。</p>
        </div>
        <div class="footer-col">
          <h4>热门分类</h4>
          <a @click="goCategory('发动机')">发动机</a>
          <a @click="goCategory('刹车系统')">刹车系统</a>
          <a @click="goCategory('滤清器')">滤清器</a>
          <a @click="goCategory('车灯照明')">车灯照明</a>
        </div>
        <div class="footer-col">
          <h4>客户服务</h4>
          <a @click="$router.push('/help')">帮助中心</a>
          <a @click="$router.push('/return-policy')">退换货政策</a>
          <a @click="$router.push('/shipping')">配送说明</a>
          <a @click="$router.push('/contact')">联系我们</a>
        </div>
        <div class="footer-col">
          <h4>联系方式</h4>
          <p><el-icon><Phone /></el-icon> 18029538913</p>
          <p><el-icon><Message /></el-icon> chouj357@gmail.com</p>
          <p>周一至周日 9:00-21:00</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>&copy; 2026 AutoParts 汽修配件商城 — 专业汽车配件在线商城</p>
      </div>
    </footer>

    <!-- ===== Mobile Bottom Tab Bar ===== -->
    <nav class="mobile-tabs">
      <div class="mobile-tab" :class="{ active: $route.path === '/' }" @click="$router.push('/')">
        <el-icon :size="22"><HomeFilled v-if="$route.path === '/'" /><HomeFilled v-else /></el-icon>
        <span>首页</span>
      </div>
      <div class="mobile-tab" :class="{ active: $route.path === '/seckill' }" @click="$router.push('/seckill')">
        <el-icon :size="22"><Lightning /></el-icon>
        <span>秒杀</span>
      </div>
      <div class="mobile-tab cart-tab" @click="drawerVisible = true">
        <el-badge :value="cartStore.totalCount" :hidden="!cartStore.totalCount" class="mobile-cart-badge">
          <el-icon :size="24"><ShoppingCart /></el-icon>
        </el-badge>
        <span>购物车</span>
      </div>
      <div class="mobile-tab" :class="{ active: $route.path.startsWith('/orders') }" @click="$router.push('/orders')">
        <el-icon :size="22"><Document /></el-icon>
        <span>订单</span>
      </div>
      <div class="mobile-tab" :class="{ active: $route.path === '/profile' }" @click="$router.push('/profile')">
        <el-icon :size="22"><User /></el-icon>
        <span>我的</span>
      </div>
    </nav>

    <!-- ===== Cart Drawer ===== -->
    <CartDrawer v-model="drawerVisible" />
    <AIChatWidget v-if="userStore.isLoggedIn" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from './stores/cart.js'
import { useUserStore } from './stores/user.js'
import { getSessionId } from './utils/session.js'
import CartDrawer from './components/CartDrawer.vue'
import AIChatWidget from './components/AIChatWidget.vue'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()
const drawerVisible = ref(false)
const searchText = ref('')
const showCats = ref(false)

const categories = ['发动机','刹车系统','滤清器','点火系统','底盘系统','传感器','悬挂系统','车身附件','车灯照明']
const catIcons = {
  '发动机':'🔧','刹车系统':'🛑','滤清器':'🔍','点火系统':'⚡',
  '底盘系统':'🔩','传感器':'📡','悬挂系统':'🏗️','车身附件':'🚗','车灯照明':'💡'
}

function doSearch() {
  if (searchText.value.trim()) {
    router.push(`/?keyword=${encodeURIComponent(searchText.value.trim())}`)
  }
}
function goCategory(cat) {
  router.push(`/?category=${encodeURIComponent(cat)}`)
}
function handleLogout() {
  userStore.logout()
  cartStore.clear()
  router.push('/')
}

onMounted(() => {
  getSessionId()
  cartStore.fetchCart()
})
</script>

<style scoped>
.app-container { min-height: 100vh; display: flex; flex-direction: column; background: #F8FAFC; }

/* ===== Top Bar ===== */
.top-bar { background: #0F172A; font-size: 12px; color: #94A3B8; }
.top-bar-inner { max-width: 1400px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; padding: 6px 24px; gap: 16px; }
.top-contact { display: flex; align-items: center; gap: 4px; }
.top-auth a { color: #E2E8F0; cursor: pointer; transition: color 0.2s; }
.top-auth a:hover { color: #3B82F6; }
.top-auth strong { color: #3B82F6; font-weight: 600; }
.sep { margin: 0 8px; color: #475569; }

/* ===== Header ===== */
.main-header { background: #fff; border-bottom: 1px solid #E2E8F0; position: sticky; top: 0; z-index: 100; }
.header-inner { max-width: 1400px; margin: 0 auto; display: flex; align-items: center; gap: 20px; padding: 10px 24px; }
.logo { display: flex; align-items: center; gap: 10px; cursor: pointer; flex-shrink: 0; }
.logo-icon {
  width: 42px; height: 42px; background: linear-gradient(135deg, #2563EB, #3B82F6);
  border-radius: 12px; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(37,99,235,0.3);
}
.logo-text { display: flex; flex-direction: column; line-height: 1; }
.logo-title { font-size: 20px; font-weight: 800; color: #0F172A; }
.logo-accent { color: #2563EB; }
.logo-sub { font-size: 10px; color: #94A3B8; letter-spacing: 1.5px; text-transform: uppercase; }

.header-search { flex: 1; max-width: 520px; }
.search-input :deep(.el-input__wrapper) { border-radius: 24px !important; padding: 4px 4px 4px 16px; background: #F1F5F9 !important; border: 2px solid transparent !important; box-shadow: none !important; transition: all 0.25s; height: 44px; }
.search-input :deep(.el-input__wrapper:hover) { background: #fff !important; border-color: #CBD5E1 !important; }
.search-input :deep(.el-input.is-focus .el-input__wrapper) { background: #fff !important; border-color: #2563EB !important; box-shadow: 0 0 0 3px rgba(37,99,235,0.1) !important; }
.search-input :deep(.el-input-group__append) { background: none; border: none; padding: 0; }
.search-btn { border-radius: 20px; font-weight: 600; padding: 10px 20px; }

.header-actions { display: flex; align-items: center; gap: 4px; flex-shrink: 0; }
.action-btn { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 6px 12px; cursor: pointer; border-radius: 10px; color: #475569; transition: all 0.2s; font-size: 11px; }
.action-btn:hover { background: #F1F5F9; color: #2563EB; }
.action-label { font-size: 11px; white-space: nowrap; }
.action-badge { display: flex; }
.avatar-btn { flex-direction: row; gap: 6px; padding: 4px 8px; }
.avatar-btn .action-label { font-size: 13px; font-weight: 500; }

/* ===== Navigation ===== */
.main-nav { background: #fff; border-bottom: 1px solid #F1F5F9; position: sticky; top: 64px; z-index: 99; }
.nav-inner { max-width: 1400px; margin: 0 auto; display: flex; align-items: center; gap: 20px; padding: 0 24px; }
.nav-categories { position: relative; padding: 8px 0; }
.cat-dropdown {
  position: absolute; top: 100%; left: 0; z-index: 200;
  background: #fff; border: 1px solid #E2E8F0; border-radius: 14px;
  min-width: 200px; padding: 6px; box-shadow: 0 20px 40px rgba(0,0,0,0.12);
}
.cat-item { display: flex; align-items: center; gap: 10px; padding: 10px 14px; cursor: pointer; font-size: 14px; color: #334155; border-radius: 8px; transition: all 0.15s; }
.cat-item:hover { background: #EFF6FF; color: #2563EB; }
.cat-emoji { font-size: 18px; }
.cat-arrow { margin-left: auto; color: #CBD5E1; font-size: 12px; }
.nav-links { display: flex; gap: 2px; }
.nav-link {
  padding: 12px 18px; font-size: 14px; font-weight: 500; color: #475569;
  cursor: pointer; border-radius: 6px; transition: all 0.2s; position: relative;
}
.nav-link:hover, .nav-link.active { color: #2563EB; background: #EFF6FF; }
.nav-link.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 24px; height: 3px; background: #2563EB; border-radius: 2px; }
.seckill-link { display: flex; align-items: center; gap: 6px; }
.hot-dot { width: 6px; height: 6px; border-radius: 50%; background: #EF4444; animation: dotPulse 1.5s infinite; }
@keyframes dotPulse { 0%,100%{opacity:1;transform:scale(1)} 50%{opacity:0.5;transform:scale(0.8)} }

/* Dropdown transition */
.fade-scale-enter-active { transition: all 0.2s ease; }
.fade-scale-leave-active { transition: all 0.15s ease; }
.fade-scale-enter-from, .fade-scale-leave-to { opacity: 0; transform: scale(0.95) translateY(-4px); }

/* ===== Main Content ===== */
.app-main { flex: 1; padding: 24px; max-width: 1400px; margin: 0 auto; width: 100%; padding-bottom: 80px; }

/* Page transition */
.page-fade-enter-active { transition: all 0.25s ease; }
.page-fade-leave-active { transition: all 0.15s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(8px); }
.page-fade-leave-to { opacity: 0; }

/* ===== Footer ===== */
.main-footer { background: #0F172A; margin-top: auto; }
.footer-inner { max-width: 1400px; margin: 0 auto; display: grid; grid-template-columns: 2fr 1fr 1fr 1.5fr; gap: 40px; padding: 48px 24px; }
.footer-col h4 { font-size: 15px; font-weight: 700; margin-bottom: 16px; color: #F8FAFC; position: relative; padding-bottom: 10px; }
.footer-col h4::after { content: ''; position: absolute; bottom: 0; left: 0; width: 28px; height: 3px; background: #2563EB; border-radius: 2px; }
.footer-col .about-text { font-size: 13px; color: #94A3B8; line-height: 1.8; }
.footer-col p, .footer-col a { font-size: 13px; color: #94A3B8; line-height: 2.2; display: block; cursor: pointer; }
.footer-col a:hover { color: #3B82F6; }
.footer-bottom { border-top: 1px solid #1E293B; text-align: center; padding: 16px 24px; font-size: 12px; color: #64748B; }

/* ===== Mobile Bottom Tabs ===== */
.mobile-tabs {
  display: none; position: fixed; bottom: 0; left: 0; right: 0; z-index: 200;
  background: rgba(255,255,255,0.92); backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid #E2E8F0; padding: 4px 0 max(4px, env(safe-area-inset-bottom));
  display: none; justify-content: space-around; align-items: center;
}
@media (max-width: 768px) {
  .mobile-tabs { display: flex; }
  .top-bar, .header-search, .main-nav, .main-footer { display: none; }
  .main-header { position: sticky; top: 0; }
  .header-actions .action-btn { display: none; }
  .header-actions .action-btn:first-child { display: flex; }
  .app-main { padding: 12px 12px 80px; }
  .logo-title { font-size: 17px; }
  .logo-sub { display: none; }
}
.mobile-tab { display: flex; flex-direction: column; align-items: center; gap: 2px; padding: 6px 12px; color: #94A3B8; cursor: pointer; transition: color 0.2s; font-size: 10px; flex: 1; text-align: center; }
.mobile-tab.active { color: #2563EB; }
.mobile-tab .el-icon { transition: transform 0.2s; }
.mobile-tab:active .el-icon { transform: scale(0.9); }
.cart-tab { position: relative; }
.mobile-cart-badge :deep(.el-badge__content) { border-color: transparent; }
</style>
