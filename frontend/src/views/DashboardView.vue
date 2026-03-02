<template>
  <div class="dashboard">
    <div class="hero page-card">
      <div>
        <div class="hero-title">教学看板</div>
        <div class="hero-subtitle">{{ heroSubtitle }}</div>
      </div>
      <div class="hero-meta">{{ todayText }}</div>
    </div>

    <div class="metrics">
      <div class="metric-card">
        <div class="metric-label">未读公告</div>
        <div class="metric-value">{{ stats.unreadAnnouncementCount || 0 }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">7日内截止任务</div>
        <div class="metric-value">{{ deadlineSoonCount }}</div>
      </div>
      <div class="metric-card" v-if="!isStudentRole">
        <div class="metric-label">小组总数</div>
        <div class="metric-value">{{ stats.teamCount || 0 }}</div>
      </div>
      <div class="metric-card">
        <div class="metric-label">提交总数</div>
        <div class="metric-value">{{ stats.submissionCount || 0 }}</div>
      </div>
    </div>

    <div class="top-grid">
      <div class="page-card" v-if="isStudentRole">
        <div class="card-title">我的资料</div>
        <div class="profile-grid">
          <div class="item"><span>姓名</span><b>{{ profile.realName || '-' }}</b></div>
          <div class="item"><span>用户名</span><b>{{ profile.username || '-' }}</b></div>
          <div class="item"><span>角色</span><b>{{ roleText(profile.userType) }}</b></div>
          <div class="item"><span>班级</span><b>{{ profile.className || '-' }}</b></div>
          <div class="item"><span>手机</span><b>{{ profile.phone || '-' }}</b></div>
          <div class="item"><span>邮箱</span><b>{{ profile.email || '-' }}</b></div>
        </div>
      </div>

      <div class="page-card">
      <div class="card-title">当前待办</div>
      <div class="todo-list">
        <div class="todo-item todo-clickable" v-for="todo in todoList" :key="todo.label" @click="openTodo(todo)">
          <span>{{ todo.label }}</span>
          <b>{{ todo.value }}</b>
        </div>
      </div>
    </div>
    </div>

    <div class="bottom-grid">
      <div class="page-card">
        <div class="card-title">近期任务截止提醒</div>
        <el-table :data="upcomingTasks" border stripe>
          <el-table-column prop="title" label="任务标题" min-width="180" show-overflow-tooltip />
          <el-table-column label="阶段" width="90">
            <template #default="{ row }">{{ stageText(row.stageType) }}</template>
          </el-table-column>
          <el-table-column prop="deadline" label="截止时间" width="170" />
          <el-table-column prop="weight" label="权重(%)" width="90" />
        </el-table>
      </div>

      <div class="page-card">
        <div class="card-title">最新公告</div>
        <el-table :data="recentAnnouncements" border stripe>
          <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="发布人" width="120">
            <template #default="{ row }">{{ row.authorName || `用户${row.authorId}` }}</template>
          </el-table-column>
          <el-table-column prop="publishTime" label="发布时间" width="170" />
        </el-table>
      </div>
    </div>

    <div class="page-card" v-if="!isStudentRole">
      <div class="card-title">最近提交动态</div>
      <el-table :data="recentSubmissions" border stripe>
        <el-table-column label="任务" min-width="160">
          <template #default="{ row }">{{ taskName(row.taskId) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="提交说明" min-width="260" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'info'">{{ row.status === 2 ? '已批阅' : '待批阅' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardStatsApi } from '../api/dashboard'
import { listTasksApi } from '../api/tasks'
import { listAnnouncementsApi } from '../api/announcements'
import { listSubmissionsApi } from '../api/submissions'
import { meApi } from '../api/auth'
import { ROLE, hasAnyRole } from '../utils/auth'

const router = useRouter()

const isStudentRole = computed(() => hasAnyRole([ROLE.STUDENT]))
const isManageRole = computed(() => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER]))

const stats = ref({})
const profile = ref({})
const tasks = ref([])
const announcements = ref([])
const submissions = ref([])

const todayText = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
})

const heroSubtitle = computed(() =>
  isStudentRole.value ? '关注个人任务截止、公告通知和资料信息。' : '关注小组进度、任务截止与提交负载。'
)

const toTimestamp = (v) => {
  if (!v) return 0
  const t = new Date(v).getTime()
  return Number.isNaN(t) ? 0 : t
}

const upcomingTasks = computed(() => {
  const now = Date.now()
  return [...tasks.value]
    .filter((item) => item.status === 1 && toTimestamp(item.deadline) >= now)
    .sort((a, b) => toTimestamp(a.deadline) - toTimestamp(b.deadline))
    .slice(0, 8)
})

const deadlineSoonCount = computed(() => {
  const now = Date.now()
  const sevenDays = now + 7 * 24 * 60 * 60 * 1000
  return tasks.value.filter((item) => {
    const ts = toTimestamp(item.deadline)
    return item.status === 1 && ts >= now && ts <= sevenDays
  }).length
})

