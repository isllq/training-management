<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">任务提交管理</div>
      <div class="module-head-desc">跟踪学生与团队的阶段提交、版本迭代和教师反馈，形成过程留痕。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.taskId" placeholder="按任务筛选" clearable filterable>
          <el-option v-for="item in taskOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.teamId" placeholder="按团队筛选" clearable filterable>
          <el-option v-for="item in teamOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button v-if="canSubmit" type="primary" @click="openCreate">新建提交</el-button>
      </div>

      <el-table :data="submissions" border stripe>
        <el-table-column label="任务名称" min-width="170">
          <template #default="{ row }">{{ taskName(row.taskId) }}</template>
        </el-table-column>
        <el-table-column label="所属团队" width="140">
          <template #default="{ row }">{{ teamName(row.teamId) }}</template>
        </el-table-column>
        <el-table-column label="提交人" width="120">
          <template #default="{ row }">{{ studentName(row.studentId) }}</template>
        </el-table-column>
        <el-table-column prop="versionNo" label="版本号" width="80" />
        <el-table-column prop="content" label="提交说明" min-width="220" show-overflow-tooltip />
        <el-table-column prop="teacherFeedback" label="教师反馈" min-width="180" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'info'">{{ row.status === 2 ? '已批阅' : '待批阅' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canEditRow(row)" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canDeleteRow(row)" size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑提交记录' : '新建提交记录'"
      width="720px"
      append-to-body
    >
      <el-form :model="form" label-width="95px" class="dialog-form">
        <el-form-item label="任务名称">
          <el-select v-model="form.taskId" placeholder="请选择任务" filterable>
            <el-option v-for="item in taskOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属团队">
          <el-select v-model="form.teamId" placeholder="请选择团队" filterable>
            <el-option v-for="item in teamOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="isAdminRole" label="提交人">
          <el-select v-model="form.studentId" placeholder="请选择提交人" filterable>
            <el-option v-for="item in studentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本号"><el-input-number v-model="form.versionNo" :min="1" /></el-form-item>
        <el-form-item label="提交说明"><el-input v-model="form.content" type="textarea" rows="4" /></el-form-item>
        <el-form-item label="参考链接">
          <el-input v-model="form.fileUrl" placeholder="可填写仓库链接或文档地址" />
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="form.submitTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="isAdminRole" label="处理状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">待批阅</el-radio>
            <el-radio :label="2">已批阅</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="isAdminRole" label="教师反馈"><el-input v-model="form.teacherFeedback" type="textarea" /></el-form-item>

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
            <div class="attachment-tip">保存提交记录时会自动上传所选附件。</div>

            <el-table v-if="submissionFiles.length" :data="submissionFiles" border stripe style="margin-top: 10px">
              <el-table-column prop="originalName" label="已上传附件" min-width="220" show-overflow-tooltip />
              <el-table-column prop="uploadedAt" label="上传时间" width="170" />
              <el-table-column label="操作" width="130">
                <template #default="{ row }">
                  <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
                  <el-button v-if="canEditCurrentSubmission" link type="danger" @click="deleteSubmissionFile(row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="提交详情" width="760px" append-to-body>
      <div class="detail-wrap" v-if="detailRecord">
        <div class="detail-meta">
          <span>任务：{{ taskName(detailRecord.taskId) }}</span>
          <span>团队：{{ teamName(detailRecord.teamId) }}</span>
          <span>提交人：{{ studentName(detailRecord.studentId) }}</span>
          <span>版本：V{{ detailRecord.versionNo || 1 }}</span>
          <span>提交时间：{{ detailRecord.submitTime || '-' }}</span>
        </div>
        <div class="detail-meta">
          <span>处理状态：{{ detailRecord.status === 2 ? '已批阅' : '待批阅' }}</span>
          <span>教师反馈：{{ detailRecord.teacherFeedback || '暂无反馈' }}</span>
        </div>
        <div class="detail-content">{{ detailRecord.content || '暂无提交说明' }}</div>
        <div class="detail-link" v-if="detailRecord.fileUrl">
          <span class="detail-label">参考链接：</span>
          <a :href="detailRecord.fileUrl" target="_blank" rel="noopener noreferrer">{{ detailRecord.fileUrl }}</a>
        </div>

        <div class="detail-files">
          <div class="detail-files-title">提交附件</div>
          <el-empty v-if="!detailFiles.length" description="暂无附件" :image-size="56" />
          <el-table v-else :data="detailFiles" border stripe>
            <el-table-column prop="originalName" label="文件名" min-width="220" show-overflow-tooltip />
            <el-table-column prop="uploadedAt" label="上传时间" width="170" />
            <el-table-column label="操作" width="90">
              <template #default="{ row }">
                <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createSubmissionApi,
  deleteSubmissionApi,
  listSubmissionsApi,
  updateSubmissionApi
} from '../api/submissions'
import { listTasksApi } from '../api/tasks'
import { listTeamsApi } from '../api/teams'
import { listUserOptionsApi } from '../api/auth'
import { deleteFileApi, downloadFileApi, listFilesApi, uploadFileApi } from '../api/files'
import { ROLE, getUserId, hasAnyRole } from '../utils/auth'
import { toNameMap, fallbackName } from '../utils/dicts'

