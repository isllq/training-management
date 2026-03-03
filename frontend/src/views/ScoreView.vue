<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">综合成绩管理</div>
      <div class="module-head-desc">按单门实训科目集中录入并跟踪未录入学生，支持批量导入成绩。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.termName" placeholder="学期（全部）" filterable @change="onFilterChange">
          <el-option v-for="item in termOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.projectId" placeholder="科目（全部）" clearable filterable @change="onFilterChange">
          <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="query.className" placeholder="班级（全部）" clearable filterable @change="onFilterChange">
          <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button v-if="canManageScore" type="primary" @click="openCreate()">录入成绩</el-button>
        <el-button v-if="canManageScore" @click="openImport">导入成绩</el-button>
        <el-button @click="exportScores">导出成绩单</el-button>
      </div>

      <el-alert
        v-if="canManageScore && !query.projectId"
        type="info"
        :closable="false"
        title="请先选择科目，再按班级查看与录入成绩。"
        style="margin-bottom: 12px"
      />
      <el-alert
        v-if="canManageScore && query.projectId && selectedPublishes.length === 0"
        type="info"
        :closable="false"
        title="当前筛选条件下没有开设计划，请调整学期或班级。"
        style="margin-bottom: 12px"
      />
      <el-alert
        v-if="canManageScore && query.projectId && selectedPublishes.length > 0 && scores.length === 0"
        type="info"
        :closable="false"
        title="当前筛选范围尚未录入成绩，请开始录入或使用导入功能。"
        style="margin-bottom: 12px"
      />
      <el-alert
        v-if="canManageScore && query.projectId && selectedPublishes.length > 0"
        :type="missingRows.length ? 'warning' : 'success'"
        :closable="false"
        :title="missingTip"
        style="margin-bottom: 12px"
      />

      <el-table v-if="canManageScore" :data="manageRows" border stripe>
        <el-table-column prop="studentName" label="学生姓名" width="130" />
        <el-table-column prop="className" label="班级" width="130" />
        <el-table-column label="录入状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.recorded ? 'success' : 'warning'">{{ row.recorded ? '已录入' : '未录入' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过程得分" width="110">
          <template #default="{ row }">{{ row.usualScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="团队协作得分" width="130">
          <template #default="{ row }">{{ row.taskScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="答辩得分" width="110">
          <template #default="{ row }">{{ row.reportScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="综合总评" width="110">
          <template #default="{ row }">{{ row.finalScore ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openCreate(row)">{{ row.recorded ? '编辑' : '录入' }}</el-button>
            <el-button v-if="row.recorded" size="small" type="danger" @click="remove(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="studentScores" border stripe>
        <el-table-column label="科目名称" min-width="180">
          <template #default="{ row }">{{ row.projectName || publishName(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="学期" width="130">
          <template #default="{ row }">{{ row.termName || '-' }}</template>
        </el-table-column>
        <el-table-column label="班级" width="130">
          <template #default="{ row }">{{ row.className || '-' }}</template>
        </el-table-column>
        <el-table-column prop="usualScore" label="过程得分" width="110" />
        <el-table-column prop="taskScore" label="团队协作得分" width="130" />
        <el-table-column prop="reportScore" label="答辩得分" width="110" />
        <el-table-column prop="finalScore" label="综合总评" width="110" />
      </el-table>
    </div>

    <el-dialog
      v-if="canManageScore"
      v-model="dialogVisible"
      :title="form.id ? '编辑成绩' : '录入成绩'"
      width="620px"
      append-to-body
    >
      <el-form :model="form" label-width="105px" class="dialog-form">
        <el-form-item label="实训科目">
          <el-select v-model="form.publishId" filterable placeholder="请选择实训科目" :disabled="studentLocked" @change="onFormPublishChange">
            <el-option v-for="item in dialogPublishOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-select v-model="form.studentId" filterable placeholder="请选择学生" :disabled="studentLocked">
            <el-option v-for="item in publishStudentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="过程得分">
          <el-input-number v-model="form.usualScore" :min="0" :max="100" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="团队协作得分">
          <el-input-number v-model="form.taskScore" :min="0" :max="100" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="答辩得分">
          <el-input-number v-model="form.reportScore" :min="0" :max="100" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="综合总评">
          <el-input :model-value="previewFinalScore" disabled placeholder="保存后自动计算" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-if="canManageScore" v-model="importVisible" title="导入成绩" width="620px" append-to-body>
      <el-form label-width="120px">
        <el-form-item label="默认实训科目">
          <el-select v-model="importPublishId" filterable clearable placeholder="可选：用于文件中未填写开设信息时兜底">
            <el-option v-for="item in importPublishOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="导入文件">
          <el-upload
            v-model:file-list="importFileList"
            :auto-upload="false"
            :limit="1"
            :on-change="onImportFileChange"
            :on-remove="onImportFileRemove"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 .xlsx/.xls/.csv。建议列名包含：学期、班级、项目名称、学生ID(或学生姓名)、过程得分、团队协作得分、答辩得分。
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <el-alert
        v-if="importResult"
        type="info"
        :closable="false"
        :title="`总行数 ${importResult.totalRows}，成功 ${importResult.successRows}，失败 ${importResult.failedRows}`"
      />
      <div v-if="importResult && importResult.errors && importResult.errors.length" class="import-errors">
        <div class="import-errors-title">失败明细（最多展示30条）</div>
        <ul>
          <li v-for="item in importResult.errors" :key="item">{{ item }}</li>
        </ul>
      </div>
      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importing" @click="submitImport">开始导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createScoreApi, deleteScoreApi, getScoreFormulaApi, importScoresApi, listScoresApi, updateScoreApi } from '../api/scores'
import { listProjectsApi, listPublishesApi } from '../api/projects'
import { listUserOptionsApi } from '../api/auth'
import { ROLE, hasAnyRole } from '../utils/auth'
import { fallbackName, toNameMap } from '../utils/dicts'

const canManageScore = computed(() => hasAnyRole([ROLE.ADMIN, ROLE.TEACHER]))
const query = reactive({
  termName: 'ALL',
  projectId: null,
  className: ''
})

const publishes = ref([])
const projects = ref([])
const allStudents = ref([])
const scores = ref([])
const formula = ref({ alpha: 0.4, beta: 0.3, gamma: 0.3 })

const dialogVisible = ref(false)
const saving = ref(false)
const studentLocked = ref(false)
const form = reactive({
  id: null,
  publishId: null,
  studentId: null,
  usualScore: null,
  taskScore: null,
  reportScore: null
})

const importVisible = ref(false)
const importing = ref(false)
const importPublishId = ref(null)
const importFileList = ref([])
const importFile = ref(null)
const importResult = ref(null)

const projectNameMap = computed(() => toNameMap(projects.value, 'id', 'projectName'))
const publishById = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = item
  })
  return map
})
const publishNameMap = computed(() => {
  const map = {}
  publishes.value.forEach((item) => {
    map[item.id] = compactPublishLabel(item, true)
  })
  return map
})

