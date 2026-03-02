import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainLayout from '../layout/MainLayout.vue'
import DashboardView from '../views/DashboardView.vue'
import UserView from '../views/UserView.vue'
import ProjectView from '../views/ProjectView.vue'
import TeamView from '../views/TeamView.vue'
import TaskView from '../views/TaskView.vue'
import ScoreView from '../views/ScoreView.vue'
import BaseDataView from '../views/BaseDataView.vue'
import QaView from '../views/QaView.vue'
import SystemView from '../views/SystemView.vue'
import RbacView from '../views/RbacView.vue'
import AnnouncementView from '../views/AnnouncementView.vue'
import ProfileView from '../views/ProfileView.vue'
import { ROLE, defaultHomeByRole, getUserType } from '../utils/auth'

const routes = [
  { path: '/login', component: LoginView, meta: { public: true } },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: DashboardView, meta: { title: '系统看板', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'profile', component: ProfileView, meta: { title: '个人资料', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'announcements', component: AnnouncementView, meta: { title: '公告通知', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'users', component: UserView, meta: { title: '用户管理', roles: [ROLE.ADMIN] } },
      { path: 'projects', component: ProjectView, meta: { title: '项目管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'teams', component: TeamView, meta: { title: '团队管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'tasks', component: TaskView, meta: { title: '任务管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'scores', component: ScoreView, meta: { title: '成绩管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'base-data', component: BaseDataView, meta: { title: '基础数据', roles: [ROLE.ADMIN] } },
      { path: 'rbac', component: RbacView, meta: { title: '角色权限', roles: [ROLE.ADMIN] } },
      { path: 'qa', component: QaView, meta: { title: '在线答疑', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] } },
      { path: 'system', component: SystemView, meta: { title: '系统监控', roles: [ROLE.ADMIN] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userType = getUserType()

  if (!to.meta.public && !token) {
    next('/login')
    return
  }

  if (to.path === '/login' && token) {
    next(defaultHomeByRole(userType))
    return
  }

  const allowRoles = to.meta.roles
  if (allowRoles && allowRoles.length > 0 && !allowRoles.includes(userType)) {
    next(defaultHomeByRole(userType))
    return
  }

  next()
})

export default router
