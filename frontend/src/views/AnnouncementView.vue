<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">公告通知中心</div>
      <div class="module-head-desc">发布课程通知、进度提醒与教学安排，支持附件共享与已读跟踪。</div>
    </div>

    <div class="page-card notice-kpi">
      <div class="kpi-item">
        <div class="kpi-label">我的未读公告</div>
        <div class="kpi-value">{{ unreadCount }}</div>
      </div>
      <div class="kpi-item">
        <div class="kpi-label">当前筛选公告数</div>
        <div class="kpi-value">{{ filteredAnnouncements.length }}</div>
      </div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.termName" placeholder="按学期筛选" clearable filterable>
          <el-option v-for="item in termOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.publishId" placeholder="按开设计划筛选" clearable filterable>
          <el-option v-for="item in filteredPublishOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="按公告标题或内容检索" clearable @keyup.enter="loadAnnouncements" />
        <el-button v-if="canManage" :type="query.onlyMine ? 'primary' : 'default'" @click="toggleMineMode">
          {{ query.onlyMine ? '返回全部公告' : '管理我发布公告' }}
        </el-button>
        <el-button v-if="canManage" type="primary" @click="openCreate">发布公告</el-button>
      </div>

      <el-table :data="pagedAnnouncements" border stripe :row-class-name="rowClassName">
        <el-table-column prop="title" label="公告标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="适用开设" min-width="180">
          <template #default="{ row }">{{ publishName(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="发布人" width="120">
          <template #default="{ row }">{{ row.authorName || userName(row.authorId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="170" />
        <el-table-column label="阅读" width="90">
          <template #default="{ row }">
            <el-tag :type="isRead(row) ? 'success' : 'warning'">{{ isRead(row) ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canManage" label="阅读统计" width="160">
          <template #default="{ row }">
            <span class="read-stat">已读 {{ row.readCount || 0 }} / 未读 {{ row.unreadCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">查看详情</el-button>
            <el-button v-if="canEditRow(row)" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canDeleteRow(row)" size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap" v-if="sortedAnnouncements.length > pageSize">
        <el-pagination
          background
          layout="total, prev, pager, next, jumper"
          :page-size="pageSize"
          :current-page="pageNo"
          :total="sortedAnnouncements.length"
          @current-change="onPageChange"
        />
      </div>
    </div>

    <el-dialog
      v-if="canManage"
      v-model="dialogVisible"
      :title="form.id ? '编辑公告' : '发布公告'"
      width="720px"
      append-to-body
    >
      <el-form :model="form" label-width="95px" class="dialog-form">
        <el-form-item label="适用开设">
          <el-select v-model="form.publishId" placeholder="不选则全体可见" clearable filterable>
            <el-option v-for="item in publishOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="公告标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="公告内容"><el-input v-model="form.content" type="textarea" rows="6" /></el-form-item>
        <el-form-item label="发布状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker v-model="form.publishTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="失效时间">
          <el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>

        <el-form-item label="公告附件">
          <div class="attachment-panel">
            <el-upload
              v-model:file-list="uploadFileList"
              :auto-upload="false"
              :show-file-list="true"
              :on-change="onAnnouncementFileSelect"
              :on-remove="onAnnouncementFileRemove"
              multiple
            >
              <el-button type="primary">选择附件</el-button>
            </el-upload>
            <div class="attachment-tip">保存公告时会自动上传所选附件。{{ form.id ? '当前公告已存在，可继续追加附件。' : '新公告会先创建后上传附件。' }}</div>

            <el-table v-if="manageFiles.length" :data="manageFiles" border stripe style="margin-top: 10px">
              <el-table-column prop="originalName" label="已上传附件" min-width="220" show-overflow-tooltip />
              <el-table-column prop="uploadedAt" label="上传时间" width="170" />
              <el-table-column label="操作" width="130">
                <template #default="{ row }">
                  <el-button link type="primary" @click="downloadFile(row)">下载</el-button>
                  <el-button link type="danger" @click="deleteManageFile(row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="公告详情" width="760px" append-to-body>
      <div class="detail-wrap" v-if="detailAnnouncement">
        <div v-if="canManage && Number(detailAnnouncement.status) === 1" class="detail-reminder">
          当前公告已发布，如需调整内容，请删除后重新发布。
        </div>
        <div class="detail-head">
          <h3>{{ detailAnnouncement.title }}</h3>
          <div class="detail-meta">
            <span>发布人：{{ detailAnnouncement.authorName || userName(detailAnnouncement.authorId) }}</span>
            <span>发布时间：{{ detailAnnouncement.publishTime || '-' }}</span>
            <span>适用范围：{{ publishName(detailAnnouncement.publishId) }}</span>
            <span v-if="canManage">已读：{{ detailAnnouncement.readCount || 0 }}</span>
            <span v-if="canManage">未读：{{ detailAnnouncement.unreadCount || 0 }}</span>
          </div>
        </div>
        <div class="detail-content">{{ detailAnnouncement.content }}</div>

        <div class="detail-files">
          <div class="detail-files-title">附件下载</div>
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
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAnnouncementApi,
  deleteAnnouncementApi,
  listAnnouncementsApi,
  markAnnouncementReadApi,
  unreadAnnouncementCountApi,
  updateAnnouncementApi
} from '../api/announcements'
import { listPublishesApi } from '../api/projects'
import { listUserOptionsApi } from '../api/auth'
import { deleteFileApi, downloadFileApi, listFilesApi, uploadFileApi } from '../api/files'
import { ROLE, getUserId, hasAnyRole } from '../utils/auth'
import { fallbackName, toNameMap } from '../utils/dicts'

const currentUserId = computed(() => getUserId())
const canManage = computed(() => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER]))
const isAdmin = computed(() => hasAnyRole([ROLE.ADMIN]))

const query = reactive({ termName: '', publishId: null, keyword: '', onlyMine: false })
const announcements = ref([])
const unreadCount = ref(0)

const publishes = ref([])
const users = ref([])

const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null,
  publishId: null,
  title: '',
  content: '',
  status: 1,
  publishTime: '',
  expireTime: ''
})
const pendingFiles = ref([])
const uploadFileList = ref([])
const manageFiles = ref([])

