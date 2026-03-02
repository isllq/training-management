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
        <el-input v-model="publishQuery.className" placeholder="按班级名称检索开设计划" clearable @keyup.enter="loadPublishes" />
        <el-button v-if="canEdit" type="primary" @click="openPublishCreate">新增开设计划</el-button>
        <el-button @click="loadPublishes">检索</el-button>
      </div>

      <el-table :data="publishes" border stripe>
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

    <el-dialog v-if="canEdit" v-model="projectDialog" :title="projectForm.id ? '编辑项目模板' : '新增项目模板'" width="560px">
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

    <el-dialog v-if="canEdit" v-model="publishDialog" :title="publishForm.id ? '编辑开设计划' : '新增开设计划'" width="620px">
      <el-form :model="publishForm" label-width="95px" class="dialog-form">
        <el-form-item label="所属项目">
          <el-select v-model="publishForm.projectId" placeholder="请选择项目" filterable>
            <el-option v-for="item in projectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开设学期"><el-input v-model="publishForm.termName" placeholder="例：2025-2026-2" /></el-form-item>
        <el-form-item label="面向班级"><el-input v-model="publishForm.className" /></el-form-item>
        <el-form-item label="指导教师">
          <el-select v-model="publishForm.teacherId" placeholder="请选择教师" filterable>
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
        <el-form-item label="考核标准"><el-input v-model="publishForm.assessmentStandard" type="textarea" /></el-form-item>
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
import { computed, onMounted, reactive, ref } from 'vue'
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
import { canManageTeaching } from '../utils/auth'
import { toNameMap, fallbackName } from '../utils/dicts'

const canEdit = computed(() => canManageTeaching())

const projectQuery = reactive({ keyword: '' })
const publishQuery = reactive({ className: '' })
const projects = ref([])
const publishes = ref([])
const teachers = ref([])

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
  termName: '2025-2026-2',
  className: '',
  teacherId: null,
  groupCount: 0,
  groupSizeLimit: 0,
  assessmentStandard: '',
  startDate: '2026-03-01',
  endDate: '2026-06-15',
  status: 1
})

const projectOptions = computed(() =>
  projects.value.map((item) => ({ value: item.id, label: `${item.projectName}（${item.projectCode}）` }))
)

const teacherOptions = computed(() =>
  teachers.value.map((item) => ({ value: item.id, label: item.name || `教师${item.id}` }))
)

const projectNameMap = computed(() => toNameMap(projects.value, 'id', 'projectName'))
const teacherNameMap = computed(() => toNameMap(teachers.value, 'id', 'name'))

const projectName = (id) => fallbackName(id, projectNameMap.value, '项目')
const teacherName = (id) => fallbackName(id, teacherNameMap.value, '教师')

const loadProjects = async () => {
  projects.value = await listProjectsApi(projectQuery)
}

const loadPublishes = async () => {
  publishes.value = await listPublishesApi(publishQuery)
}

const loadTeachers = async () => {
  teachers.value = await listUserOptionsApi({ userType: 'TEACHER' })
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
  Object.assign(publishForm, {
    id: null,
    projectId: projectOptions.value.length ? projectOptions.value[0].value : null,
    termName: '2025-2026-2',
    className: '',
    teacherId: teacherOptions.value.length ? teacherOptions.value[0].value : null,
    groupCount: 0,
    groupSizeLimit: 0,
    assessmentStandard: '',
    startDate: '2026-03-01',
    endDate: '2026-06-15',
    status: 1
  })
  publishDialog.value = true
}

const openPublishEdit = (row) => {
  Object.assign(publishForm, { groupCount: 0, groupSizeLimit: 0 }, row)
  publishDialog.value = true
}

const submitPublish = async () => {
  if (!publishForm.projectId || !publishForm.className || !publishForm.teacherId) {
    ElMessage.warning('请完整选择项目、班级和指导教师')
    return
  }
  if (publishForm.id) {
    await updatePublishApi(publishForm.id, publishForm)
  } else {
    await createPublishApi(publishForm)
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
})
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
</style>
