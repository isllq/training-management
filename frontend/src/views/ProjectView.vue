<template>
  <div class="project-page">
    <div class="module-head page-card">
      <div class="module-head-title">实训项目与开设管理</div>
      <div class="module-head-desc">统一维护项目库、学期开设计划与指导教师安排，支撑实训全过程组织。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-input v-model="projectQuery.keyword" placeholder="按项目名称或编号检索" clearable @keyup.enter="loadProjects" />
        <el-button v-if="canEdit" type="primary" @click="openProjectCreate">新增项目模板</el-button>
        <el-button @click="loadProjects">检索</el-button>
      </div>

      <el-table :data="projects" border stripe>
        <el-table-column prop="projectCode" label="项目编号" width="140" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column prop="totalHours" label="建议学时" width="100" />
        <el-table-column prop="difficulty" label="难度等级" width="110" />
        <el-table-column prop="status" label="启用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="canEdit" label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openProjectEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProject(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="publishQuery.termName" placeholder="按学期筛选" clearable filterable>
          <el-option v-for="item in termOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-select v-model="publishQuery.className" placeholder="按班级筛选" clearable filterable>
          <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button v-if="canEdit" type="primary" @click="openPublishCreate">新增开设计划</el-button>
      </div>

      <el-table :data="filteredPublishes" border stripe>
        <el-table-column prop="termName" label="开设学期" width="140" />
        <el-table-column label="实训项目" min-width="180">
          <template #default="{ row }">{{ projectName(row.projectId) }}</template>
        </el-table-column>
        <el-table-column prop="className" label="面向班级" width="150" />
        <el-table-column label="指导教师" width="140">
          <template #default="{ row }">{{ row.teacherName || teacherName(row.teacherId) }}</template>
        </el-table-column>
        <el-table-column prop="groupCount" label="计划小组数" width="110" />
        <el-table-column prop="groupSizeLimit" label="每组人数上限" width="120">
          <template #default="{ row }">{{ row.groupSizeLimit > 0 ? row.groupSizeLimit : '不限' }}</template>
        </el-table-column>
        <el-table-column prop="assessmentStandard" label="考核标准" min-width="220" show-overflow-tooltip />
        <el-table-column label="成绩权重" width="170">
          <template #default="{ row }">{{ weightText(row) }}</template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column v-if="canEdit" label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openPublishEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deletePublish(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-if="canEdit" v-model="projectDialog" :title="projectForm.id ? '编辑项目模板' : '新增项目模板'" width="560px" append-to-body>
      <el-form :model="projectForm" label-width="95px" class="dialog-form">
        <el-form-item label="项目编号"><el-input v-model="projectForm.projectCode" :disabled="!!projectForm.id" /></el-form-item>
        <el-form-item label="项目名称"><el-input v-model="projectForm.projectName" /></el-form-item>
        <el-form-item label="项目简介"><el-input v-model="projectForm.description" type="textarea" /></el-form-item>
        <el-form-item label="建议学时"><el-input-number v-model="projectForm.totalHours" :min="8" :max="200" /></el-form-item>
        <el-form-item label="难度等级">
          <el-select v-model="projectForm.difficulty">
            <el-option label="低" value="EASY" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-radio-group v-model="projectForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="projectDialog = false">取消</el-button>
        <el-button type="primary" @click="submitProject">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-if="canEdit" v-model="publishDialog" :title="publishForm.id ? '编辑开设计划' : '新增开设计划'" width="620px" append-to-body>
      <el-form :model="publishForm" label-width="95px" class="dialog-form">
        <el-form-item label="所属项目">
          <el-select v-model="publishForm.projectId" placeholder="请选择项目" filterable>
            <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开设学期">
          <el-select
            v-model="publishForm.termName"
            filterable
            placeholder="根据班级自动推荐，可手动调整"
          >
            <el-option v-for="item in termOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <div class="form-tip">系统会按班级入学年份和当前月份推荐学期：下半年为第1学期，上半年为第2学期。</div>
        </el-form-item>
        <el-form-item label="面向班级">
          <el-select v-model="publishForm.classNames" multiple filterable collapse-tags placeholder="请选择班级（可多选）" @change="onPublishClassChange">
            <el-option v-for="item in classOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="指导教师">
          <el-select v-model="publishForm.teacherId" placeholder="请选择教师" filterable :disabled="isTeacherRole">
            <el-option v-for="item in teacherOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划小组数">
          <el-input-number v-model="publishForm.groupCount" :min="0" :max="50" />
        </el-form-item>
        <el-form-item label="每组人数上限">
          <el-input-number v-model="publishForm.groupSizeLimit" :min="0" :max="20" />
          <div class="form-tip">填 0 表示不限制人数</div>
        </el-form-item>
        <el-form-item label="评分权重">
          <div class="weight-row">
            <el-input-number v-model="publishForm.processWeightPercent" :min="0" :max="100" :precision="2" />
            <span class="weight-label">过程(%)</span>
            <el-input-number v-model="publishForm.teamWeightPercent" :min="0" :max="100" :precision="2" />
            <span class="weight-label">协作(%)</span>
            <el-input-number v-model="publishForm.finalWeightPercent" :min="0" :max="100" :precision="2" />
            <span class="weight-label">答辩(%)</span>
          </div>
          <div class="form-tip">用于成绩算法计算，三项之和必须等于100%</div>
        </el-form-item>
        <el-form-item label="考核标准"><el-input v-model="publishForm.assessmentStandard" type="textarea" /></el-form-item>
        <div class="form-tip">考核标准是给师生看的文字说明；评分权重是系统实际计算总评使用的参数。</div>
        <el-form-item label="开始日期">
          <el-date-picker v-model="publishForm.startDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="publishForm.endDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-radio-group v-model="publishForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPublish">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createProjectApi,
  createPublishApi,
  deleteProjectApi,
  deletePublishApi,
  listProjectsApi,
  listPublishesApi,
  updateProjectApi,
  updatePublishApi
} from '../api/projects'
import { listUserOptionsApi } from '../api/auth'
import { listClassesApi } from '../api/base'
import { ROLE, canManageTeaching, hasAnyRole } from '../utils/auth'
import { toNameMap, fallbackName } from '../utils/dicts'

