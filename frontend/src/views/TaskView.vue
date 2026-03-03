<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">实训任务中心</div>
      <div class="module-head-desc">任务发布、学生提交、教师跟踪在同一页面闭环完成。</div>
    </div>

    <div class="page-card task-stage-card">
      <div class="task-stage-head">
        <div class="task-stage-filter">
          <span class="task-stage-label">当前学期</span>
          <el-select v-model="query.termName" placeholder="请选择学期" clearable filterable @change="onTermChange">
            <el-option v-for="item in termOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="task-stage-kpis" v-if="query.termName">
          <div class="task-kpi-item">
            <div class="task-kpi-label">项目数</div>
            <div class="task-kpi-value">{{ projectCards.length }}</div>
          </div>
          <div class="task-kpi-item">
            <div class="task-kpi-label">任务数</div>
            <div class="task-kpi-value">{{ termTaskCount }}</div>
          </div>
          <div class="task-kpi-item">
            <div class="task-kpi-label">临近截止</div>
            <div class="task-kpi-value">{{ urgentTaskCount }}</div>
          </div>
        </div>
      </div>

      <el-empty
        v-if="!query.termName"
        description="请先选择学期，再查看该学期全部项目与任务"
        :image-size="64"
      />
      <div v-else class="project-card-grid">
        <button
          v-for="item in projectCards"
          :key="item.projectId"
          type="button"
          class="project-card"
          :class="{ active: item.projectId === activeProjectId }"
          @click="selectProject(item.projectId)"
        >
          <div class="project-card-title">{{ item.projectName }}</div>
          <div class="project-card-meta">{{ item.classCount }}个班级 · {{ item.publishCount }}个开设</div>
          <div class="project-card-stats">
            <span>任务 {{ item.taskCount }}</span>
            <span>进行中 {{ item.activeCount }}</span>
            <span>已截止 {{ item.overdueCount }}</span>
          </div>
        </button>
      </div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <div class="project-focus-title">
          {{ activeProjectCard ? `项目任务：${activeProjectCard.projectName}` : '请先选择上方项目卡片' }}
        </div>
        <el-button v-if="canManageTask" type="primary" :disabled="!activeProjectCard" @click="openCreate">新增任务</el-button>
      </div>

      <el-table :data="projectTasks" border stripe>
        <el-table-column label="实训项目" min-width="160">
          <template #default="{ row }">{{ row.projectName || projectNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="所属学期" width="130">
          <template #default="{ row }">{{ row.termName || termNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="适用班级" width="120">
          <template #default="{ row }">{{ row.className || classNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="任务阶段" width="100">
          <template #default="{ row }">{{ stageText(row.stageType) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="任务标题" min-width="180" />
        <el-table-column prop="deadline" label="截止时间" width="170" />
        <el-table-column label="剩余时间" width="140">
          <template #default="{ row }">
            <el-tag :type="remainTagType(row.deadline)">{{ remainText(row.deadline) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务状态" width="110">
          <template #default="{ row }">
            <el-tag :type="taskStatusType(row)">{{ taskStatusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canManageTask" label="提交进度" width="220">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-tag :type="progressTagType(row.completionRate)">
                {{ row.submittedGroupCount || 0 }}/{{ row.expectedGroupCount || 0 }}（{{ row.completionRate || 0 }}%）
              </el-tag>
              <el-button link type="primary" @click="openSubmissionDrawer(row)">查看详情</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="评分权重(%)" width="110" />
        <el-table-column :width="canManageTask ? 360 : 250" label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="isStudentRole && canOperateSubmission(row)" size="small" type="primary" @click="openSubmitDialog(row)">
              {{ submitButtonText(row) }}
            </el-button>
            <el-button v-if="canManageTask" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canManageTask" size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-if="canManageTask"
      v-model="dialogVisible"
      :title="form.id ? '编辑任务' : '新增任务'"
      width="700px"
      append-to-body
    >
      <el-form :model="form" label-width="95px" class="dialog-form">
        <el-form-item label="所属项目班级">
          <el-select v-model="form.publishId" filterable placeholder="请选择项目/班级">
            <el-option v-for="item in dialogPublishOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="任务阶段">
          <el-select v-model="form.stageType">
            <el-option label="开题" value="OPENING" />
            <el-option label="中期" value="MIDTERM" />
            <el-option label="结题" value="FINAL" />
            <el-option label="日常" value="DAILY" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务说明"><el-input v-model="form.content" type="textarea" /></el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.deadline"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="评分权重">
          <el-input-number v-model="form.weight" :min="0" :max="100" />
        </el-form-item>

        <el-form-item label="任务附件">
          <div class="attachment-panel">
            <el-upload
              v-model:file-list="taskUploadFileList"
              :auto-upload="false"
              :show-file-list="true"
              :on-change="onTaskFileSelect"
              :on-remove="onTaskFileRemove"
              multiple
            >
              <el-button type="primary">选择附件</el-button>
            </el-upload>
            <div class="attachment-tip">保存任务时会自动上传所选附件。</div>

            <el-table v-if="taskFiles.length" :data="taskFiles" border stripe style="margin-top: 10px">
              <el-table-column prop="originalName" label="已上传附件" min-width="220" show-overflow-tooltip />
              <el-table-column prop="uploadedAt" label="上传时间" width="170" />
              <el-table-column label="操作" width="130">
                <template #default="{ row }">
                  <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
                  <el-button link type="danger" @click="deleteTaskFile(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-if="isStudentRole"
      v-model="submitDialogVisible"
      :title="submitForm.id ? '修改任务提交' : '任务提交'"
      width="700px"
      append-to-body
    >
      <el-form :model="submitForm" label-width="95px" class="dialog-form">
        <el-form-item label="任务名称">
          <el-input :model-value="currentSubmitTask ? currentSubmitTask.title : ''" disabled />
        </el-form-item>
        <el-form-item label="提交说明">
          <el-input v-model="submitForm.content" type="textarea" rows="5" placeholder="请填写本次提交的核心内容与完成情况" />
        </el-form-item>
        <el-form-item label="参考链接">
          <el-input v-model="submitForm.fileUrl" placeholder="可填写代码仓库/文档链接（选填）" />
        </el-form-item>
        <el-form-item label="提交附件">
          <div class="attachment-panel">
            <el-upload
              v-model:file-list="submissionUploadFileList"
              :auto-upload="false"
              :show-file-list="true"
              :on-change="onSubmissionFileSelect"
              :on-remove="onSubmissionFileRemove"
              multiple
            >
              <el-button type="primary">选择附件</el-button>
            </el-upload>
            <div class="attachment-tip">保存提交时会自动上传附件。</div>

            <el-table v-if="submissionFiles.length" :data="submissionFiles" border stripe style="margin-top: 10px">
              <el-table-column prop="originalName" label="已上传附件" min-width="220" show-overflow-tooltip />
              <el-table-column prop="uploadedAt" label="上传时间" width="170" />
              <el-table-column label="操作" width="130">
                <template #default="{ row }">
                  <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
                  <el-button link type="danger" @click="deleteSubmissionFile(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitSaving" @click="submitTaskWork">保存提交</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="submissionDrawerVisible"
      :title="`任务提交详情（${submissionTask?.title || '-'}）`"
      size="72%"
      append-to-body
    >
      <el-alert
        v-if="submissionTask"
        :title="`提交进度：${submissionTask.submittedGroupCount || 0}/${submissionTask.expectedGroupCount || 0}（${submissionTask.completionRate || 0}%）`"
        type="info"
        :closable="false"
      />
      <el-table :data="submissionRows" border stripe style="margin-top: 12px">
        <el-table-column label="小组" width="130">
          <template #default="{ row }">{{ teamName(row.teamId) }}</template>
        </el-table-column>
        <el-table-column label="提交人" width="120">
          <template #default="{ row }">{{ studentName(row.studentId) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="提交说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSubmissionDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="detailVisible" title="任务详情" width="760px" append-to-body>
      <div class="detail-wrap" v-if="detailTask">
        <div class="detail-meta">
          <span>实训项目：{{ detailTask.projectName || projectNameByPublish(detailTask.publishId) }}</span>
          <span>所属学期：{{ detailTask.termName || termNameByPublish(detailTask.publishId) }}</span>
          <span>适用班级：{{ detailTask.className || classNameByPublish(detailTask.publishId) }}</span>
          <span>任务阶段：{{ stageText(detailTask.stageType) }}</span>
          <span>截止时间：{{ detailTask.deadline || '-' }}</span>
          <span>剩余：{{ remainText(detailTask.deadline) }}</span>
          <span>评分权重：{{ detailTask.weight || 0 }}%</span>
        </div>
        <div class="detail-title">{{ detailTask.title || '-' }}</div>
        <div class="detail-content">{{ detailTask.content || '暂无任务说明' }}</div>
        <div class="detail-files">
          <div class="detail-files-title">任务附件</div>
          <el-empty v-if="!detailFiles.length" description="暂无附件" :image-size="56" />
          <div v-else class="file-list">
            <div v-for="row in detailFiles" :key="row.id" class="file-item">
              <div class="file-main">
                <div class="file-name">{{ row.originalName }}</div>
                <div class="file-time">{{ row.uploadedAt || '-' }}</div>
              </div>
              <el-button size="small" type="primary" plain @click="downloadFile(row)">下载</el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="submissionDetailVisible" title="提交详情" width="760px" append-to-body>
      <div class="detail-wrap" v-if="submissionDetail">
        <div class="detail-meta">
          <span>任务：{{ taskName(submissionDetail.taskId) }}</span>
          <span>小组：{{ teamName(submissionDetail.teamId) }}</span>
          <span>提交人：{{ studentName(submissionDetail.studentId) }}</span>
          <span>提交时间：{{ submissionDetail.submitTime || '-' }}</span>
        </div>
        <div class="detail-content">{{ submissionDetail.content || '暂无提交说明' }}</div>
        <div class="detail-link" v-if="submissionDetail.fileUrl">
          <span class="detail-label">参考链接：</span>
          <a :href="submissionDetail.fileUrl" target="_blank" rel="noopener noreferrer">{{ submissionDetail.fileUrl }}</a>
        </div>
        <div class="detail-files">
          <div class="detail-files-title">提交附件</div>
          <el-empty v-if="!submissionDetailFiles.length" description="暂无附件" :image-size="56" />
          <div v-else class="file-list">
            <div v-for="row in submissionDetailFiles" :key="row.id" class="file-item">
              <div class="file-main">
                <div class="file-name">{{ row.originalName }}</div>
                <div class="file-time">{{ row.uploadedAt || '-' }}</div>
              </div>
              <el-button size="small" type="primary" plain @click="downloadFile(row)">下载</el-button>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="submissionDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTaskApi, deleteTaskApi, listTasksApi, updateTaskApi } from '../api/tasks'
import { listProjectsApi, listPublishesApi } from '../api/projects'
import { listTeamsApi, myTeamApi } from '../api/teams'
import { listUserOptionsApi } from '../api/auth'
import { createSubmissionApi, listSubmissionsApi, updateSubmissionApi } from '../api/submissions'
import { deleteFileApi, downloadFileApi, listFilesApi, uploadFileApi } from '../api/files'
import { ROLE, canManageTeaching, hasAnyRole } from '../utils/auth'
import { fallbackName, toNameMap } from '../utils/dicts'

const canManageTask = computed(() => canManageTeaching())
const isStudentRole = computed(() => hasAnyRole([ROLE.STUDENT]))

const query = reactive({ termName: '' })
const activeProjectId = ref(null)
const tasks = ref([])
const projects = ref([])
const publishes = ref([])
const teams = ref([])
const students = ref([])
const mySubmissions = ref([])

const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null,
  publishId: null,
  title: '',
  stageType: 'DAILY',
  content: '',
  deadline: '',
  weight: 20,
  status: 1
})

const pendingFiles = ref([])
const taskUploadFileList = ref([])
const taskFiles = ref([])

const submitDialogVisible = ref(false)
const submitSaving = ref(false)
const submitForm = reactive({
  id: null,
  taskId: null,
  teamId: null,
  content: '',
  fileUrl: '',
  submitTime: ''
})
const submissionPendingFiles = ref([])
const submissionUploadFileList = ref([])
const submissionFiles = ref([])
const currentSubmitTask = ref(null)

const submissionDrawerVisible = ref(false)
const submissionTask = ref(null)
const submissionRows = ref([])

const submissionDetailVisible = ref(false)
const submissionDetail = ref(null)
const submissionDetailFiles = ref([])

const detailVisible = ref(false)
const detailTask = ref(null)
const detailFiles = ref([])

const termOptions = computed(() => {
  const set = new Set()
  publishes.value.forEach((item) => {
    if (item.termName) set.add(item.termName)
  })
  return Array.from(set)
    .sort((a, b) => String(b).localeCompare(String(a)))
    .map((value) => ({ value, label: value }))
})

const publishNameMap = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = publishOptionLabel(item)
  })
  return map
})
const publishDetailMap = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = {
      projectId: item.projectId || null,
      termName: item.termName || '-',
      className: item.className || '-'
    }
  })
  return map
})
const publishOptions = computed(() =>
  publishes.value.map((item) => ({
    value: item.id,
    label: publishOptionLabel(item),
    projectId: item.projectId || null
  }))
)
const filteredPublishOptions = computed(() => {
  if (!query.termName) return []
  const term = String(query.termName)
  return publishOptions.value.filter((item) => String(publishDetailMap.value[item.value]?.termName || '') === term)
})
const projectCards = computed(() => {
  const map = {}
  filteredPublishOptions.value.forEach((item) => {
    const projectId = item.projectId || -1
    if (!map[projectId]) {
      map[projectId] = {
        projectId,
        projectName: projectId === -1 ? '未关联项目' : projectName(projectId),
        publishIds: [],
        publishCount: 0,
        classSet: new Set()
      }
    }
    map[projectId].publishIds.push(item.value)
    map[projectId].publishCount += 1
    const classText = String(publishDetailMap.value[item.value]?.className || '')
    classText
      .split(/[,，;；/\s、]+/)
      .map((name) => name.trim())
      .filter(Boolean)
      .forEach((name) => map[projectId].classSet.add(name))
  })
  return Object.values(map).map((item) => {
    const publishIdSet = new Set(item.publishIds)
    const projectTasks = tasks.value.filter((task) => publishIdSet.has(task.publishId))
    const activeCount = projectTasks.filter((task) => {
      const sec = remainSeconds(task.deadline)
      return sec == null || sec > 0
    }).length
    const overdueCount = projectTasks.filter((task) => {
      const sec = remainSeconds(task.deadline)
      return sec != null && sec <= 0
    }).length
    return {
      projectId: item.projectId,
      projectName: item.projectName,
      publishIds: item.publishIds,
      publishCount: item.publishCount,
      classCount: item.classSet.size,
      taskCount: projectTasks.length,
      activeCount,
      overdueCount
    }
  }).sort((a, b) => {
    if (b.taskCount !== a.taskCount) return b.taskCount - a.taskCount
    return String(a.projectName).localeCompare(String(b.projectName))
  })
})
const activeProjectCard = computed(() =>
  projectCards.value.find((item) => item.projectId === activeProjectId.value) || null
)
const activeProjectPublishIds = computed(() => new Set(activeProjectCard.value?.publishIds || []))
const projectTasks = computed(() => {
  if (!query.termName || !activeProjectCard.value) return []
  return tasks.value.filter((item) => activeProjectPublishIds.value.has(item.publishId))
})
const termTaskCount = computed(() =>
  tasks.value.filter((item) => String(termNameByPublish(item.publishId)) === String(query.termName || '')).length
)
const urgentTaskCount = computed(() =>
  tasks.value.filter((item) => {
    if (String(termNameByPublish(item.publishId)) !== String(query.termName || '')) return false
    const sec = remainSeconds(item.deadline)
    return sec != null && sec > 0 && sec <= 3 * 24 * 3600
  }).length
)
const dialogPublishOptions = computed(() => {
  if (!query.termName) return []
  const source = activeProjectCard.value
    ? filteredPublishOptions.value.filter((item) => item.projectId === activeProjectCard.value.projectId)
    : filteredPublishOptions.value
  return source.map((item) => ({ value: item.value, label: item.label }))
})
const teamNameMap = computed(() => toNameMap(teams.value, 'id', 'teamName'))
const studentNameMap = computed(() => toNameMap(students.value, 'id', 'name'))
const taskNameMap = computed(() => toNameMap(tasks.value, 'id', 'title'))
const projectNameMap = computed(() => toNameMap(projects.value, 'id', 'projectName'))

