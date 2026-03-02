import http from './http'

export const listSubmissionsApi = (params) => http.get('/submissions', { params })
export const createSubmissionApi = (data) => http.post('/submissions', data)
export const updateSubmissionApi = (id, data) => http.put(`/submissions/${id}`, data)
export const deleteSubmissionApi = (id) => http.delete(`/submissions/${id}`)