const termOptions = computed(() => {
  const set = new Set()
  publishes.value.forEach((item) => {
    if (item.termName) set.add(item.termName)
  })
  const options = Array.from(set)
    .sort((a, b) => String(b).localeCompare(String(a)))
    .map((value) => ({ value, label: value }))
  return [{ value: 'ALL', label: '全部学期' }, ...options]
})

const termFilteredPublishes = computed(() =>
  publishes.value.filter((item) => {
    if (query.termName === 'ALL') return true
    return String(item.termName || '') === String(query.termName)
  })
)

const projectOptions = computed(() => {
  const projectIdSet = new Set(termFilteredPublishes.value.map((item) => item.projectId).filter((id) => id != null))
  const options = projects.value
    .filter((item) => projectIdSet.has(item.id))
    .map((item) => ({ value: item.id, label: item.projectName || `项目${item.id}` }))
    .sort((a, b) => String(a.label).localeCompare(String(b.label)))
  return [{ value: null, label: '全部科目' }, ...options]
})

const projectFilteredPublishes = computed(() => {
  if (!query.projectId) return []
  return termFilteredPublishes.value.filter((item) => item.projectId === query.projectId)
})

const classOptions = computed(() => {
  const set = new Set()
  projectFilteredPublishes.value.forEach((item) => {
    parseClassScope(item.className).forEach((name) => set.add(name))
  })
  return Array.from(set)
    .sort((a, b) => String(a).localeCompare(String(b)))
    .map((value) => ({ value, label: value }))
})

const selectedPublishes = computed(() => {
  if (!query.projectId) return []
  if (!query.className) return projectFilteredPublishes.value
  return projectFilteredPublishes.value.filter((item) => matchesClassScope(query.className, item.className))
})

const allPublishOptions = computed(() =>
  publishes.value
    .map((item) => ({ value: item.id, label: compactPublishLabel(item, true) }))
    .sort((a, b) => (b.value || 0) - (a.value || 0))
)

const dialogPublishOptions = computed(() => selectedPublishes.value.map((item) => ({
  value: item.id,
  label: compactPublishLabel(item, query.termName === 'ALL')
})))

