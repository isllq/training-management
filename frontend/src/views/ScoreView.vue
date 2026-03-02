<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">综合成绩管理</div>
      <div class="module-head-desc">支持教师按科目和班级筛选后录入成绩，按学期查看成绩单。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.termName" placeholder="按学期筛选" clearable filterable>
          <el-option v-for="item in termOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.projectId" placeholder="按科目筛选" clearable filterable>
          <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.className" placeholder="按班级筛选" clearable filterable>
          <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button v-if="canManageScore" type="primary" @click="openCreate">录入成绩</el-button>
        <el-button v-if="canManageScore" @click="exportScores">导出成绩单</el-button>
      </div>

      <el-table :data="filteredScores" border stripe>
        <el-table-column label="科目名称" min-width="190">
          <template #default="{ row }">{{ row.projectName || projectNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="所属学期" width="130">
          <template #default="{ row }">{{ row.termName || termNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="开设班级" width="140">
          <template #default="{ row }">{{ row.className || classNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="成绩类型" width="170">
          <template #default>综合成绩（平时+任务+报告）</template>
        </el-table-column>
        <el-table-column label="学生姓名" width="120">
          <template #default="{ row }">{{ row.studentName || studentName(row.studentId) }}</template>
        </el-table-column>
        <el-table-column prop="usualScore" label="平时表现" width="100" />
        <el-table-column prop="taskScore" label="任务完成" width="100" />
        <el-table-column prop="reportScore" label="报告答辩" width="100" />
        <el-table-column prop="finalScore" label="综合总评" width="100" />
        <el-table-column v-if="canManageScore" label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-if="canManageScore"
      v-model="dialogVisible"
      :title="form.id ? '编辑成绩' : '录入成绩'"
      width="620px"
      append-to-body
    >
      <el-form :model="form" label-width="95px" class="dialog-form">
        <el-form-item label="成绩类型">
          <el-tag type="info">综合成绩（平时+任务+报告）</el-tag>
        </el-form-item>
        <el-form-item label="科目筛选">
          <el-select v-model="formFilter.projectId" filterable clearable placeholder="请选择科目" @change="onFormFilterChange">
            <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级筛选">
          <el-select v-model="formFilter.className" filterable clearable placeholder="请选择班级" @change="onFormFilterChange">
            <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开设计划">
          <el-select v-model="form.publishId" filterable placeholder="请选择项目/班级/学期">
            <el-option-group v-for="group in publishOptionGroups" :key="group.label" :label="group.label">
              <el-option v-for="item in group.options" :key="item.value" :label="item.label" :value="item.value" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-select v-model="form.studentId" filterable placeholder="请选择学生">
            <el-option v-for="item in dialogStudentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="平时表现"><el-input-number v-model="form.usualScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="任务完成"><el-input-number v-model="form.taskScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="报告答辩"><el-input-number v-model="form.reportScore" :min="0" :max="100" :precision="2" /></el-form-item>
        <el-form-item label="综合总评"><el-input-number v-model="form.finalScore" :min="0" :max="100" :precision="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createScoreApi, deleteScoreApi, listScoresApi, updateScoreApi } from '../api/scores'
import { listProjectsApi, listPublishesApi } from '../api/projects'
import { listUserOptionsApi } from '../api/auth'
import { ROLE, hasAnyRole } from '../utils/auth'
import { toNameMap, fallbackName } from '../utils/dicts'

const canManageScore = computed(() => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER]))

const query = reactive({ termName: '', projectId: null, className: '' })
const scores = ref([])
const projects = ref([])
const publishes = ref([])
const allStudents = ref([])
const dialogStudents = ref([])
const dialogVisible = ref(false)
const form = reactive({
  id: null,
  publishId: null,
  studentId: null,
  usualScore: 85,
  taskScore: 88,
  reportScore: 90,
  finalScore: 88
})
const formFilter = reactive({ projectId: null, className: '' })

