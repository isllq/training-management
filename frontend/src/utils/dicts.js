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
  const clazz = item.className || ''
  if (project && clazz) return `${project} · ${clazz}`
  if (project) return project
  if (clazz) return clazz
  return '开设计划'
}

export const fallbackName = (id, map, prefix) => {
  if (id == null) return '-'
  if (map && map[id]) return map[id]
  if (prefix === '开设') return '开设计划'
  return `${prefix}${id}`
}
