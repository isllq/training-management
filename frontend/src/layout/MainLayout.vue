<template>
  <div class="layout-root">
    <aside class="side-nav">
      <div class="brand">
        <div class="brand-mark">TM</div>
        <div class="brand-text">
          <div class="brand-title">实训管理系统</div>
          <small>Training Manager</small>
        </div>
      </div>

      <div class="menu-block-title">教学流程</div>
      <el-menu
        :default-active="activePath"
        class="menu"
        background-color="transparent"
        text-color="#d8e7f3"
        active-text-color="#ffffff"
        @select="go"
      >
        <el-menu-item v-for="item in teachingMenus" :key="item.path" :index="item.path">{{ item.title }}</el-menu-item>
      </el-menu>

      <template v-if="systemMenus.length">
        <div class="menu-block-title">系统治理</div>
        <el-menu
          :default-active="activePath"
          class="menu menu-second"
          background-color="transparent"
          text-color="#d8e7f3"
          active-text-color="#ffffff"
          @select="go"
        >
          <el-menu-item v-for="item in systemMenus" :key="item.path" :index="item.path">{{ item.title }}</el-menu-item>
        </el-menu>
      </template>
    </aside>

    <main class="main-area">
      <header class="top-bar">
        <div>
          <div class="title">{{ pageTitle }}</div>
          <div class="subtitle">教学过程数字化与实训闭环管理</div>
        </div>
        <div class="user">
          <span class="user-pill">{{ userTypeText }}</span>
          <span>{{ userName }}</span>
          <el-button link type="primary" @click="logout">退出登录</el-button>
        </div>
      </header>

      <section class="content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade-slide" mode="out-in">
            <component :is="Component" :key="viewKey" />
          </transition>
        </router-view>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ROLE, defaultHomeByRole, getUserInfo, getUserType, hasAnyRole } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const activePath = computed(() => route.path)
const viewKey = computed(() => route.fullPath)
const pageTitle = computed(() => route.meta.title || '实训管理系统')
const userInfo = computed(() => getUserInfo())
const userType = computed(() => getUserType())

const allTeachingMenus = [
  { path: '/dashboard', title: '系统看板', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] },
  { path: '/profile', title: '个人资料', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] },
  { path: '/announcements', title: '公告通知', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] },
  { path: '/projects', title: '项目管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] },
  { path: '/teams', title: '团队管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] },
  { path: '/tasks', title: '任务管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] },
  { path: '/scores', title: '成绩管理', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] },
  { path: '/qa', title: '在线答疑', roles: [ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT] }
]

const allSystemMenus = [
  { path: '/users', title: '用户管理', roles: [ROLE.ADMIN] },
  { path: '/rbac', title: '角色权限', roles: [ROLE.ADMIN] },
  { path: '/base-data', title: '基础数据', roles: [ROLE.ADMIN] },
  { path: '/system', title: '系统监控', roles: [ROLE.ADMIN] }
]

const teachingMenus = computed(() => allTeachingMenus.filter((item) => hasAnyRole(item.roles)))
const systemMenus = computed(() => allSystemMenus.filter((item) => hasAnyRole(item.roles)))

const userName = computed(() => userInfo.value.realName || userInfo.value.username || '用户')
const userTypeText = computed(() => {
  const t = userType.value
  if (t === ROLE.ADMIN) return '管理员'
  if (t === ROLE.TEACHER) return '指导教师'
  if (t === ROLE.STUDENT) return '学生'
  return '访客'
})

const go = (path) => router.push(path)

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

if (!hasAnyRole([ROLE.ADMIN, ROLE.TEACHER, ROLE.STUDENT])) {
  logout()
} else if (!teachingMenus.value.some((item) => item.path === route.path) &&
    !systemMenus.value.some((item) => item.path === route.path)) {
  router.replace(defaultHomeByRole(userType.value))
}
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  display: flex;
  animation: layoutAppear 420ms ease-out both;
}

.side-nav {
  width: 258px;
  background:
    radial-gradient(circle at 0% 0%, rgba(70, 132, 171, 0.2), transparent 40%),
    linear-gradient(180deg, #0f2f47 0%, #174766 100%);
  color: #fff;
  padding: 16px 12px;
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  transition: width 260ms ease, padding 260ms ease, box-shadow 260ms ease;
  box-shadow: inset -1px 0 0 rgba(255, 255, 255, 0.06);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding: 8px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  transition: background-color 220ms ease, transform 220ms ease;
}

.brand:hover {
  background: rgba(255, 255, 255, 0.14);
  transform: translateY(-1px);
}

.brand-mark {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.brand-title {
  font-weight: 800;
}

.brand-text small {
  opacity: 0.75;
}

.menu-block-title {
  margin-top: 12px;
  margin-bottom: 6px;
  padding-left: 10px;
  font-size: 12px;
  opacity: 0.65;
  letter-spacing: 1px;
}

:deep(.el-menu) {
  border-right: 0;
}

:deep(.el-menu-item) {
  border-radius: 10px;
  margin: 4px 0;
  transition: transform 180ms ease, background-color 180ms ease, box-shadow 220ms ease;
}

:deep(.el-menu-item:hover) {
  transform: translateX(2px);
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(62, 137, 186, 0.95), rgba(43, 103, 146, 0.95)) !important;
  box-shadow: 0 8px 16px rgba(12, 51, 79, 0.24);
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.top-bar {
  min-height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  background: rgba(255, 255, 255, 0.72);
  border-bottom: 1px solid #d7e2ea;
  backdrop-filter: blur(10px);
  transition: background-color 220ms ease, border-color 220ms ease;
}

.title {
  font-weight: 800;
  color: #162739;
  font-size: 20px;
}

.subtitle {
  margin-top: 2px;
  color: #597085;
  font-size: 12px;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #2e4355;
}

.user-pill {
  padding: 3px 10px;
  border-radius: 999px;
  background: #e1eef7;
  color: #1f5377;
  font-size: 12px;
  font-weight: 700;
  transition: transform 180ms ease, box-shadow 220ms ease;
}

.user-pill:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(26, 74, 110, 0.16);
}

.content {
  padding: 18px;
  position: relative;
  overflow-x: hidden;
}

@keyframes layoutAppear {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 980px) {
  .side-nav {
    width: 84px;
    padding: 12px 8px;
  }

  .brand-text,
  .menu-block-title {
    display: none;
  }

  :deep(.el-menu-item) {
    font-size: 0;
  }

  :deep(.el-menu-item::before) {
    content: '•';
    font-size: 18px;
  }
}
</style>