const publishName = (id) => fallbackName(id, publishNameMap.value, '开设')
const displayTeamName = (teamName, teamId) => {
  const value = (teamName || '').trim()
  if (!value) return teamId ? `第${teamId}组` : '-'
  const match = value.match(/^([A-Za-z])组([\-－—_].*)?$/)
  if (!match) return value
  const index = match[1].toUpperCase().charCodeAt(0) - 64
  if (index < 1 || index > 26) return value
  return `第${index}组${match[2] || ''}`
}
const teamName = (id) => displayTeamName(fallbackName(id, teamNameMap.value, '小组'), id)
const studentName = (id) => fallbackName(id, studentNameMap.value, '学生')
const taskName = (id) => fallbackName(id, taskNameMap.value, '任务')
const projectName = (id) => fallbackName(id, projectNameMap.value, '项目')
const termNameByPublish = (id) => publishDetailMap.value[id]?.termName || '-'
const classNameByPublish = (id) => publishDetailMap.value[id]?.className || '-'
const projectIdByPublish = (id) => publishDetailMap.value[id]?.projectId || null
const projectNameByPublish = (id) => {
  const pid = projectIdByPublish(id)
  return pid ? projectName(pid) : publishName(id)
}

const publishOptionLabel = (item) => {
  const projectText = projectName(item.projectId)
  const classText = item.className || '未设班级'
  return `${projectText} · ${classText}`
}

