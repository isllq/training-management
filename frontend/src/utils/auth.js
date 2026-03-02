export const ROLE = {
  ADMIN: 'ADMIN',
  TEACHER: 'TEACHER',
  STUDENT: 'STUDENT'
}

export const getUserInfo = () => {
  const text = localStorage.getItem('userInfo')
  if (!text) return {}
  try {
    return JSON.parse(text)
  } catch (error) {
    return {}
  }
}

export const getUserType = () => getUserInfo().userType || ''

export const getUserId = () => getUserInfo().userId || null

export const hasAnyRole = (roles = []) => {
  const current = getUserType()
  return roles.includes(current)
}

export const isAdmin = () => hasAnyRole([ROLE.ADMIN])

export const isTeacher = () => hasAnyRole([ROLE.TEACHER])

export const isStudent = () => hasAnyRole([ROLE.STUDENT])

export const canManageTeaching = () => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER])

export const canManageSystem = () => isAdmin()

export const defaultHomeByRole = (role) => {
  if (role === ROLE.ADMIN) return '/dashboard'
  if (role === ROLE.TEACHER) return '/dashboard'
  if (role === ROLE.STUDENT) return '/dashboard'
  return '/login'
}