const canEdit = computed(() => canManageTeaching())
const isTeacherRole = computed(() => hasAnyRole([ROLE.TEACHER]) && !hasAnyRole([ROLE.ADMIN]))

const projectQuery = reactive({ keyword: '' })
const publishQuery = reactive({ termName: '', className: '' })
const projects = ref([])
const publishes = ref([])
const teachers = ref([])
const classes = ref([])

const projectDialog = ref(false)
const publishDialog = ref(false)

const projectForm = reactive({
  id: null,
  projectCode: '',
  projectName: '',
  description: '',
  totalHours: 64,
  difficulty: 'MEDIUM',
  status: 1
})

const publishForm = reactive({
  id: null,
  projectId: null,
  termName: '',
  className: '',
  classNames: [],
  teacherId: null,
  groupCount: 0,
  groupSizeLimit: 0,
  processWeightPercent: 40,
  teamWeightPercent: 30,
  finalWeightPercent: 30,
  assessmentStandard: '',
  startDate: '',
  endDate: '',
  status: 1
})

const projectOptions = computed(() =>
  projects.value.map((item) => ({ value: item.id, label: `${item.projectName}（${item.projectCode}）` }))
)

const teacherOptions = computed(() =>
  teachers.value.map((item) => ({ value: item.id, label: item.name || `教师${item.id}` }))
)

const classOptions = computed(() =>
  classes.value.map((item) => ({ value: item.className, label: `${item.className}${item.gradeYear ? `（${item.gradeYear}级）` : ''}` }))
)

const termOptions = computed(() => {
  const set = new Set()
  publishes.value.forEach((item) => {
    if (item.termName) {
      set.add(item.termName)
    }
  })
  buildTermCandidates(4).forEach((item) => set.add(item))
  return Array.from(set)
    .sort((a, b) => String(b).localeCompare(String(a)))
    .map((value) => ({ value, label: value }))
})

const filteredPublishes = computed(() =>
  publishes.value.filter((item) => {
    if (publishQuery.termName && item.termName !== publishQuery.termName) return false
    if (publishQuery.className && !String(item.className || '').includes(publishQuery.className)) return false
    return true
  })
)

const projectNameMap = computed(() => toNameMap(projects.value, 'id', 'projectName'))
const teacherNameMap = computed(() => toNameMap(teachers.value, 'id', 'name'))

const projectName = (id) => fallbackName(id, projectNameMap.value, '项目')
const teacherName = (id) => fallbackName(id, teacherNameMap.value, '教师')
const weightText = (row) => {
  const p = toPercent(row.processWeight)
  const t = toPercent(row.teamWeight)
  const f = toPercent(row.finalWeight)
  if (p == null || t == null || f == null) {
    return '未配置（默认40/30/30）'
  }
  return `过程${p}% / 协作${t}% / 答辩${f}%`
}