const hasSubmitted = (taskId) => mySubmissions.value.some((item) => item.taskId === taskId)

const stageText = (stageType) => {
  if (stageType === 'OPENING') return '开题'
  if (stageType === 'MIDTERM') return '中期'
  if (stageType === 'FINAL') return '结题'
  if (stageType === 'DAILY') return '日常'
  return stageType || '-'
}

const parseTime = (value) => {
  if (!value) return null
  const date = new Date(String(value).replace(' ', 'T'))
  if (Number.isNaN(date.getTime())) return null
  return date
}

const remainSeconds = (deadline) => {
  const d = parseTime(deadline)
  if (!d) return null
  return Math.floor((d.getTime() - Date.now()) / 1000)
}

const remainText = (deadline) => {
  const sec = remainSeconds(deadline)
  if (sec == null) return '无截止'
  if (sec <= 0) return '已截止'
  const days = Math.floor(sec / 86400)
  const hours = Math.floor((sec % 86400) / 3600)
  const mins = Math.floor((sec % 3600) / 60)
  if (days > 0) return `${days}天${hours}小时`
  if (hours > 0) return `${hours}小时${mins}分钟`
  return `${Math.max(1, mins)}分钟`
}

const remainTagType = (deadline) => {
  const sec = remainSeconds(deadline)
  if (sec == null) return 'info'
  if (sec <= 0) return 'danger'
  if (sec <= 3 * 24 * 3600) return 'warning'
  return 'success'
}

