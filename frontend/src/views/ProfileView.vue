<template>
  <div class="profile-page">
    <!-- Avatar + Stats -->
    <div class="pf-top">
      <div class="pf-card pf-avatar-card">
        <AvatarUploader
          :model-value="user?.avatar || ''"
          :initials="user?.username?.charAt(0)?.toUpperCase() || 'U'"
          @update:model-value="avatar = $event; handleAvatarChange($event)"
        />
        <div class="pf-user-info">
          <h2>{{ user?.username }}</h2>
          <span class="pf-role" :class="{ admin: user?.role === 'ADMIN' }">
            {{ user?.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </span>
        </div>
      </div>
      <div class="pf-card pf-stats">
        <div class="pf-stat"><span class="pfs-num">0</span><span class="pfs-label">待付款</span></div>
        <div class="pf-stat"><span class="pfs-num">0</span><span class="pfs-label">待发货</span></div>
        <div class="pf-stat"><span class="pfs-num">0</span><span class="pfs-label">待收货</span></div>
        <div class="pf-stat"><span class="pfs-num">0</span><span class="pfs-label">已完成</span></div>
      </div>
    </div>

    <!-- Info Edit -->
    <div class="pf-card">
      <h3>基本信息</h3>
      <el-form v-if="user" :model="form" label-width="70px">
        <el-form-item label="用户名"><el-input :model-value="user.username" disabled /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSave" :loading="saving">保存修改</el-button></el-form-item>
      </el-form>
    </div>

    <!-- Password Change - uses reusable component -->
    <div class="pf-card">
      <h3>修改密码</h3>
      <PasswordChangeForm ref="pwdFormRef" @submit="handleChangePwd" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user.js'
import AvatarUploader from '../components/AvatarUploader.vue'
import PasswordChangeForm from '../components/PasswordChangeForm.vue'

const userStore = useUserStore()
const user = ref(null)
const avatar = ref('')
const saving = ref(false)
const pwdFormRef = ref(null)
const form = reactive({ phone: '', email: '' })

onMounted(async () => {
  user.value = await userStore.fetchUserInfo()
  form.phone = user.value.phone || ''
  form.email = user.value.email || ''
  avatar.value = user.value.avatar || ''
})

async function handleAvatarChange(url) {
  try { await userStore.updateProfile({ avatar: url }); user.value.avatar = url }
  catch { ElMessage.error('头像更新失败') }
}

async function handleSave() {
  saving.value = true
  try { await userStore.updateProfile({ phone: form.phone, email: form.email }); ElMessage.success('保存成功') }
  catch { } finally { saving.value = false }
}

async function handleChangePwd({ oldPassword, newPassword }) {
  await userStore.updatePassword(oldPassword, newPassword)
  pwdFormRef.value?.reset()
}
</script>

<style scoped>
.profile-page { max-width: 680px; margin: 0 auto; display: flex; flex-direction: column; gap: 20px; }
.pf-top { display: flex; gap: 20px; align-items: stretch; }
.pf-card { background: #fff; border-radius: 16px; padding: 24px; border: 1px solid #F1F5F9; box-shadow: 0 1px 3px rgba(0,0,0,.04); }
.pf-card h3 { font-size: 16px; font-weight: 700; color: #0F172A; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #F1F5F9; }
.pf-avatar-card { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 16px; }
.pf-user-info { text-align: center; }
.pf-user-info h2 { font-size: 20px; font-weight: 700; color: #0F172A; margin-bottom: 4px; }
.pf-role { font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 10px; background: #EFF6FF; color: #2563EB; }
.pf-role.admin { background: #FEF2F2; color: #EF4444; }
.pf-stats { flex: 1; display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.pf-stat { display: flex; flex-direction: column; align-items: center; gap: 4px; justify-content: center; }
.pfs-num { font-size: 28px; font-weight: 800; color: #0F172A; }
.pfs-label { font-size: 12px; color: #94A3B8; }
@media (max-width: 520px) { .pf-top { flex-direction: column; } }
</style>