const toPercent = (value) => {
  if (value == null || value === '') return null
  const n = Number(value)
  if (Number.isNaN(n)) return null
  return Number((n * 100).toFixed(2))
}

const toRatio = (percent) => {
  if (percent == null || percent === '') return null
  const n = Number(percent)
  if (Number.isNaN(n)) return null
  return Number((n / 100).toFixed(4))
}

const todayDate = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const defaultTerm = () => {
  const now = new Date()
  const year = now.getMonth() + 1 >= 8 ? now.getFullYear() : now.getFullYear() - 1
  const next = year + 1
  const termNo = now.getMonth() + 1 >= 2 && now.getMonth() + 1 <= 7 ? 2 : 1
  return `${year}-${next}-${termNo}`
}

const parseClassEntryYear = (className) => {
  const text = String(className || '').trim()
  const match = text.match(/^(\d{2})\d{4}$/)
  if (!match) return null
  const year = Number(match[1])
  return Number.isNaN(year) ? null : (2000 + year)
}

const parseTermStartYear = (termName) => {
  const text = String(termName || '').trim()
  const match = text.match(/^(\d{4})-\d{4}-[12]$/)
  if (!match) return null
  const year = Number(match[1])
  return Number.isNaN(year) ? null : year
}

const recommendTermByClasses = (classNames = []) => {
  if (!classNames.length) return defaultTerm()
  const now = new Date()
  const month = now.getMonth() + 1
  const currentStartYear = month >= 8 ? now.getFullYear() : now.getFullYear() - 1
  const currentTermNo = month >= 2 && month <= 7 ? 2 : 1
  let maxEntryYear = 0
  classNames.forEach((name) => {
    const year = parseClassEntryYear(name)
    if (year && year > maxEntryYear) {
      maxEntryYear = year
    }
  })
  const startYear = maxEntryYear > 0 ? Math.max(currentStartYear, maxEntryYear) : currentStartYear
  return `${startYear}-${startYear + 1}-${currentTermNo}`
}

const buildTermCandidates = (span = 4) => {
  const termList = []
  const now = new Date()
  const base = now.getMonth() + 1 >= 8 ? now.getFullYear() : now.getFullYear() - 1
  for (let i = -span; i <= span; i++) {
    const start = base + i
    const end = start + 1
    termList.push(`${start}-${end}-1`)
    termList.push(`${start}-${end}-2`)
  }
  return termList
}

const parseClassNames = (className) =>
  String(className || '')
    .split(/[,，;；/\s、]+/)
    .map((item) => item.trim())
    .filter(Boolean)

const collectClassesFromPublishes = () => {
  const map = {}
  publishes.value.forEach((item) => {
    parseClassNames(item.className).forEach((name) => {
      if (!map[name]) {
        map[name] = { className: name, gradeYear: null }
      }
    })
  })
  classes.value = Object.values(map)
}

const loadProjects = async () => {
  projects.value = await listProjectsApi(projectQuery)
}

const loadPublishes = async () => {
  publishes.value = await listPublishesApi({})
}

const loadTeachers = async () => {
  teachers.value = await listUserOptionsApi({ userType: 'TEACHER' })
}

const loadClasses = async () => {
  try {
    classes.value = await listClassesApi({})
    if (!classes.value.length) {
      collectClassesFromPublishes()
    }
  } catch (error) {
    collectClassesFromPublishes()
  }
}

const onPublishClassChange = (classNames) => {
  if (!classNames || !classNames.length) return
  if (!publishForm.id || !publishForm.termName) {
    publishForm.termName = recommendTermByClasses(classNames)
  }
}

const openProjectCreate = () => {
  Object.assign(projectForm, {
    id: null,
    projectCode: '',
    projectName: '',
    description: '',
    totalHours: 64,
    difficulty: 'MEDIUM',
    status: 1
  })
  projectDialog.value = true
}

const openProjectEdit = (row) => {
  Object.assign(projectForm, row)
  projectDialog.value = true
}

const submitProject = async () => {
  if (!projectForm.projectCode || !projectForm.projectName) {
    ElMessage.warning('请填写项目编号和项目名称')
    return
  }
  if (projectForm.id) {
    await updateProjectApi(projectForm.id, projectForm)
  } else {
    await createProjectApi(projectForm)
  }
  ElMessage.success('项目保存成功')
  projectDialog.value = false
  await loadProjects()
}