const taskStatusText = (task) => {
  if (isStudentRole.value) {
    if (hasSubmitted(task.id)) return '已完成'
    const sec = remainSeconds(task.deadline)
    if (sec != null && sec <= 0) return '已截止'
    return '待完成'
  }
  const sec = remainSeconds(task.deadline)
  if (sec != null && sec <= 0) return '已截止'
  return '进行中'
}

const taskStatusType = (task) => {
  const text = taskStatusText(task)
  if (text === '已完成') return 'success'
  if (text === '已截止') return 'danger'
  return 'warning'
}

const canOperateSubmission = (task) => {
  if (!isStudentRole.value) return false
  const sec = remainSeconds(task.deadline)
  if (sec == null) return true
  return sec > 0
}

const submitButtonText = (task) => (hasSubmitted(task.id) ? '修改提交' : '提交任务')

const progressTagType = (rate) => {
  const value = Number(rate || 0)
  if (value >= 100) return 'success'
  if (value >= 60) return 'warning'
  return 'danger'
}

const loadDicts = async () => {
  projects.value = await listProjectsApi({})
  publishes.value = await listPublishesApi({})
  teams.value = await listTeamsApi({})
  students.value = await listUserOptionsApi({ userType: 'STUDENT' })
}

const loadTasks = async () => {
  tasks.value = await listTasksApi({})
  if (isStudentRole.value) {
    mySubmissions.value = await listSubmissionsApi({})
  }
}

