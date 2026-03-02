<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">用户管理</div>
      <div class="module-head-desc">支持用户查询、账号维护、密码重置与 Excel 批量导入。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="按用户名/姓名搜索" clearable @keyup.enter="loadUsers" />
        <el-select v-model="query.className" placeholder="按班级筛选" clearable filterable @change="loadUsers">
          <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.sortBy" placeholder="排序方式" @change="loadUsers">
          <el-option label="ID顺序" value="ID_ASC" />
          <el-option label="班级排序" value="CLASS_ASC" />
          <el-option label="姓名首字母" value="NAME_INITIAL_ASC" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增用户</el-button>
        <el-upload
          :show-file-list="false"
          :auto-upload="false"
          :on-change="onImportFile"
          accept=".xlsx"
        >
          <el-button>Excel导入</el-button>
        </el-upload>
        <el-button @click="loadUsers">查询</el-button>
      </div>

      <el-table :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="userType" label="角色" />
        <el-table-column prop="className" label="班级" />
        <el-table-column prop="phone" label="手机" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="resetPwd(row.id)">重置密码</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="520px">
        <el-form :model="form" label-width="90px" class="dialog-form">
          <el-form-item label="用户名">
            <el-input v-model="form.username" :disabled="!!form.id" />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="form.realName" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="form.userType">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="指导教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="form.userType === 'STUDENT'" label="所属班级">
            <el-select v-model="form.className" placeholder="请选择班级" filterable clearable>
              <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="!form.id" label="初始密码">
            <el-input v-model="form.password" placeholder="默认123456" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createUserApi,
  deleteUserApi,
  listUsersApi,
  resetUserPasswordApi,
  updateUserApi,
  importUsersApi
} from '../api/users'
import { listClassesApi } from '../api/base'

const query = reactive({ keyword: '', className: '', sortBy: 'ID_ASC' })
const tableData = ref([])
const classOptions = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  username: '',
  realName: '',
  userType: 'STUDENT',
  phone: '',
  email: '',
  className: '',
  status: 1,
  password: '123456'
})

const resetForm = () => {
  form.id = null
  form.username = ''
  form.realName = ''
  form.userType = 'STUDENT'
  form.phone = ''
  form.email = ''
  form.className = ''
  form.status = 1
  form.password = '123456'
}

const loadUsers = async () => {
  tableData.value = await listUsersApi(query)
}

const loadClassOptions = async () => {
  const classes = await listClassesApi({})
  classOptions.value = classes.map((item) => ({ value: item.className, label: item.className }))
}

const openCreate = () => {
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.userType = row.userType
  form.phone = row.phone
  form.email = row.email
  form.className = row.className || ''
  form.status = row.status
  form.password = '123456'
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.username || !form.realName) {
    ElMessage.warning('用户名和姓名不能为空')
    return
  }
  const payload = {
    username: form.username,
    realName: form.realName,
    userType: form.userType,
    phone: form.phone,
    email: form.email,
    className: form.userType === 'STUDENT' ? form.className : '',
    status: form.status
  }
  if (form.id) {
    await updateUserApi(form.id, payload)
  } else {
    await createUserApi(payload, form.password || '123456')
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await loadUsers()
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该用户吗？', '提示', { type: 'warning' })
  await deleteUserApi(id)
  ElMessage.success('删除成功')
  await loadUsers()
}

const resetPwd = async (id) => {
  await ElMessageBox.confirm('将密码重置为 123456，是否继续？', '提示', { type: 'warning' })
  await resetUserPasswordApi(id, '123456')
  ElMessage.success('重置成功')
}

const onImportFile = async (file) => {
  const formData = new FormData()
  formData.append('file', file.raw)
  const result = await importUsersApi(formData)
  ElMessage.success(`导入完成，成功 ${result.successCount} 条`)
  await loadUsers()
}

onMounted(async () => {
  await loadClassOptions()
  await loadUsers()
})
</script>
