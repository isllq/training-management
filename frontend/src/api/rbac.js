import http from './http'

export const listRolesApi = () => http.get('/rbac/roles')
export const createRoleApi = (data) => http.post('/rbac/roles', data)
export const updateRoleApi = (id, data) => http.put(`/rbac/roles/${id}`, data)
export const deleteRoleApi = (id) => http.delete(`/rbac/roles/${id}`)

export const listPermissionsApi = () => http.get('/rbac/permissions')
export const createPermissionApi = (data) => http.post('/rbac/permissions', data)
export const updatePermissionApi = (id, data) => http.put(`/rbac/permissions/${id}`, data)
export const deletePermissionApi = (id) => http.delete(`/rbac/permissions/${id}`)

export const listUserRoleIdsApi = (userId) => http.get(`/rbac/users/${userId}/roles`)
export const bindUserRolesApi = (userId, roleIds) => http.put(`/rbac/users/${userId}/roles`, { roleIds })

export const listRolePermissionIdsApi = (roleId) => http.get(`/rbac/roles/${roleId}/permissions`)
export const bindRolePermissionsApi = (roleId, permissionIds) =>
  http.put(`/rbac/roles/${roleId}/permissions`, { permissionIds })