const selectProject = (projectId) => {
  activeProjectId.value = projectId
}

const onTermChange = async () => {
  if (!query.termName) {
    activeProjectId.value = null
    return
  }
  if (!projectCards.value.length) {
    activeProjectId.value = null
    return
  }
  if (!projectCards.value.some((item) => item.projectId === activeProjectId.value)) {
    activeProjectId.value = projectCards.value[0].projectId
  }
  await loadTasks()
}

const loadTaskFiles = async (taskId) => {
  if (!taskId) {
    taskFiles.value = []
    return
  }
  taskFiles.value = await listFilesApi({ bizType: 'TASK', bizId: taskId })
}

const openCreate = () => {
  const defaultPublishId =
    (dialogPublishOptions.value.length ? dialogPublishOptions.value[0].value : null) ||
    (filteredPublishOptions.value.length ? filteredPublishOptions.value[0].value : null) ||
    (publishOptions.value.length ? publishOptions.value[0].value : null)
  Object.assign(form, {
    id: null,
    publishId: defaultPublishId,
    title: '',
    stageType: 'DAILY',
    content: '',
    deadline: '',
    weight: 20,
    status: 1
  })
  pendingFiles.value = []
  taskUploadFileList.value = []
  taskFiles.value = []
  dialogVisible.value = true
}

