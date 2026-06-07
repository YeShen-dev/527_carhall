<template>
  <div class="password-change-form">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" @submit.prevent="handleSubmit">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码 (至少6位)" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="warning" native-type="submit" :loading="loading" class="pcf-btn">修改密码</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['submit'])
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await emit('submit', { ...form })
    form.oldPassword = ''; form.newPassword = ''; form.confirmPassword = ''
    formRef.value.resetFields()
    ElMessage.success('密码修改成功')
  } catch { } finally { loading.value = false }
}

function reset() { formRef.value?.resetFields() }
defineExpose({ reset })
</script>

<style scoped>
.pcf-btn { width: 100%; border-radius: 10px; font-weight: 600; height: 44px; }
</style>
