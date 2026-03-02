import http from './http'

export const uploadFileApi = (formData, bizType, bizId) =>
  http.post(`/files/upload?bizType=${encodeURIComponent(bizType)}${bizId ? `&bizId=${bizId}` : ''}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })

export const listFilesApi = (params) => http.get('/files', { params })
export const deleteFileApi = (id) => http.delete(`/files/${id}`)

export const downloadFileApi = async (id, fallbackName = 'download.bin') => {
  const token = localStorage.getItem('token')
  const response = await fetch(`/api/files/${id}/download`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
  if (!response.ok) {
    throw new Error('下载失败')
  }
  const blob = await response.blob()
  const url = window.URL.createObjectURL(blob)
  const disposition = response.headers.get('content-disposition') || ''
  const matched = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  const fileName = matched && matched[1] ? decodeURIComponent(matched[1]) : fallbackName
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
  window.URL.revokeObjectURL(url)
}
