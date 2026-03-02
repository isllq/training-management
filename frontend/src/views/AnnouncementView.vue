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
        <div class="kpi-label">当前公告总数</div>
        <div class="kpi-value">{{ announcements.length }}</div>
      </div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.publishId" placeholder="选择开设计划" clearable filterable>
          <el-option v-for="item in publishOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input v-model="query.keyword" placeholder="按公告标题或内容检索" clearable @keyup.enter="loadAnnouncements" />
        <el-button v-if="canManage" type="primary" @click="openCreate">发布公告</el-button>
      </div>

      <el-table :data="announcements" border stripe :row-class-name="rowClassName">
        <el-table-column prop="title" label="公告标题" min-width="240" show-overflow-tooltip />
        <el-table-column label="适用开设" min-width="180">
          <template #default="{ row }">{{ publishName(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="发布人" width="120">
          <template #default="{ row }">{{ row.authorName || userName(row.authorId) }}</template>
        </el-table-column>
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTag(row.priority)">{{ priorityText(row.priority) }}</el-tag>
          </template>
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
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">查看详情</el-button>
            <el-button v-if="canEditRow(row)" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canEditRow(row)" size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
        <el-form-item label="优先级">
          <el-select v-model="form.priority">
            <el-option label="普通" :value="1" />
            <el-option label="重要" :value="2" />
            <el-option label="紧急" :value="3" />
          </el-select>
        </el-form-item>
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
import { publishLabel, fallbackName, toNameMap } from '../utils/dicts'

const currentUserId = computed(() => getUserId())
const canManage = computed(() => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER]))
const isAdmin = computed(() => hasAnyRole([ROLE.ADMIN]))

const query = reactive({ publishId: null, keyword: '' })
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
  priority: 1,
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

const publishOptions = computed(() => publishes.value.map((item) => ({ value: item.id, label: publishLabel(item) })))
const publishNameMap = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = publishLabel(item)
  })
  return map
})
const userNameMap = computed(() => toNameMap(users.value, 'id', 'name'))

const publishName = (id) => {
  if (id == null) return '全体学生'
  return fallbackName(id, publishNameMap.value, '开设')
}
const userName = (id) => fallbackName(id, userNameMap.value, '用户')

const priorityText = (priority) => {
  if (priority === 3) return '紧急'
  if (priority === 2) return '重要'
  return '普通'
}

const priorityTag = (priority) => {
  if (priority === 3) return 'danger'
  if (priority === 2) return 'warning'
  return 'info'
}

const canEditRow = (row) => canManage.value && (isAdmin.value || row.authorId === currentUserId.value)

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
    publishId: null,
    title: '',
    content: '',
    priority: 1,
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
  Object.assign(form, row)
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
    let announcementId = form.id
    if (form.id) {
      await updateAnnouncementApi(form.id, form)
    } else {
      const created = await createAnnouncementApi(form)
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
  () => query.publishId,
  async () => {
    await loadAnnouncements()
    await loadUnreadCount()
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
  gap: 14px;
}

.detail-head h3 {
  margin: 0;
  font-size: 20px;
  color: #1a334a;
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
  border-radius: 10px;
  padding: 12px;
  color: #2d475c;
  line-height: 1.7;
  white-space: pre-wrap;
}

.detail-files-title {
  font-weight: 700;
  color: #1f3f5a;
  margin-bottom: 8px;
}

.read-stat {
  color: #35546d;
  font-size: 12px;
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
