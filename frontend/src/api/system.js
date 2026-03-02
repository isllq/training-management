import http from './http'

export const listLoginLogsApi = (params) => http.get('/system/login-logs', { params })
export const runtimeStatusApi = () => http.get('/system/runtime')