const openEdit = async (row) => {
  Object.assign(form, row)
  pendingFiles.value = []
  taskUploadFileList.value = []
  await loadTaskFiles(row.id)
  dialogVisible.value = true
}

const onTaskFileSelect = (uploadFile) => {
  if (!uploadFile?.raw) return
  pendingFiles.value.push(uploadFile.raw)
}

const onTaskFileRemove = (uploadFile) => {
  if (!uploadFile?.raw) return
  pendingFiles.value = pendingFiles.value.filter((item) => item !== uploadFile.raw)
}

const uploadPendingTaskFiles = async (taskId) => {
  if (!pendingFiles.value.length) return
  for (const raw of pendingFiles.value) {
    const formData = new FormData()
    formData.append('file', raw)
    await uploadFileApi(formData, 'TASK', taskId)
  }
  pendingFiles.value = []
  taskUploadFileList.value = []
}

const submit = async () => {
  if (!form.publishId || !form.title) {
    ElMessage.warning('请先选择项目班级并填写任务标题')
    return
  }
  form.status = 1
  saving.value = true
  try {
    let taskId = form.id
    if (form.id) {
      await updateTaskApi(form.id, form)
    } else {
      const created = await createTaskApi(form)
      taskId = created.id
      form.id = created.id
    }
    await uploadPendingTaskFiles(taskId)
    await loadTaskFiles(taskId)
    ElMessage.success('任务保存成功')
    dialogVisible.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该任务吗？', '提示', { type: 'warning' })
  await deleteTaskApi(id)
  ElMessage.success('任务删除成功')
  await loadTasks()
}

const deleteTaskFile = async (row) => {
  await ElMessageBox.confirm('确认删除该附件吗？', '提示', { type: 'warning' })
  await deleteFileApi(row.id)
  ElMessage.success('附件删除成功')
  await loadTaskFiles(form.id)
}

const openDetail = async (row) => {
  detailTask.value = row
  detailVisible.value = true
  detailFiles.value = await listFilesApi({ bizType: 'TASK', bizId: row.id })
}

const openSubmitDialog = async (task) => {
  const myTeam = await myTeamApi({ publishId: task.publishId })
  if (!myTeam?.inTeam) {
    ElMessage.warning('请先在团队管理中加入小组，再提交任务')
    return
  }
  currentSubmitTask.value = task
  submitDialogVisible.value = true
  const rows = await listSubmissionsApi({ taskId: task.id })
  const existing = rows.length ? rows[0] : null
  Object.assign(submitForm, {
    id: existing ? existing.id : null,
    taskId: task.id,
    teamId: myTeam.teamId,
    content: existing ? existing.content : '',
    fileUrl: existing ? existing.fileUrl : '',
    submitTime: existing ? existing.submitTime : ''
  })
  submissionPendingFiles.value = []
  submissionUploadFileList.value = []
  if (submitForm.id) {
    submissionFiles.value = await listFilesApi({ bizType: 'SUBMISSION', bizId: submitForm.id })
  } else {
    submissionFiles.value = []
  }
}

const onSubmissionFileSelect = (uploadFile) => {
  if (!uploadFile?.raw) return
  submissionPendingFiles.value.push(uploadFile.raw)
}

const onSubmissionFileRemove = (uploadFile) => {
  if (!uploadFile?.raw) return
  submissionPendingFiles.value = submissionPendingFiles.value.filter((item) => item !== uploadFile.raw)
}

const uploadPendingSubmissionFiles = async (submissionId) => {
  if (!submissionPendingFiles.value.length) return
  for (const raw of submissionPendingFiles.value) {
    const formData = new FormData()
    formData.append('file', raw)
    await uploadFileApi(formData, 'SUBMISSION', submissionId)
  }
  submissionPendingFiles.value = []
  submissionUploadFileList.value = []
}

const submitTaskWork = async () => {
  if (!submitForm.taskId || !submitForm.content) {
    ElMessage.warning('请填写提交说明')
    return
  }
  submitSaving.value = true
  try {
    let submissionId = submitForm.id
    if (submitForm.id) {
      await updateSubmissionApi(submitForm.id, submitForm)
    } else {
      const created = await createSubmissionApi(submitForm)
      submissionId = created.id
      submitForm.id = created.id
    }
    await uploadPendingSubmissionFiles(submissionId)
    submissionFiles.value = await listFilesApi({ bizType: 'SUBMISSION', bizId: submissionId })
    ElMessage.success('提交成功')
    submitDialogVisible.value = false
    await loadTasks()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitSaving.value = false
  }
}

const deleteSubmissionFile = async (row) => {
  await ElMessageBox.confirm('确认删除该附件吗？', '提示', { type: 'warning' })
  await deleteFileApi(row.id)
  ElMessage.success('附件删除成功')
  if (submitForm.id) {
    submissionFiles.value = await listFilesApi({ bizType: 'SUBMISSION', bizId: submitForm.id })
  }
}

const openSubmissionDrawer = async (task) => {
  submissionTask.value = task
  submissionDrawerVisible.value = true
  teams.value = await listTeamsApi({ publishId: task.publishId })
  const rows = await listSubmissionsApi({ taskId: task.id })
  const latestByTeam = {}
  rows.forEach((row) => {
    const key = row.teamId || row.id
    const existing = latestByTeam[key]
    if (!existing) {
      latestByTeam[key] = row
      return
    }
    const t1 = parseTime(existing.submitTime)?.getTime() || 0
    const t2 = parseTime(row.submitTime)?.getTime() || 0
    if (t2 >= t1) {
      latestByTeam[key] = row
    }
  })
  submissionRows.value = Object.values(latestByTeam).sort((a, b) => {
    const t1 = parseTime(a.submitTime)?.getTime() || 0
    const t2 = parseTime(b.submitTime)?.getTime() || 0
    return t2 - t1
  })
}

const openSubmissionDetail = async (row) => {
  submissionDetail.value = row
  submissionDetailVisible.value = true
  submissionDetailFiles.value = await listFilesApi({ bizType: 'SUBMISSION', bizId: row.id })
}

const downloadFile = async (row) => {
  try {
    await downloadFileApi(row.id, row.originalName)
  } catch (error) {
    ElMessage.error(error.message || '下载失败')
  }
}

watch(projectCards, (cards) => {
  if (!query.termName) {
    activeProjectId.value = null
    return
  }
  if (!cards.length) {
    activeProjectId.value = null
    return
  }
  if (!cards.some((item) => item.projectId === activeProjectId.value)) {
    activeProjectId.value = cards[0].projectId
  }
})

onMounted(async () => {
  await loadDicts()
  if (!query.termName && termOptions.value.length) {
    query.termName = termOptions.value[0].value
  }
  await loadTasks()
  if (projectCards.value.length && activeProjectId.value == null) {
    activeProjectId.value = projectCards.value[0].projectId
  }
})
</script>

<style scoped>
.task-stage-card {
  background:
    linear-gradient(135deg, rgba(247, 252, 255, 0.96) 0%, rgba(240, 248, 253, 0.94) 100%);
}

.task-stage-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.task-stage-filter {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-stage-label {
  font-size: 12px;
  letter-spacing: 0.5px;
  color: #5a7286;
  font-weight: 700;
}

.task-stage-filter :deep(.el-select) {
  width: 280px;
}

.task-stage-kpis {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.task-kpi-item {
  min-width: 110px;
  border: 1px solid #d1e1ee;
  border-radius: 12px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.88);
}

.task-kpi-label {
  color: #597186;
  font-size: 12px;
}

.task-kpi-value {
  margin-top: 2px;
  color: #1b3f5c;
  font-size: 20px;
  font-weight: 800;
}

.project-card-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}

.project-card {
  appearance: none;
  border: 1px solid #cfe0ec;
  border-radius: 14px;
  padding: 14px;
  background:
    linear-gradient(140deg, rgba(255, 255, 255, 0.95) 0%, rgba(246, 251, 255, 0.95) 100%);
  text-align: left;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 220ms ease, border-color 220ms ease;
}

.project-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 22px rgba(20, 58, 88, 0.12);
  border-color: #b9d0e1;
}

