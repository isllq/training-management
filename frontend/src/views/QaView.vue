<template>
  <div class="qa-page">
    <div class="module-head page-card">
      <div class="module-head-title">实训在线答疑</div>
      <div class="module-head-desc">沉淀师生问答记录，关联开设计划追踪问题处理过程。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-if="canModerate" v-model="query.className" placeholder="选择班级（默认全部）" clearable filterable>
          <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button type="primary" @click="openThreadCreate">新建问题主题</el-button>
      </div>
      <el-table :data="threads" border stripe :row-class-name="rowClassName">
        <el-table-column label="所属开设" min-width="170">
          <template #default="{ row }">{{ publishName(row.publishId) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="问题主题" min-width="180" />
        <el-table-column prop="content" label="问题描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="回复状态" width="120">
          <template #default="{ row }">
            <el-tag :type="(row.replyCount || 0) > 0 ? 'success' : 'warning'">
              {{ (row.replyCount || 0) > 0 ? `已回复(${row.replyCount})` : '待回复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发起人" width="120">
          <template #default="{ row }">{{ row.creatorName || userName(row.creatorId) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '开放中' : '已关闭' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-if="canEditThread(row)" size="small" @click="openThreadEdit(row)">编辑</el-button>
            <el-button size="small" @click="openReplies(row)">查看回复</el-button>
            <el-button v-if="canDeleteThread(row)" size="small" type="danger" @click="removeThread(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="threadDialog" :title="threadForm.id ? '编辑问题主题' : '新建问题主题'" width="580px">
      <el-form :model="threadForm" label-width="95px" class="dialog-form">
        <el-form-item label="所属开设">
          <el-select v-model="threadForm.publishId" placeholder="请选择开设计划" filterable>
            <el-option v-for="item in publishOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题标题"><el-input v-model="threadForm.title" /></el-form-item>
        <el-form-item label="问题内容"><el-input v-model="threadForm.content" type="textarea" /></el-form-item>
        <el-form-item label="主题状态">
          <el-radio-group v-model="threadForm.status">
            <el-radio :label="1">开放</el-radio>
            <el-radio :label="2">关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="threadDialog = false">取消</el-button>
        <el-button type="primary" @click="submitThread">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="replyDrawer" title="主题回复记录" size="460px">
      <div class="toolbar">
        <el-input v-model="replyContent" placeholder="输入回复内容" />
        <el-button type="primary" @click="submitReply">发送回复</el-button>
      </div>
      <el-table :data="replies" border stripe>
        <el-table-column label="回复人" width="120">
          <template #default="{ row }">{{ row.creatorName || userName(row.creatorId) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="回复内容" />
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="canDeleteReply(row)" link type="danger" @click="removeReply(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createQaReplyApi,
  createQaThreadApi,
  deleteQaReplyApi,
  deleteQaThreadApi,
  listQaRepliesApi,
  listQaThreadsApi,
  updateQaThreadApi
} from '../api/qa'
import { listPublishesApi } from '../api/projects'
import { listUserOptionsApi } from '../api/auth'
import { ROLE, getUserId, hasAnyRole } from '../utils/auth'
import { toNameMap, publishLabel, fallbackName } from '../utils/dicts'

const canModerate = computed(() => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER]))
const currentUserId = computed(() => getUserId())

const query = reactive({ className: '' })
const threads = ref([])
const publishes = ref([])
const users = ref([])

const threadDialog = ref(false)
const threadForm = reactive({
  id: null,
  publishId: null,
  title: '',
  content: '',
  status: 1
})

const replyDrawer = ref(false)
const currentThreadId = ref(null)
const replies = ref([])
const replyContent = ref('')

