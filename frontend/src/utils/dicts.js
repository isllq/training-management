export const toNameMap = (list = [], idKey = 'id', nameKey = 'name') => {
  const map = {}
  list.forEach((item) => {
    if (item && item[idKey] != null) {
      map[item[idKey]] = item[nameKey] || ''
    }
  })
  return map
}

export const publishLabel = (item) => {
  if (!item) return ''
  const project = item.projectName || ''
  const term = item.termName || ''
  const clazz = item.className || ''
  if (project && (term || clazz)) {
    return `${project}｜${clazz || '未设班级'}｜${term || '未设学期'}`
  }
  if (project) return project
  if (term || clazz) {
    return `${clazz || '未设班级'}｜${term || '未设学期'}`
  }
  return `开设#${item.id}`
}

export const fallbackName = (id, map, prefix) => {
  if (id == null) return '-'
  if (map && map[id]) return map[id]
  return `${prefix}${id}`
}
