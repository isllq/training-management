import http from './http'

export const listCollegesApi = () => http.get('/base/colleges')
export const createCollegeApi = (data) => http.post('/base/colleges', data)
export const updateCollegeApi = (id, data) => http.put(`/base/colleges/${id}`, data)
export const deleteCollegeApi = (id) => http.delete(`/base/colleges/${id}`)

export const listMajorsApi = (params) => http.get('/base/majors', { params })
export const createMajorApi = (data) => http.post('/base/majors', data)
export const updateMajorApi = (id, data) => http.put(`/base/majors/${id}`, data)
export const deleteMajorApi = (id) => http.delete(`/base/majors/${id}`)

export const listClassesApi = (params) => http.get('/base/classes', { params })
export const createClassApi = (data) => http.post('/base/classes', data)
export const updateClassApi = (id, data) => http.put(`/base/classes/${id}`, data)
export const deleteClassApi = (id) => http.delete(`/base/classes/${id}`)