const overdueCount = computed(() => {
  const now = Date.now()
  return tasks.value.filter((item) => item.status === 1 && toTimestamp(item.deadline) > 0 && toTimestamp(item.deadline) < now).length
})

const recentAnnouncements = computed(() =>
  [...announcements.value]
    .sort((a, b) => toTimestamp(b.publishTime || b.createdAt) - toTimestamp(a.publishTime || a.createdAt))
    .slice(0, 8)
)

const recentSubmissions = computed(() =>
  [...submissions.value]
    .sort((a, b) => toTimestamp(b.submitTime) - toTimestamp(a.submitTime))
    .slice(0, 8)
)

const taskMap = computed(() => {
  const map = {}
  tasks.value.forEach((item) => {
    map[item.id] = item.title
  })
  return map
})

const taskName = (id) => taskMap.value[id] || `任务${id}`
const stageText = (stageType) => {
  if (stageType === 'OPENING') return '开题'
  if (stageType === 'MIDTERM') return '中期'
  if (stageType === 'FINAL') return '结题'
  if (stageType === 'DAILY') return '日常'
  return stageType || '-'
}

const todoList = computed(() => {
  if (isStudentRole.value) {
    return [
      { label: '未读公告', value: stats.value.unreadAnnouncementCount || 0, path: '/announcements' },
      { label: '7日内截止任务', value: deadlineSoonCount.value, path: '/tasks' },
      { label: '逾期未完成任务', value: overdueCount.value, path: '/tasks' }
    ]
  }
  return [
    { label: '小组总数', value: stats.value.teamCount || 0, path: '/teams' },
    { label: '未读公告', value: stats.value.unreadAnnouncementCount || 0, path: '/announcements' },
    { label: '7日内截止任务', value: deadlineSoonCount.value, path: '/tasks' },
    { label: '最近提交记录', value: recentSubmissions.value.length, path: '/tasks' }
  ]
})

const openTodo = (todo) => {
  if (!todo?.path) return
  router.push(todo.path)
}

const roleText = (userType) => {
  if (userType === 'ADMIN') return '管理员'
  if (userType === 'TEACHER') return '指导教师'
  if (userType === 'STUDENT') return '学生'
  return userType || '-'
}

const loadData = async () => {
  const [statsRes, meRes, taskRes, announcementRes, submissionRes] = await Promise.allSettled([
    dashboardStatsApi(),
    meApi(),
    listTasksApi({}),
    listAnnouncementsApi({}),
    isManageRole.value ? listSubmissionsApi({}) : Promise.resolve([])
  ])
  stats.value = statsRes.status === 'fulfilled' ? (statsRes.value || {}) : {}
  profile.value = meRes.status === 'fulfilled' ? (meRes.value || {}) : {}
  tasks.value = taskRes.status === 'fulfilled' ? (taskRes.value || []) : []
  announcements.value = announcementRes.status === 'fulfilled' ? (announcementRes.value || []) : []
  submissions.value = submissionRes.status === 'fulfilled' ? (submissionRes.value || []) : []
}

let refreshTimer = null
onMounted(async () => {
  await loadData()
  refreshTimer = setInterval(loadData, 30000)
  window.addEventListener('focus', loadData)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  window.removeEventListener('focus', loadData)
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background:
    radial-gradient(circle at 0% 0%, rgba(33, 99, 143, 0.16), transparent 40%),
    linear-gradient(120deg, #f5f9fc, #f9fcff);
}

.hero-title {
  font-size: 24px;
  font-weight: 800;
  color: #153047;
}

.hero-subtitle {
  margin-top: 4px;
  color: #537089;
}

.hero-meta {
  font-size: 13px;
  color: #1f557b;
  padding: 7px 12px;
  border-radius: 999px;
  background: #e4eff7;
  font-weight: 700;
}

.metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.metric-card {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid #cfe0ec;
  background: linear-gradient(150deg, #1d4f74 0%, #2a678f 100%);
  color: #f3f8fc;
}

.metric-label {
  opacity: 0.92;
  font-size: 13px;
}

.metric-value {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 800;
}

.top-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.card-title {
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 800;
  color: #1e3b54;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.profile-grid .item {
  padding: 10px;
  border-radius: 10px;
  background: #f5f9fc;
  border: 1px solid #d8e6f1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile-grid .item span {
  font-size: 12px;
  color: #5b7387;
}

.profile-grid .item b {
  color: #1f3f5a;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 10px;
  background: #f6fbff;
  border: 1px solid #d8e6f1;
}

.todo-item span {
  color: #4f677b;
}

.todo-item b {
  color: #1f5377;
}

.todo-clickable {
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.todo-clickable:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(26, 62, 94, 0.14);
}

@media (max-width: 980px) {
  .top-grid,
  .bottom-grid {
    grid-template-columns: 1fr;
  }

  .hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