const detailVisible = ref(false)
const detailAnnouncement = ref(null)
const detailFiles = ref([])
const pageNo = ref(1)
const pageSize = 10

const publishOptions = computed(() => publishes.value.map((item) => ({ value: item.id, label: publishOptionLabel(item) })))
const termOptions = computed(() => {
  const set = new Set()
  publishes.value.forEach((item) => {
    if (item.termName) set.add(item.termName)
  })
  return Array.from(set)
    .sort((a, b) => String(b).localeCompare(String(a)))
    .map((value) => ({ value, label: value }))
})
const filteredPublishOptions = computed(() => {
  if (!query.termName) {
    return publishOptions.value
  }
  const term = String(query.termName)
  return publishes.value
    .filter((item) => String(item.termName || '') === term)
    .map((item) => ({ value: item.id, label: publishOptionLabel(item) }))
})
const publishNameMap = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = publishOptionLabel(item)
  })
  return map
})
const userNameMap = computed(() => toNameMap(users.value, 'id', 'name'))
const publishTermMap = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = item.termName || ''
  })
  return map
})
const filteredAnnouncements = computed(() => {
  let list = announcements.value
  if (query.onlyMine) {
    list = list.filter((item) => Number(item.authorId) === Number(currentUserId.value))
  }
  if (query.publishId) {
    list = list.filter((item) => item.publishId === query.publishId)
  }
  if (query.termName) {
    const term = String(query.termName)
    list = list.filter((item) => String(publishTermMap.value[item.publishId] || '') === term)
  }
  return list
})
const sortedAnnouncements = computed(() =>
  [...filteredAnnouncements.value]
    .sort((a, b) => toTimestamp(b.publishTime || b.createdAt) - toTimestamp(a.publishTime || a.createdAt))
)
const pagedAnnouncements = computed(() => {
  const start = (pageNo.value - 1) * pageSize
  return sortedAnnouncements.value.slice(start, start + pageSize)
})

const publishName = (id) => {
  if (id == null) return '全体学生'
  return fallbackName(id, publishNameMap.value, '开设')
}
const userName = (id) => fallbackName(id, userNameMap.value, '用户')
const canDeleteRow = (row) => canManage.value && (isAdmin.value || Number(row.authorId) === Number(currentUserId.value))
const canEditRow = (row) => canDeleteRow(row) && Number(row.status) !== 1
const toTimestamp = (value) => {
  if (!value) return 0
  const time = new Date(String(value).replace(' ', 'T')).getTime()
  return Number.isNaN(time) ? 0 : time
}
const publishOptionLabel = (publish) => {
  const project = publish?.projectName || '实训科目'
  const className = publish?.className || '未设班级'
  return `${project} · ${className}`
}
const toggleMineMode = () => {
  query.onlyMine = !query.onlyMine
  pageNo.value = 1
}
const onPageChange = (next) => {
  pageNo.value = next
}

const isRead = (row) => Number(row?.readFlag) === 1
const rowClassName = ({ row }) => (isRead(row) ? '' : 'notice-row-unread')

const loadDicts = async () => {
  publishes.value = await listPublishesApi({})
  users.value = await listUserOptionsApi({})
}

const loadAnnouncements = async () => {
  const params = {}
  if (query.publishId) params.publishId = query.publishId
  if (query.keyword) params.keyword = query.keyword
  announcements.value = await listAnnouncementsApi(params)
  pageNo.value = 1
}

const loadUnreadCount = async () => {
  unreadCount.value = await unreadAnnouncementCountApi()
}

const loadManageFiles = async (announcementId) => {
  if (!announcementId) {
    manageFiles.value = []
    return
  }
  manageFiles.value = await listFilesApi({ bizType: 'ANNOUNCEMENT', bizId: announcementId })
}