.project-card.active {
  border-color: #2b6892;
  box-shadow: 0 14px 26px rgba(16, 58, 90, 0.2);
  background:
    linear-gradient(140deg, rgba(36, 96, 136, 0.96) 0%, rgba(23, 68, 100, 0.96) 100%);
}

.project-card.active .project-card-title,
.project-card.active .project-card-meta,
.project-card.active .project-card-stats {
  color: #f4f9fd;
}

.project-card-title {
  font-weight: 800;
  color: #1c3a53;
  font-size: 16px;
}

.project-card-meta {
  margin-top: 6px;
  color: #5a7489;
  font-size: 12px;
}

.project-card-stats {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  color: #2f566f;
  font-size: 12px;
  font-weight: 600;
}

.project-focus-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a3a54;
}

.progress-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.attachment-panel {
  width: 100%;
}

.attachment-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #62798d;
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #5b7387;
  font-size: 13px;
  line-height: 1.6;
}

.detail-title {
  font-size: 24px;
  line-height: 1.35;
  font-weight: 700;
  color: #122f46;
}

.detail-content {
  background: #f7fbff;
  border: 1px solid #d8e6f1;
  border-radius: 12px;
  padding: 16px 18px;
  color: #203a50;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.detail-files-title {
  font-weight: 700;
  color: #1f3f5a;
  margin-bottom: 10px;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #d9e6f1;
  background: #f9fcff;
}

.file-main {
  min-width: 0;
}

.file-name {
  color: #21435f;
  font-weight: 600;
  word-break: break-all;
}

.file-time {
  margin-top: 3px;
  font-size: 12px;
  color: #6a8094;
}

.detail-link a {
  color: #1f5d88;
}

.detail-label {
  color: #5b7387;
}
</style>
