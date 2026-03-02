import http from './http'

export const listUsersApi = (params) => http.get('/users', { params })
export const createUserApi = (data, password) =>
  http.post(`/users${password ? `?password=${encodeURIComponent(password)}` : ''}`, data)
export const updateUserApi = (id, data) => http.put(`/users/${id}`, data)
export const deleteUserApi = (id) => http.delete(`/users/${id}`)
export const resetUserPasswordApi = (id, password) => http.put(`/users/${id}/password`, { password })
export const importUsersApi = (formData) =>
  http.post('/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
