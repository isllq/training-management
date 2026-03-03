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
      <button type="button" class="metric-card metric-action" @click="openMetric('announcement')">
        <div class="metric-label">未读公告</div>
        <div class="metric-value">{{ stats.unreadAnnouncementCount || 0 }}</div>
        <div class="metric-foot">点击进入公告中心</div>
      </button>
      <button type="button" class="metric-card metric-action" @click="openMetric('task')">
        <div class="metric-label">7日内截止任务</div>
        <div class="metric-value">{{ deadlineSoonCount }}</div>
        <div class="metric-foot">点击进入任务中心</div>
      </button>
      <button v-if="!isStudentRole" type="button" class="metric-card metric-action" @click="openMetric('team')">
        <div class="metric-label">小组总数</div>
        <div class="metric-value">{{ stats.teamCount || 0 }}</div>
        <div class="metric-foot">点击进入团队管理</div>
      </button>
      <button type="button" class="metric-card metric-action" @click="openMetric('submission')">
        <div class="metric-label">{{ isStudentRole ? '我的提交' : '提交总数' }}</div>
        <div class="metric-value">{{ stats.submissionCount || 0 }}</div>
        <div class="metric-foot">{{ isStudentRole ? '点击进入任务中心' : '点击查看提交详情' }}</div>
      </button>
    </div>

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
import { listSubmissionsApi } from '../api/submissions'
import { meApi } from '../api/auth'
import { ROLE, hasAnyRole } from '../utils/auth'

const router = useRouter()

const isStudentRole = computed(() => hasAnyRole([ROLE.STUDENT]))
const isManageRole = computed(() => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER]))

const stats = ref({})
const profile = ref({})
const tasks = ref([])
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

const deadlineSoonCount = computed(() => {
  const now = Date.now()
  const sevenDays = now + 7 * 24 * 60 * 60 * 1000
  return tasks.value.filter((item) => {
    const ts = toTimestamp(item.deadline)
    return item.status === 1 && ts >= now && ts <= sevenDays
  }).length
})

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

const openMetric = (type) => {
  if (type === 'announcement') {
    router.push('/announcements')
    return
  }
  if (type === 'team') {
    router.push('/teams')
    return
  }
  router.push('/tasks')
}

const roleText = (userType) => {
  if (userType === 'ADMIN') return '管理员'
  if (userType === 'TEACHER') return '指导教师'
  if (userType === 'STUDENT') return '学生'
  return userType || '-'
}

const loadData = async () => {
  const [statsRes, meRes, taskRes, submissionRes] = await Promise.allSettled([
    dashboardStatsApi(),
    meApi(),
    listTasksApi({}),
    isManageRole.value ? listSubmissionsApi({}) : Promise.resolve([])
  ])
  stats.value = statsRes.status === 'fulfilled' ? (statsRes.value || {}) : {}
  profile.value = meRes.status === 'fulfilled' ? (meRes.value || {}) : {}
  tasks.value = taskRes.status === 'fulfilled' ? (taskRes.value || []) : []
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

.metric-action {
  cursor: pointer;
  text-align: left;
  transition: transform 0.16s ease, box-shadow 0.2s ease;
}

.metric-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px rgba(22, 60, 88, 0.18);
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

.metric-foot {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.9;
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

@media (max-width: 980px) {
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
