<template>
  <div class="rbac-page">
    <div class="module-head page-card">
      <div class="module-head-title">角色权限</div>
      <div class="module-head-desc">统一维护角色、权限点与绑定关系，支撑权限治理与审计。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-button type="primary" @click="openRoleCreate">新增角色</el-button>
        <el-button @click="loadRoles">刷新</el-button>
      </div>
      <el-table :data="roles" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roleCode" label="角色编码" width="140" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" @click="openRoleEdit(row)">编辑</el-button>
            <el-button size="small" @click="openBindPerm(row)">绑定权限</el-button>
            <el-button size="small" type="danger" @click="removeRole(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-button type="primary" @click="openPermCreate">新增权限</el-button>
        <el-button @click="loadPermissions">刷新</el-button>
      </div>
      <el-table :data="permissions" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="permCode" label="权限编码" width="180" />
        <el-table-column prop="permName" label="权限名称" width="180" />
        <el-table-column prop="permType" label="类型" width="100" />
        <el-table-column prop="path" label="路径" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openPermEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removePermission(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="roleDialog" :title="roleForm.id ? '编辑角色' : '新增角色'" width="520px">
      <el-form :model="roleForm" label-width="90px" class="dialog-form">
        <el-form-item label="角色编码"><el-input v-model="roleForm.roleCode" :disabled="!!roleForm.id" /></el-form-item>
        <el-form-item label="角色名称"><el-input v-model="roleForm.roleName" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRole">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permDialog" :title="permForm.id ? '编辑权限' : '新增权限'" width="560px">
      <el-form :model="permForm" label-width="90px" class="dialog-form">
        <el-form-item label="权限编码"><el-input v-model="permForm.permCode" :disabled="!!permForm.id" /></el-form-item>
        <el-form-item label="权限名称"><el-input v-model="permForm.permName" /></el-form-item>
        <el-form-item label="权限类型"><el-input v-model="permForm.permType" /></el-form-item>
        <el-form-item label="路径"><el-input v-model="permForm.path" /></el-form-item>
        <el-form-item label="方法"><el-input v-model="permForm.method" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPermission">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindPermDialog" title="绑定角色权限" width="560px">
      <el-checkbox-group v-model="bindPermissionIds">
        <el-checkbox v-for="perm in permissions" :key="perm.id" :label="perm.id">{{ perm.permName }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="bindPermDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBindPerm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  bindRolePermissionsApi,
  createPermissionApi,
  createRoleApi,
  deletePermissionApi,
  deleteRoleApi,
  listPermissionsApi,
  listRolePermissionIdsApi,
  listRolesApi,
  updatePermissionApi,
  updateRoleApi
} from '../api/rbac'

const roles = ref([])
const permissions = ref([])

const roleDialog = ref(false)
const roleForm = reactive({ id: null, roleCode: '', roleName: '', status: 1 })

const permDialog = ref(false)
const permForm = reactive({ id: null, permCode: '', permName: '', permType: 'API', path: '', method: 'GET', status: 1 })

const bindPermDialog = ref(false)
const bindRoleId = ref(null)
const bindPermissionIds = ref([])

const loadRoles = async () => {
  roles.value = await listRolesApi()
}

const loadPermissions = async () => {
  permissions.value = await listPermissionsApi()
}

const openRoleCreate = () => {
  Object.assign(roleForm, { id: null, roleCode: '', roleName: '', status: 1 })
  roleDialog.value = true
}

const openRoleEdit = (row) => {
  Object.assign(roleForm, row)
  roleDialog.value = true
}

const submitRole = async () => {
  if (roleForm.id) await updateRoleApi(roleForm.id, roleForm)
  else await createRoleApi(roleForm)
  roleDialog.value = false
  ElMessage.success('保存成功')
  await loadRoles()
}

const removeRole = async (id) => {
  await ElMessageBox.confirm('确认删除该角色吗？', '提示', { type: 'warning' })
  await deleteRoleApi(id)
  ElMessage.success('删除成功')
  await loadRoles()
}

const openPermCreate = () => {
  Object.assign(permForm, {
    id: null,
    permCode: '',
    permName: '',
    permType: 'API',
    path: '',
    method: 'GET',
    status: 1
  })
  permDialog.value = true
}

const openPermEdit = (row) => {
  Object.assign(permForm, row)
  permDialog.value = true
}

const submitPermission = async () => {
  if (permForm.id) await updatePermissionApi(permForm.id, permForm)
  else await createPermissionApi(permForm)
  permDialog.value = false
  ElMessage.success('保存成功')
  await loadPermissions()
}

const removePermission = async (id) => {
  await ElMessageBox.confirm('确认删除该权限吗？', '提示', { type: 'warning' })
  await deletePermissionApi(id)
  ElMessage.success('删除成功')
  await loadPermissions()
}

const openBindPerm = async (row) => {
  bindRoleId.value = row.id
  bindPermissionIds.value = await listRolePermissionIdsApi(row.id)
  bindPermDialog.value = true
}

const submitBindPerm = async () => {
  await bindRolePermissionsApi(bindRoleId.value, bindPermissionIds.value)
  ElMessage.success('绑定成功')
  bindPermDialog.value = false
}

onMounted(async () => {
  await loadRoles()
  await loadPermissions()
})
</script>

<style scoped>
.rbac-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