const isAdminRole = computed(() => hasAnyRole([ROLE.ADMIN]))
const isStudentRole = computed(() => hasAnyRole([ROLE.STUDENT]))
const canSubmit = computed(() => isStudentRole.value)
const currentUserId = computed(() => getUserId())

const query = reactive({ taskId: null, teamId: null })
const submissions = ref([])
const tasks = ref([])
const teams = ref([])
const students = ref([])

const dialogVisible = ref(false)
const detailVisible = ref(false)
const detailRecord = ref(null)
const detailFiles = ref([])

const saving = ref(false)
const form = reactive({
  id: null,
  taskId: null,
  teamId: null,
  studentId: null,
  versionNo: 1,
  content: '',
  fileUrl: '',
  submitTime: '',
  status: 1,
  teacherFeedback: ''
})

const pendingFiles = ref([])
const submissionUploadFileList = ref([])
const submissionFiles = ref([])

const taskOptions = computed(() => tasks.value.map((item) => ({ value: item.id, label: item.title || `任务${item.id}` })))
const teamOptions = computed(() => teams.value.map((item) => ({ value: item.id, label: item.teamName || `团队${item.id}` })))
const studentOptions = computed(() => students.value.map((item) => ({ value: item.id, label: item.name || `学生${item.id}` })))

const taskNameMap = computed(() => toNameMap(tasks.value, 'id', 'title'))
const teamNameMap = computed(() => toNameMap(teams.value, 'id', 'teamName'))
const studentNameMap = computed(() => toNameMap(students.value, 'id', 'name'))

const taskName = (id) => fallbackName(id, taskNameMap.value, '任务')
const teamName = (id) => fallbackName(id, teamNameMap.value, '团队')
const studentName = (id) => fallbackName(id, studentNameMap.value, '学生')

const canEditCurrentSubmission = computed(() => {
  if (!form.id) return true
  if (isAdminRole.value) return true
  return isStudentRole.value && form.studentId === currentUserId.value
})

const canEditRow = (row) => isAdminRole.value || (isStudentRole.value && row.studentId === currentUserId.value)
const canDeleteRow = (row) => isAdminRole.value || (isStudentRole.value && row.studentId === currentUserId.value)

const loadDicts = async () => {
  tasks.value = await listTasksApi({})
  teams.value = await listTeamsApi({})
  students.value = await listUserOptionsApi({ userType: 'STUDENT' })
}

const loadSubmissions = async () => {
  const params = {}
  if (query.taskId) params.taskId = query.taskId
  if (query.teamId) params.teamId = query.teamId
  submissions.value = await listSubmissionsApi(params)
}

const loadSubmissionFiles = async (submissionId) => {
  if (!submissionId) {
    submissionFiles.value = []
    return
  }
  submissionFiles.value = await listFilesApi({ bizType: 'SUBMISSION', bizId: submissionId })
}

const loadDetailFiles = async (submissionId) => {
  if (!submissionId) {
    detailFiles.value = []
    return
  }
  detailFiles.value = await listFilesApi({ bizType: 'SUBMISSION', bizId: submissionId })
}

