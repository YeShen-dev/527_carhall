import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue') },
  { path: '/', name: 'Home', component: () => import('../views/HomeView.vue') },
  { path: '/seckill', name: 'Seckill', component: () => import('../views/SeckillView.vue') },
  { path: '/products/:id', name: 'ProductDetail', component: () => import('../views/ProductDetailView.vue') },
  { path: '/help', name: 'HelpCenter', component: () => import('../views/HelpCenterView.vue') },
  { path: '/return-policy', name: 'ReturnPolicy', component: () => import('../views/ReturnPolicyView.vue') },
  { path: '/shipping', name: 'ShippingInfo', component: () => import('../views/ShippingInfoView.vue') },
  { path: '/contact', name: 'ContactUs', component: () => import('../views/ContactUsView.vue') },
  {
    path: '/payment/:id',
    name: 'Payment',
    component: () => import('../views/PaymentView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/payment/return',
    name: 'PaymentReturn',
    component: () => import('../views/PaymentReturnView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/OrdersView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/products' },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/AdminProducts.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/AdminOrders.vue') },
      { path: 'seckill', name: 'AdminSeckill', component: () => import('../views/admin/AdminSeckill.vue') },
      { path: 'comments', name: 'AdminComments', component: () => import('../views/admin/AdminComments.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function isTokenExpired(t) {
  if (!t) return true
  try {
    const payload = JSON.parse(atob(t.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch { return true }
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  if (token && isTokenExpired(token)) {
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    if (to.meta.requiresAuth) return next('/login')
    return next()
  }

  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  if (to.meta.requiresAdmin && userInfo?.role !== 'ADMIN') {
    return next('/')
  }

  if ((to.path === '/login' || to.path === '/register') && token) {
    return next('/')
  }

  next()
})

export default router
