<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">个人资料</div>
      <div class="module-head-desc">查看并维护个人基础信息，保障实训过程中的通知与任务触达。</div>
    </div>

    <div class="page-card profile-card">
      <el-form :model="form" label-width="90px" class="profile-form">
        <el-form-item label="用户名">
          <el-input :model-value="form.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-input :model-value="roleText(form.userType)" disabled />
        </el-form-item>
        <el-form-item label="所属班级">
          <el-input :model-value="form.className || '-'" disabled />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>

      <div class="actions">
        <el-button @click="loadProfile">重置</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { meApi, updateProfileApi } from '../api/auth'

const saving = ref(false)
const form = reactive({
  username: '',
  realName: '',
  userType: '',
  className: '',
  phone: '',
  email: ''
})

const roleText = (userType) => {
  if (userType === 'ADMIN') return '管理员'
  if (userType === 'TEACHER') return '指导教师'
  if (userType === 'STUDENT') return '学生'
  return userType || '-'
}

const loadProfile = async () => {
  const user = await meApi()
  form.username = user?.username || ''
  form.realName = user?.realName || ''
  form.userType = user?.userType || ''
  form.className = user?.className || ''
  form.phone = user?.phone || ''
  form.email = user?.email || ''
}

const saveProfile = async () => {
  if (!form.realName) {
    ElMessage.warning('姓名不能为空')
    return
  }
  saving.value = true
  try {
    await updateProfileApi({
      realName: form.realName,
      phone: form.phone,
      email: form.email
    })
    const userInfoText = localStorage.getItem('userInfo')
    if (userInfoText) {
      const userInfo = JSON.parse(userInfoText)
      userInfo.realName = form.realName
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
    ElMessage.success('个人资料已更新')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.profile-card {
  max-width: 720px;
}

.profile-form {
  width: 100%;
}

.actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