const projectNameMap = computed(() => toNameMap(projects.value, 'id', 'projectName'))
const studentNameMap = computed(() => toNameMap(allStudents.value, 'id', 'name'))
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
    projectId: item.projectId || null,
    className: item.className || '',
    termName: item.termName || ''
  }))
)
const filteredPublishOptions = computed(() =>
  publishOptions.value.filter((item) => {
    if (formFilter.projectId && item.projectId !== formFilter.projectId) return false
    if (formFilter.className && item.className !== formFilter.className) return false
    return true
  })
)
const publishOptionGroups = computed(() => {
  const groupMap = {}
  filteredPublishOptions.value.forEach((item) => {
    const term = item.termName || '未设学期'
    if (!groupMap[term]) {
      groupMap[term] = {
        label: term,
        options: []
      }
    }
    groupMap[term].options.push(item)
  })
  return Object.values(groupMap)
})
const termOptions = computed(() => {
  const terms = Array.from(new Set(publishes.value.map((item) => item.termName).filter((item) => item && item.trim())))
  return terms.sort((a, b) => String(b).localeCompare(String(a))).map((item) => ({ value: item, label: item }))
})
const projectOptions = computed(() =>
  projects.value.map((item) => ({ value: item.id, label: item.projectName || `科目${item.id}` }))
)
const classOptions = computed(() => {
  const classes = Array.from(new Set(publishes.value.map((item) => item.className).filter((item) => item && item.trim())))
  return classes.sort((a, b) => String(a).localeCompare(String(b))).map((item) => ({ value: item, label: item }))
})
const dialogStudentOptions = computed(() =>
  dialogStudents.value.map((item) => ({ value: item.id, label: item.name || `学生${item.id}` }))
)

const filteredScores = computed(() =>
  scores.value.filter((item) => {
    const publishId = item.publishId
    const term = item.termName || termNameByPublish(publishId)
    const clazz = item.className || classNameByPublish(publishId)
    const projectId = projectIdByPublish(publishId)
    if (query.termName && term !== query.termName) return false
    if (query.className && clazz !== query.className) return false
    if (query.projectId && projectId !== query.projectId) return false
    return true
  })
)

const projectName = (id) => fallbackName(id, projectNameMap.value, '科目')
const studentName = (id) => fallbackName(id, studentNameMap.value, '学生')
const termNameByPublish = (id) => publishDetailMap.value[id]?.termName || '-'
const classNameByPublish = (id) => publishDetailMap.value[id]?.className || '-'
const projectIdByPublish = (id) => publishDetailMap.value[id]?.projectId || null
const projectNameByPublish = (id) => {
  const pid = projectIdByPublish(id)
  return pid ? projectName(pid) : fallbackName(id, {}, '开设')
}

const publishOptionLabel = (item) => {
  const projectText = projectName(item.projectId)
  const classText = item.className || '未设班级'
  const termText = item.termName || '未设学期'
  return `${projectText}｜${classText}｜${termText}`
}

const firstPublishMatch = ({ termName = '', projectId = null, className = '' } = {}) => {
  const hit = publishes.value.find((item) => {
    if (termName && item.termName !== termName) return false
    if (projectId && item.projectId !== projectId) return false
    if (className && item.className !== className) return false
    return true
  })
  return hit ? hit.id : null
}

const syncFormFilterByPublish = (publishId) => {
  const detail = publishDetailMap.value[publishId]
  if (!detail) return
  formFilter.projectId = detail.projectId
  formFilter.className = detail.className
}

const loadDialogStudentsByClass = async (className) => {
  const params = { userType: 'STUDENT' }
  if (className) {
    params.className = className
  }
  dialogStudents.value = await listUserOptionsApi(params)
}