const importPublishOptions = computed(() => {
  if (selectedPublishes.value.length) {
    return selectedPublishes.value.map((item) => ({
      value: item.id,
      label: compactPublishLabel(item, query.termName === 'ALL')
    }))
  }
  if (query.projectId) {
    return projectFilteredPublishes.value.map((item) => ({
      value: item.id,
      label: compactPublishLabel(item, query.termName === 'ALL')
    }))
  }
  if (query.termName !== 'ALL') {
    return termFilteredPublishes.value.map((item) => ({
      value: item.id,
      label: compactPublishLabel(item, false)
    }))
  }
  return allPublishOptions.value
})

const scoreByStudentMap = computed(() => {
  const map = {}
  scores.value.forEach((item) => {
    if (item.publishId != null && item.studentId != null) {
      map[`${item.publishId}_${item.studentId}`] = item
    }
  })
  return map
})

const studentsByPublish = computed(() => {
  const map = {}
  const publishIdSet = new Set(selectedPublishes.value.map((item) => item.id))
  if (form.publishId != null) {
    publishIdSet.add(form.publishId)
  }
  Array.from(publishIdSet).forEach((publishId) => {
    const publish = publishById.value[publishId]
    if (!publish) return
    const scopes = parseClassScope(publish.className)
    const list = !scopes.length
      ? [...allStudents.value]
      : allStudents.value.filter((item) => scopes.includes(String(item.className || '').trim()))
    map[publish.id] = list
  })
  return map
})

const manageRows = computed(() => {
  if (!canManageScore.value || !query.projectId || !selectedPublishes.value.length) {
    return []
  }
  const rows = []
  selectedPublishes.value.forEach((publish) => {
    const students = studentsByPublish.value[publish.id] || []
    students.forEach((student) => {
      const score = scoreByStudentMap.value[`${publish.id}_${student.id}`] || null
      rows.push({
        id: score ? score.id : null,
        publishId: publish.id,
        projectName: projectName(publish.projectId),
        termName: publish.termName || '-',
        studentId: student.id,
        studentName: student.name || `学生${student.id}`,
        className: student.className || '-',
        recorded: !!score,
        usualScore: score ? score.usualScore : null,
        taskScore: score ? score.taskScore : null,
        reportScore: score ? score.reportScore : null,
        finalScore: score ? score.finalScore : null
      })
    })
  })
  return rows.sort((a, b) => {
    const c1 = String(a.className || '')
    const c2 = String(b.className || '')
    if (c1 !== c2) return c1.localeCompare(c2)
    return String(a.studentName || '').localeCompare(String(b.studentName || ''))
  })
})

const missingRows = computed(() => manageRows.value.filter((item) => !item.recorded))

const missingTip = computed(() => {
  if (!query.projectId) {
    return '请先选择科目'
  }
  if (!selectedPublishes.value.length) {
    return '当前筛选条件下没有开设计划'
  }
  const total = manageRows.value.length
  const recorded = total - missingRows.value.length
  if (!missingRows.value.length) {
    return `当前范围共 ${total} 名学生，已全部录入完成。`
  }
  return `当前范围共 ${total} 名学生，已录入 ${recorded} 名，未录入 ${missingRows.value.length} 名。`
})

const publishStudentOptions = computed(() =>
  (studentsByPublish.value[form.publishId] || []).map((item) => ({ value: item.id, label: `${item.name}（${item.className || '-'}）` }))
)

const previewFinalScore = computed(() => {
  if (form.usualScore == null || form.taskScore == null || form.reportScore == null) {
    return ''
  }
  const publish = publishById.value[form.publishId] || null
  const weights = resolveWeightsByPublish(publish)
  const usual = Number(form.usualScore || 0)
  const task = Number(form.taskScore || 0)
  const report = Number(form.reportScore || 0)
  const total = usual * Number(weights.alpha) +
    task * Number(weights.beta) +
    report * Number(weights.gamma)
  return total.toFixed(2)
})

const studentScores = computed(() =>
  scores.value.filter((item) => {
    const publish = publishById.value[item.publishId] || null
    if (query.termName !== 'ALL' && String(publish?.termName || item.termName || '') !== String(query.termName)) {
      return false
    }
    if (query.projectId && Number(publish?.projectId) !== Number(query.projectId)) {
      return false
    }
    if (query.className && !matchesClassScope(query.className, item.className || publish?.className || '')) {
      return false
    }
    return true
  })
)