const openCreate = async () => {
  Object.assign(form, {
    id: null,
    publishId: query.publishId || null,
    title: '',
    content: '',
    status: 1,
    publishTime: '',
    expireTime: ''
  })
  pendingFiles.value = []
  uploadFileList.value = []
  manageFiles.value = []
  dialogVisible.value = true
}

const openEdit = async (row) => {
  if (Number(row.status) === 1) {
    ElMessage.warning('已发布公告不可编辑，请删除后重新发布')
    return
  }
  Object.assign(form, {
    id: row.id,
    publishId: row.publishId ?? null,
    title: row.title || '',
    content: row.content || '',
    status: row.status ?? 0,
    publishTime: row.publishTime || '',
    expireTime: row.expireTime || ''
  })
  pendingFiles.value = []
  uploadFileList.value = []
  await loadManageFiles(row.id)
  dialogVisible.value = true
}

const onAnnouncementFileSelect = (uploadFile) => {
  if (!uploadFile?.raw) return
  pendingFiles.value.push(uploadFile.raw)
}

const onAnnouncementFileRemove = (uploadFile) => {
  if (!uploadFile?.raw) return
  pendingFiles.value = pendingFiles.value.filter((item) => item !== uploadFile.raw)
}

const uploadPendingFiles = async (announcementId) => {
  if (!pendingFiles.value.length) return
  for (const raw of pendingFiles.value) {
    const formData = new FormData()
    formData.append('file', raw)
    await uploadFileApi(formData, 'ANNOUNCEMENT', announcementId)
  }
  pendingFiles.value = []
  uploadFileList.value = []
}

const submit = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写公告标题和内容')
    return
  }
  saving.value = true
  try {
    const payload = {
      publishId: form.publishId,
      title: form.title,
      content: form.content,
      status: form.status,
      publishTime: form.publishTime,
      expireTime: form.expireTime
    }
    let announcementId = form.id
    if (form.id) {
      await updateAnnouncementApi(form.id, payload)
    } else {
      const created = await createAnnouncementApi(payload)
      announcementId = created.id
      form.id = created.id
    }

    await uploadPendingFiles(announcementId)
    ElMessage.success('公告保存成功')
    await loadManageFiles(announcementId)
    await loadAnnouncements()
    await loadUnreadCount()
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该公告吗？', '提示', { type: 'warning' })
  await deleteAnnouncementApi(id)
  ElMessage.success('公告删除成功')
  await loadAnnouncements()
  await loadUnreadCount()
}

const openDetail = async (row) => {
  detailAnnouncement.value = row
  detailVisible.value = true
  detailFiles.value = await listFilesApi({ bizType: 'ANNOUNCEMENT', bizId: row.id })
  if (!isRead(row)) {
    await markAnnouncementReadApi(row.id)
    await loadAnnouncements()
    await loadUnreadCount()
  }
}

const downloadFile = async (row) => {
  try {
    await downloadFileApi(row.id, row.originalName)
  } catch (error) {
    ElMessage.error(error.message || '下载失败')
  }
}

const deleteManageFile = async (row) => {
  await ElMessageBox.confirm('确认删除该附件吗？', '提示', { type: 'warning' })
  await deleteFileApi(row.id)
  ElMessage.success('附件删除成功')
  await loadManageFiles(form.id)
}

onMounted(async () => {
  await loadDicts()
  await loadAnnouncements()
  await loadUnreadCount()
})

watch(
  () => query.termName,
  () => {
    if (query.publishId && !filteredPublishOptions.value.some((item) => item.value === query.publishId)) {
      query.publishId = null
    }
    pageNo.value = 1
  }
)

watch(
  () => query.publishId,
  async () => {
    await loadAnnouncements()
    await loadUnreadCount()
  }
)

watch(
  () => query.keyword,
  async (next, prev) => {
    if (prev && !next) {
      await loadAnnouncements()
      await loadUnreadCount()
    }
    if (next !== prev) {
      pageNo.value = 1
    }
  }
)

watch(
  () => sortedAnnouncements.value.length,
  (total) => {
    const maxPage = Math.max(1, Math.ceil(total / pageSize))
    if (pageNo.value > maxPage) {
      pageNo.value = maxPage
    }
  }
)
</script>

<style scoped>
.notice-kpi {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.kpi-item {
  border: 1px solid #d7e2ea;
  border-radius: 12px;
  padding: 14px;
  background: #f6fafd;
}

.kpi-label {
  color: #4b677f;
  font-size: 13px;
}

.kpi-value {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 800;
  color: #1d3f5d;
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

.detail-reminder {
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #d9e8f4;
  background: #f3f8fd;
  color: #36546b;
  font-size: 13px;
}

.detail-head h3 {
  margin: 0;
  font-size: 24px;
  line-height: 1.35;
  color: #102e46;
}

.detail-meta {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #5b7387;
  font-size: 13px;
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

.read-stat {
  color: #35546d;
  font-size: 12px;
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

.pager-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

:deep(.notice-row-unread td.el-table__cell) {
  background: #f6fbff !important;
}

@media (max-width: 980px) {
  .notice-kpi {
    grid-template-columns: 1fr;
  }
}
</style>
