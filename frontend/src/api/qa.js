import http from './http'

export const listQaThreadsApi = (params) => http.get('/qa/threads', { params })
export const createQaThreadApi = (data) => http.post('/qa/threads', data)
export const updateQaThreadApi = (id, data) => http.put(`/qa/threads/${id}`, data)
export const deleteQaThreadApi = (id) => http.delete(`/qa/threads/${id}`)

export const listQaRepliesApi = (threadId) => http.get(`/qa/threads/${threadId}/replies`)
export const createQaReplyApi = (threadId, data) => http.post(`/qa/threads/${threadId}/replies`, data)
export const deleteQaReplyApi = (id) => http.delete(`/qa/replies/${id}`)
