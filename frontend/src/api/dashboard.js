import http from './http'

export const dashboardStatsApi = () => http.get('/dashboard/stats')