const onFormFilterChange = async () => {
  const inScope = filteredPublishOptions.value.some((item) => item.value === form.publishId)
  if (!inScope) {
    form.publishId = filteredPublishOptions.value.length ? filteredPublishOptions.value[0].value : null
  }
  if (form.publishId) {
    syncFormFilterByPublish(form.publishId)
  }
  await loadDialogStudentsByClass(formFilter.className)
  if (!dialogStudentOptions.value.some((item) => item.value === form.studentId)) {
    form.studentId = dialogStudentOptions.value.length ? dialogStudentOptions.value[0].value : null
  }
}

const loadDicts = async () => {
  projects.value = await listProjectsApi({})
  publishes.value = await listPublishesApi({})
  allStudents.value = await listUserOptionsApi({ userType: 'STUDENT' })
  if (!query.termName && termOptions.value.length) {
    query.termName = termOptions.value[0].value
  }
}

const loadScores = async () => {
  scores.value = await listScoresApi({})
}

const openCreate = async () => {
  formFilter.projectId = query.projectId
  formFilter.className = query.className
  const defaultPublishId = firstPublishMatch({
    termName: query.termName,
    projectId: formFilter.projectId,
    className: formFilter.className
  }) || (filteredPublishOptions.value.length ? filteredPublishOptions.value[0].value : null)
  Object.assign(form, {
    id: null,
    publishId: defaultPublishId,
    studentId: null,
    usualScore: 85,
    taskScore: 88,
    reportScore: 90,
    finalScore: 88
  })
  if (form.publishId) {
    syncFormFilterByPublish(form.publishId)
  }
  await loadDialogStudentsByClass(formFilter.className)
  form.studentId = dialogStudentOptions.value.length ? dialogStudentOptions.value[0].value : null
  dialogVisible.value = true
}

const openEdit = async (row) => {
  Object.assign(form, row)
  syncFormFilterByPublish(row.publishId)
  await loadDialogStudentsByClass(formFilter.className)
  if (!dialogStudentOptions.value.some((item) => item.value === form.studentId)) {
    dialogStudents.value = await listUserOptionsApi({ userType: 'STUDENT' })
  }
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.publishId || !form.studentId) {
    ElMessage.warning('请先按科目和班级选择开设计划与学生')
    return
  }
  if (form.id) {
    await updateScoreApi(form.id, form)
  } else {
    await createScoreApi(form)
  }
  ElMessage.success('成绩保存成功')
  dialogVisible.value = false
  await loadScores()
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该成绩记录吗？', '提示', { type: 'warning' })
  await deleteScoreApi(id)
  ElMessage.success('成绩删除成功')
  await loadScores()
}

const csvCell = (value) => {
  if (value == null) return '""'
  const text = String(value).replace(/"/g, '""')
  return `"${text}"`
}

const exportScores = () => {
  const rows = filteredScores.value
  const lines = [
    ['科目名称', '所属学期', '开设班级', '成绩类型', '学生姓名', '平时表现', '任务完成', '报告答辩', '综合总评']
      .map(csvCell)
      .join(',')
  ]
  rows.forEach((row) => {
    lines.push(
      [
        row.projectName || projectNameByPublish(row.publishId),
        row.termName || termNameByPublish(row.publishId),
        row.className || classNameByPublish(row.publishId),
        '综合成绩（平时+任务+报告）',
        row.studentName || studentName(row.studentId),
        row.usualScore,
        row.taskScore,
        row.reportScore,
        row.finalScore
      ]
        .map(csvCell)
        .join(',')
    )
  })
  const content = `\uFEFF${lines.join('\n')}`
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = query.termName ? `${query.termName}_成绩单.csv` : '成绩单.csv'
  a.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('成绩单导出成功')
}

watch(
  () => form.publishId,
  async (value, oldValue) => {
    if (!dialogVisible.value || !value || value === oldValue) return
    syncFormFilterByPublish(value)
    await loadDialogStudentsByClass(formFilter.className)
    if (!dialogStudentOptions.value.some((item) => item.value === form.studentId)) {
      form.studentId = dialogStudentOptions.value.length ? dialogStudentOptions.value[0].value : null
    }
  }
)

onMounted(async () => {
  await loadDicts()
  await loadScores()
})
</script>
