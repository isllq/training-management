import http from './http'

export const listTasksApi = (params) => http.get('/tasks', { params })
export const createTaskApi = (data) => http.post('/tasks', data)
export const updateTaskApi = (id, data) => http.put(`/tasks/${id}`, data)
export const deleteTaskApi = (id) => http.delete(`/tasks/${id}`)