const deleteProject = async (id) => {
  await ElMessageBox.confirm('确认删除该项目模板吗？', '提示', { type: 'warning' })
  await deleteProjectApi(id)
  ElMessage.success('项目删除成功')
  await loadProjects()
}

const openPublishCreate = () => {
  const classNames = classOptions.value.length ? [classOptions.value[0].value] : []
  Object.assign(publishForm, {
    id: null,
    projectId: projectOptions.value.length ? projectOptions.value[0].value : null,
    termName: recommendTermByClasses(classNames),
    className: '',
    classNames,
    teacherId: teacherOptions.value.length ? teacherOptions.value[0].value : null,
    groupCount: 0,
    groupSizeLimit: 0,
    processWeightPercent: 40,
    teamWeightPercent: 30,
    finalWeightPercent: 30,
    assessmentStandard: '',
    startDate: todayDate(),
    endDate: '',
    status: 1
  })
  publishDialog.value = true
}

const openPublishEdit = (row) => {
  Object.assign(publishForm, {
    id: row.id,
    projectId: row.projectId,
    termName: row.termName || defaultTerm(),
    className: row.className || '',
    classNames: parseClassNames(row.className),
    teacherId: row.teacherId,
    groupCount: row.groupCount ?? 0,
    groupSizeLimit: row.groupSizeLimit ?? 0,
    processWeightPercent: toPercent(row.processWeight) ?? 40,
    teamWeightPercent: toPercent(row.teamWeight) ?? 30,
    finalWeightPercent: toPercent(row.finalWeight) ?? 30,
    assessmentStandard: row.assessmentStandard || '',
    startDate: row.startDate || todayDate(),
    endDate: row.endDate || '',
    status: row.status ?? 1
  })
  publishDialog.value = true
}

const submitPublish = async () => {
  if (!publishForm.projectId || !publishForm.teacherId) {
    ElMessage.warning('请完整选择项目、班级和指导教师')
    return
  }
  if (!publishForm.classNames || !publishForm.classNames.length) {
    ElMessage.warning('请至少选择一个班级')
    return
  }
  if (!publishForm.termName) {
    publishForm.termName = recommendTermByClasses(publishForm.classNames)
  }
  const termStartYear = parseTermStartYear(publishForm.termName)
  if (!termStartYear) {
    ElMessage.warning('学期格式不正确，请使用“YYYY-YYYY-学期号”')
    return
  }
  const invalidClass = publishForm.classNames.find((name) => {
    const entryYear = parseClassEntryYear(name)
    return entryYear && termStartYear < entryYear
  })
  if (invalidClass) {
    ElMessage.warning(`班级 ${invalidClass} 的学期不能早于入学年份`)
    return
  }
  const weightTotal = Number(publishForm.processWeightPercent || 0) +
    Number(publishForm.teamWeightPercent || 0) +
    Number(publishForm.finalWeightPercent || 0)
  if (Math.abs(weightTotal - 100) > 0.01) {
    ElMessage.warning('评分权重三项之和必须等于100%')
    return
  }
  const payload = {
    ...publishForm,
    className: publishForm.classNames.join(','),
    processWeight: toRatio(publishForm.processWeightPercent),
    teamWeight: toRatio(publishForm.teamWeightPercent),
    finalWeight: toRatio(publishForm.finalWeightPercent)
  }
  if (!payload.assessmentStandard) {
    payload.assessmentStandard = `过程${payload.processWeight * 100}%+协作${payload.teamWeight * 100}%+答辩${payload.finalWeight * 100}%`
  }
  if (publishForm.id) {
    await updatePublishApi(publishForm.id, payload)
  } else {
    await createPublishApi(payload)
  }
  ElMessage.success('开设计划保存成功')
  publishDialog.value = false
  await loadPublishes()
}

const deletePublish = async (id) => {
  await ElMessageBox.confirm('确认删除该开设计划吗？', '提示', { type: 'warning' })
  await deletePublishApi(id)
  ElMessage.success('开设计划删除成功')
  await loadPublishes()
}

onMounted(async () => {
  await loadProjects()
  await loadTeachers()
  await loadPublishes()
  await loadClasses()
})

watch(
  () => publishForm.classNames.slice(),
  (classNames) => {
    if (!classNames.length) return
    if (!publishForm.id || !publishForm.termName) {
      publishForm.termName = recommendTermByClasses(classNames)
    }
  }
)
</script>

<style scoped>
.project-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #62798d;
}

.weight-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.weight-label {
  color: #4c657a;
  font-size: 12px;
  min-width: 56px;
}
</style>