const openCreate = async () => {
  Object.assign(form, {
    id: null,
    taskId: taskOptions.value.length ? taskOptions.value[0].value : null,
    teamId: teamOptions.value.length ? teamOptions.value[0].value : null,
    studentId: currentUserId.value || (studentOptions.value.length ? studentOptions.value[0].value : null),
    versionNo: 1,
    content: '',
    fileUrl: '',
    submitTime: '',
    status: 1,
    teacherFeedback: ''
  })
  pendingFiles.value = []
  submissionUploadFileList.value = []
  submissionFiles.value = []
  dialogVisible.value = true
}

const openEdit = async (row) => {
  if (!canEditRow(row)) {
    ElMessage.warning('无权限编辑该提交记录')
    return
  }
  Object.assign(form, row)
  pendingFiles.value = []
  submissionUploadFileList.value = []
  await loadSubmissionFiles(row.id)
  dialogVisible.value = true
}

const openDetail = async (row) => {
  detailRecord.value = { ...row }
  detailVisible.value = true
  await loadDetailFiles(row.id)
}

const onSubmissionFileSelect = (uploadFile) => {
  if (!uploadFile?.raw) return
  pendingFiles.value.push(uploadFile.raw)
}

const onSubmissionFileRemove = (uploadFile) => {
  if (!uploadFile?.raw) return
  pendingFiles.value = pendingFiles.value.filter((item) => item !== uploadFile.raw)
}

const uploadPendingFiles = async (submissionId) => {
  if (!pendingFiles.value.length) return
  for (const raw of pendingFiles.value) {
    const formData = new FormData()
    formData.append('file', raw)
    await uploadFileApi(formData, 'SUBMISSION', submissionId)
  }
  pendingFiles.value = []
  submissionUploadFileList.value = []
}

const submit = async () => {
  if (!form.taskId || !form.teamId || !form.content) {
    ElMessage.warning('请完整填写任务、团队和提交说明')
    return
  }

  if (!isAdminRole.value) {
    form.studentId = currentUserId.value
    if (!form.id) {
      form.status = 1
      form.teacherFeedback = ''
    }
  }

  saving.value = true
  try {
    let submissionId = form.id
    if (form.id) {
      await updateSubmissionApi(form.id, form)
    } else {
      const created = await createSubmissionApi(form)
      submissionId = created.id
      form.id = created.id
    }

    await uploadPendingFiles(submissionId)
    await loadSubmissionFiles(submissionId)
    ElMessage.success('提交记录保存成功')
    dialogVisible.value = false
    await loadSubmissions()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const remove = async (id) => {
  const row = submissions.value.find((item) => item.id === id)
  if (row && !canDeleteRow(row)) {
    ElMessage.warning('无权限删除该提交记录')
    return
  }
  await ElMessageBox.confirm('确认删除该提交记录吗？', '提示', { type: 'warning' })
  await deleteSubmissionApi(id)
  ElMessage.success('提交记录删除成功')
  await loadSubmissions()
}

const downloadFile = async (row) => {
  try {
    await downloadFileApi(row.id, row.originalName)
  } catch (error) {
    ElMessage.error(error.message || '下载失败')
  }
}

const deleteSubmissionFile = async (row) => {
  await ElMessageBox.confirm('确认删除该附件吗？', '提示', { type: 'warning' })
  await deleteFileApi(row.id)
  ElMessage.success('附件删除成功')
  await loadSubmissionFiles(form.id)
}

watch(
  () => [query.taskId, query.teamId],
  async () => {
    await loadSubmissions()
  }
)

onMounted(async () => {
  await loadDicts()
  await loadSubmissions()
})
</script>

<style scoped>
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
  gap: 12px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #5b7387;
  font-size: 13px;
}

.detail-content {
  background: #f7fbff;
  border: 1px solid #d8e6f1;
  border-radius: 10px;
  padding: 12px;
  color: #2d475c;
  line-height: 1.7;
  white-space: pre-wrap;
}

.detail-link a {
  color: #1f5d88;
}

.detail-label {
  color: #5b7387;
}

.detail-files-title {
  font-weight: 700;
  color: #1f3f5a;
  margin-bottom: 8px;
}
</style>