const projectName = (id) => fallbackName(id, projectNameMap.value, '科目')
const publishName = (id) => fallbackName(id, publishNameMap.value, '开设')
const compactPublishLabel = (publish, includeTerm) => {
  if (!publish) return '-'
  const projectText = projectName(publish.projectId)
  const classText = publish.className || '未设班级'
  return `${projectText} · ${classText}`
}

const resolveWeightsByPublish = (publish) => {
  const fallback = {
    alpha: Number(formula.value.alpha || 0.4),
    beta: Number(formula.value.beta || 0.3),
    gamma: Number(formula.value.gamma || 0.3)
  }
  if (!publish) return fallback

  const p = Number(publish.processWeight)
  const t = Number(publish.teamWeight)
  const f = Number(publish.finalWeight)
  if (!Number.isNaN(p) && !Number.isNaN(t) && !Number.isNaN(f)) {
    const sum = p + t + f
    if (Math.abs(sum - 1) <= 0.0001) {
      return { alpha: p, beta: t, gamma: f }
    }
  }

  const text = String(publish.assessmentStandard || '')
  if (text) {
    const regex = /([^\d%]+)\s*(\d+(?:\.\d+)?)\s*%/g
    let process = 0
    let team = 0
    let fin = 0
    let matched = false
    let m
    while ((m = regex.exec(text)) !== null) {
      const label = String(m[1] || '').trim()
      const value = Number(m[2] || 0) / 100
      if (label.includes('过程') || label.includes('平时') || label.includes('文档') || label.includes('阶段')) {
        process += value
        matched = true
        continue
      }
      if (label.includes('团队') || label.includes('协作') || label.includes('任务') || label.includes('互评')) {
        team += value
        matched = true
        continue
      }
      if (label.includes('答辩') || label.includes('结题') || label.includes('成果') || label.includes('报告') || label.includes('终')) {
        fin += value
        matched = true
      }
    }
    if (matched && Math.abs(process + team + fin - 1) <= 0.0001) {
      return { alpha: process, beta: team, gamma: fin }
    }
  }

  return fallback
}

const parseClassScope = (value) => {
  if (!value) return []
  return String(value)
    .split(/[,，;；/\s、]+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

const matchesClassScope = (className, scopeText) => {
  const target = String(className || '').trim()
  if (!target) return false
  const scopes = parseClassScope(scopeText)
  if (!scopes.length) return true
  return scopes.some((item) => item === target || item.includes(target) || target.includes(item))
}

const loadDicts = async () => {
  const [publishData, projectData, studentData] = await Promise.all([
    listPublishesApi({}),
    listProjectsApi({}),
    listUserOptionsApi({ userType: 'STUDENT' })
  ])
  publishes.value = publishData || []
  projects.value = projectData || []
  allStudents.value = studentData || []
}

const loadFormula = async () => {
  try {
    formula.value = await getScoreFormulaApi()
  } catch (error) {
    formula.value = { alpha: 0.4, beta: 0.3, gamma: 0.3 }
  }
}

const loadScores = async () => {
  if (canManageScore.value) {
    if (!query.projectId || !selectedPublishes.value.length) {
      scores.value = []
      return
    }
    const dataList = await Promise.all(
      selectedPublishes.value.map((item) => listScoresApi({ publishId: item.id }))
    )
    const merged = dataList.flat().filter(Boolean)
    const map = {}
    merged.forEach((item) => {
      const key = item.id != null ? `ID_${item.id}` : `PK_${item.publishId}_${item.studentId}`
      map[key] = item
    })
    scores.value = Object.values(map)
    return
  }
  scores.value = await listScoresApi({})
}

const onFilterChange = async () => {
  if (query.projectId && !projectOptions.value.some((item) => item.value === query.projectId)) {
    query.projectId = null
  }
  if (!query.projectId) {
    query.className = ''
  } else if (query.className && !classOptions.value.some((item) => item.value === query.className)) {
    query.className = ''
  }
  await loadScores()
}

const onFormPublishChange = () => {
  if (!form.publishId) {
    form.studentId = null
    return
  }
  if (!publishStudentOptions.value.some((item) => item.value === form.studentId)) {
    form.studentId = publishStudentOptions.value[0]?.value || null
  }
}

const openCreate = (row = null) => {
  if (!query.projectId || !selectedPublishes.value.length) {
    ElMessage.warning('请先选择实训科目')
    return
  }
  if (row && row.recorded) {
    form.id = row.id
    form.publishId = row.publishId
    form.studentId = row.studentId
    form.usualScore = row.usualScore
    form.taskScore = row.taskScore
    form.reportScore = row.reportScore
    studentLocked.value = true
    dialogVisible.value = true
    return
  }

  const candidate = row || (missingRows.value.length ? missingRows.value[0] : null)
  form.id = null
  form.publishId = candidate ? candidate.publishId : (dialogPublishOptions.value[0]?.value || null)
  studentLocked.value = !!row
  onFormPublishChange()
  form.studentId = candidate ? candidate.studentId : (publishStudentOptions.value[0]?.value || null)
  form.usualScore = null
  form.taskScore = null
  form.reportScore = null
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.publishId || !form.studentId) {
    ElMessage.warning('请选择实训科目和学生')
    return
  }
  if (form.usualScore == null || form.taskScore == null || form.reportScore == null) {
    ElMessage.warning('请完整填写三项分数')
    return
  }
  saving.value = true
  try {
    const payload = {
      publishId: form.publishId,
      studentId: form.studentId,
      usualScore: form.usualScore,
      taskScore: form.taskScore,
      reportScore: form.reportScore
    }
    if (form.id) {
      await updateScoreApi(form.id, payload)
    } else {
      await createScoreApi(payload)
    }
    ElMessage.success('成绩保存成功')
    dialogVisible.value = false
    await loadScores()
  } finally {
    saving.value = false
  }
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该成绩记录吗？', '提示', { type: 'warning' })
  await deleteScoreApi(id)
  ElMessage.success('成绩删除成功')
  await loadScores()
}

const openImport = () => {
  importPublishId.value = selectedPublishes.value.length === 1 ? selectedPublishes.value[0].id : null
  importFileList.value = []
  importFile.value = null
  importResult.value = null
  importVisible.value = true
}

const onImportFileChange = (uploadFile) => {
  importFile.value = uploadFile?.raw || null
}

const onImportFileRemove = () => {
  importFile.value = null
}

const submitImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择导入文件')
    return
  }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', importFile.value)
    const result = await importScoresApi(formData, importPublishId.value || null)
    importResult.value = result
    if ((result?.failedRows || 0) > 0) {
      ElMessage.warning(`导入完成：成功 ${result.successRows} 行，失败 ${result.failedRows} 行`)
    } else {
      ElMessage.success(`导入成功，共 ${result.successRows} 行`)
    }
    await loadScores()
  } finally {
    importing.value = false
  }
}