const publishOptions = computed(() => publishes.value.map((item) => ({ value: item.id, label: publishLabel(item) })))
const classOptions = computed(() => {
  const classSet = new Set()
  publishes.value.forEach((item) => {
    const raw = item.className || ''
    raw
      .split(/[,，;；/\s]+/)
      .map((name) => name.trim())
      .filter(Boolean)
      .forEach((name) => classSet.add(name))
  })
  return Array.from(classSet).sort().map((name) => ({ value: name, label: name }))
})
const publishNameMap = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = publishLabel(item)
  })
  return map
})
const userNameMap = computed(() => toNameMap(users.value, 'id', 'name'))

const publishName = (id) => fallbackName(id, publishNameMap.value, '开设')
const userName = (id) => fallbackName(id, userNameMap.value, '用户')

const canEditThread = (row) => canModerate.value || row.creatorId === currentUserId.value
const canDeleteThread = (row) => canModerate.value || row.creatorId === currentUserId.value
const canDeleteReply = (row) => canModerate.value || row.creatorId === currentUserId.value
const rowClassName = ({ row }) => (canModerate.value && (row.replyCount || 0) === 0 ? 'qa-row-unreplied' : '')

const loadDicts = async () => {
  publishes.value = await listPublishesApi({})
  users.value = await listUserOptionsApi({})
}

const loadThreads = async () => {
  const params = {}
  if (canModerate.value && query.className) params.className = query.className
  threads.value = await listQaThreadsApi(params)
}

const openThreadCreate = () => {
  let defaultPublishId = publishOptions.value.length ? publishOptions.value[0].value : null
  if (canModerate.value && query.className) {
    const matched = publishes.value.find((item) => {
      const classNames = String(item.className || '')
        .split(/[,，;；/\s]+/)
        .map((name) => name.trim())
        .filter(Boolean)
      return classNames.includes(query.className)
    })
    if (matched) {
      defaultPublishId = matched.id
    }
  }
  Object.assign(threadForm, {
    id: null,
    publishId: defaultPublishId,
    title: '',
    content: '',
    status: 1
  })
  threadDialog.value = true
}

const openThreadEdit = (row) => {
  if (!canEditThread(row)) {
    ElMessage.warning('只能编辑自己发起的问题主题')
    return
  }
  Object.assign(threadForm, row)
  threadDialog.value = true
}

const submitThread = async () => {
  if (!threadForm.publishId || !threadForm.title) {
    ElMessage.warning('请先选择开设计划并填写问题标题')
    return
  }
  if (threadForm.id) await updateQaThreadApi(threadForm.id, threadForm)
  else await createQaThreadApi(threadForm)
  threadDialog.value = false
  ElMessage.success('主题保存成功')
  await loadThreads()
}

const removeThread = async (id) => {
  const row = threads.value.find((item) => item.id === id)
  if (row && !canDeleteThread(row)) {
    ElMessage.warning('只能删除自己发起的问题主题')
    return
  }
  await ElMessageBox.confirm('确认删除该问题主题吗？', '提示', { type: 'warning' })
  await deleteQaThreadApi(id)
  ElMessage.success('主题删除成功')
  await loadThreads()
}

const openReplies = async (row) => {
  currentThreadId.value = row.id
  replyDrawer.value = true
  replies.value = await listQaRepliesApi(row.id)
}

const submitReply = async () => {
  if (!replyContent.value || !currentThreadId.value) {
    return
  }
  await createQaReplyApi(currentThreadId.value, { content: replyContent.value })
  replyContent.value = ''
  replies.value = await listQaRepliesApi(currentThreadId.value)
  ElMessage.success('回复发送成功')
}

const removeReply = async (id) => {
  const row = replies.value.find((item) => item.id === id)
  if (row && !canDeleteReply(row)) {
    ElMessage.warning('只能删除自己的回复')
    return
  }
  await deleteQaReplyApi(id)
  replies.value = await listQaRepliesApi(currentThreadId.value)
}

onMounted(async () => {
  await loadDicts()
  await loadThreads()
})

watch(() => query.className, async () => {
  await loadThreads()
})
</script>

<style scoped>
.qa-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

:deep(.qa-row-unreplied td.el-table__cell) {
  background: #fff8ea !important;
}
</style>
