import http from './http'

export const loginApi = (data) => http.post('/auth/login', data)
export const meApi = () => http.get('/auth/me')
export const updateProfileApi = (data) => http.put('/auth/profile', data)
export const listUserOptionsApi = (params) => http.get('/auth/user-options', { params })