const csvCell = (value) => {
  if (value == null) return '""'
  return `"${String(value).replace(/"/g, '""')}"`
}

const exportScores = () => {
  const rows = canManageScore.value
    ? manageRows.value
    : studentScores.value.map((item) => ({
      projectName: item.projectName || projectName(publishById.value[item.publishId]?.projectId),
      termName: item.termName || publishById.value[item.publishId]?.termName || '-',
      className: item.className || '-',
      studentName: '-',
      usualScore: item.usualScore,
      taskScore: item.taskScore,
      reportScore: item.reportScore,
      finalScore: item.finalScore,
      recorded: true
    }))

  if (!rows.length) {
    ElMessage.warning('当前没有可导出的数据')
    return
  }

  const header = ['科目', '学期', '班级', '学生姓名', '录入状态', '过程得分', '团队协作得分', '答辩得分', '综合总评']
  const lines = [header.map(csvCell).join(',')]
  rows.forEach((row) => {
    const rowPublish = row.publishId != null ? publishById.value[row.publishId] : null
    lines.push([
      row.projectName || projectName(rowPublish?.projectId) || '-',
      row.termName || rowPublish?.termName || '-',
      row.className || rowPublish?.className || '-',
      row.studentName || '-',
      row.recorded === false ? '未录入' : '已录入',
      row.usualScore ?? '',
      row.taskScore ?? '',
      row.reportScore ?? '',
      row.finalScore ?? ''
    ].map(csvCell).join(','))
  })
  const content = `\uFEFF${lines.join('\n')}`
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  const exportName = query.projectId ? `${projectName(query.projectId)}_成绩单.csv` : '成绩单.csv'
  a.download = exportName
  a.click()
  window.URL.revokeObjectURL(url)
}

watch(
  () => form.publishId,
  () => {
    onFormPublishChange()
  }
)

onMounted(async () => {
  await Promise.all([loadFormula(), loadDicts()])
  await onFilterChange()
})
</script>

<style scoped>
.import-errors {
  margin-top: 10px;
  max-height: 220px;
  overflow: auto;
  padding: 10px;
  border: 1px solid #d9e6f1;
  border-radius: 10px;
  background: #f8fbff;
}

.import-errors-title {
  font-weight: 700;
  color: #21425d;
  margin-bottom: 8px;
}

.import-errors ul {
  margin: 0;
  padding-left: 18px;
  color: #4d6478;
}
</style>
