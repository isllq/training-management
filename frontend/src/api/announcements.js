import http from './http'

export const listAnnouncementsApi = (params) => http.get('/announcements', { params })
export const createAnnouncementApi = (data) => http.post('/announcements', data)
export const updateAnnouncementApi = (id, data) => http.put(`/announcements/${id}`, data)
export const deleteAnnouncementApi = (id) => http.delete(`/announcements/${id}`)
export const markAnnouncementReadApi = (id) => http.put(`/announcements/${id}/read`)
export const unreadAnnouncementCountApi = () => http.get('/announcements/unread-count')
