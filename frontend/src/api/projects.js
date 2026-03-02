import http from './http'

export const listProjectsApi = (params) => http.get('/projects', { params })
export const createProjectApi = (data) => http.post('/projects', data)
export const updateProjectApi = (id, data) => http.put(`/projects/${id}`, data)
export const deleteProjectApi = (id) => http.delete(`/projects/${id}`)

export const listPublishesApi = (params) => http.get('/projects/publishes', { params })
export const createPublishApi = (data) => http.post('/projects/publishes', data)
export const updatePublishApi = (id, data) => http.put(`/projects/publishes/${id}`, data)
export const deletePublishApi = (id) => http.delete(`/projects/publishes/${id}`)
