import http from './http'

export const listScoresApi = (params) => http.get('/scores', { params })
export const getScoreFormulaApi = () => http.get('/scores/formula')
export const createScoreApi = (data) => http.post('/scores', data)
export const updateScoreApi = (id, data) => http.put(`/scores/${id}`, data)
export const deleteScoreApi = (id) => http.delete(`/scores/${id}`)
export const exportScoresUrl = (publishId) =>
  `/api/scores/export${publishId ? `?publishId=${publishId}` : ''}`
