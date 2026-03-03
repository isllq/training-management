<template>
  <div class="base-page">
    <div class="module-head page-card">
      <div class="module-head-title">基础数据</div>
      <div class="module-head-desc">维护学院、专业、班级基础档案，保障教学业务主数据一致性。</div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="学院" name="college">
        <div class="page-card">
          <div class="toolbar">
            <el-button type="primary" @click="openCollegeCreate">新增学院</el-button>
            <el-button @click="loadColleges">刷新</el-button>
          </div>
          <el-table :data="pagedColleges" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="collegeCode" label="学院编码" width="140" />
            <el-table-column prop="collegeName" label="学院名称" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openCollegeEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeCollege(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="pages.college"
              :page-size="pageSize"
              :total="colleges.length"
              layout="prev, pager, next"
              background
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="专业" name="major">
        <div class="page-card">
          <div class="toolbar">
            <el-select
              v-model="query.collegeId"
              placeholder="按学院筛选专业（可选）"
              clearable
              filterable
              @change="onCollegeFilterChange"
            >
              <el-option v-for="item in collegeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" @click="openMajorCreate">新增专业</el-button>
            <el-button @click="loadMajors">刷新</el-button>
          </div>
          <el-table :data="pagedMajors" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="collegeName" label="所属学院" width="180" />
            <el-table-column prop="majorCode" label="专业编码" width="140" />
            <el-table-column prop="majorName" label="专业名称" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openMajorEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeMajor(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="pages.major"
              :page-size="pageSize"
              :total="majors.length"
              layout="prev, pager, next"
              background
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="班级" name="class">
        <div class="page-card">
          <div class="toolbar">
            <el-select
              v-model="query.majorId"
              placeholder="按专业筛选班级（可选）"
              clearable
              filterable
              @change="onMajorFilterChange"
            >
              <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button type="primary" @click="openClassCreate">新增班级</el-button>
            <el-button @click="loadClasses">刷新</el-button>
          </div>
          <el-table :data="pagedClasses" border stripe>
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="collegeName" label="所属学院" width="180" />
            <el-table-column prop="majorName" label="所属专业" width="180" />
            <el-table-column prop="classCode" label="班级编码" width="140" />
            <el-table-column prop="className" label="班级名称" />
            <el-table-column prop="gradeYear" label="年级" width="90" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="openClassEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="removeClass(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager-wrap">
            <el-pagination
              v-model:current-page="pages.class"
              :page-size="pageSize"
              :total="classes.length"
              layout="prev, pager, next"
              background
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" append-to-body>
      <el-form :model="form" label-width="90px" class="dialog-form">
        <template v-if="dialogType === 'college'">
          <el-form-item label="学院编码"><el-input v-model="form.collegeCode" :disabled="!!form.id" /></el-form-item>
          <el-form-item label="学院名称"><el-input v-model="form.collegeName" /></el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <template v-if="dialogType === 'major'">
          <el-form-item label="所属学院">
            <el-select v-model="form.collegeId" filterable placeholder="请选择学院">
              <el-option v-for="item in collegeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="专业编码"><el-input v-model="form.majorCode" :disabled="!!form.id" /></el-form-item>
          <el-form-item label="专业名称"><el-input v-model="form.majorName" /></el-form-item>
        </template>
        <template v-if="dialogType === 'class'">
          <el-form-item label="所属专业">
            <el-select v-model="form.majorId" filterable placeholder="请选择专业">
              <el-option v-for="item in majorOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="班级编码"><el-input v-model="form.classCode" :disabled="!!form.id" /></el-form-item>
          <el-form-item label="班级名称"><el-input v-model="form.className" /></el-form-item>
          <el-form-item label="年级"><el-input-number v-model="form.gradeYear" :min="2000" :max="2100" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createClassApi,
  createCollegeApi,
  createMajorApi,
  deleteClassApi,
  deleteCollegeApi,
  deleteMajorApi,
  listClassesApi,
  listCollegesApi,
  listMajorsApi,
  updateClassApi,
  updateCollegeApi,
  updateMajorApi
} from '../api/base'

const pageSize = 8
const activeTab = ref('college')
const query = reactive({ collegeId: null, majorId: null })
const pages = reactive({ college: 1, major: 1, class: 1 })
const colleges = ref([])
const majors = ref([])
const classes = ref([])

const dialogVisible = ref(false)
const dialogType = ref('college')
const dialogTitle = ref('新增')
const form = reactive({})

const collegeOptions = computed(() =>
  colleges.value.map((item) => ({ value: item.id, label: `${item.collegeName}（${item.collegeCode}）` }))
)
const majorOptions = computed(() =>
  majors.value.map((item) => ({ value: item.id, label: `${item.majorName}（${item.majorCode}）` }))
)

const pageSlice = (list, page) => {
  const start = (page - 1) * pageSize
  return list.slice(start, start + pageSize)
}
const clampPage = (key, total) => {
  const max = Math.max(1, Math.ceil(total / pageSize))
  if (pages[key] > max) {
    pages[key] = max
  }
}

const pagedColleges = computed(() => pageSlice(colleges.value, pages.college))
const pagedMajors = computed(() => pageSlice(majors.value, pages.major))
const pagedClasses = computed(() => pageSlice(classes.value, pages.class))

const loadColleges = async () => {
  colleges.value = await listCollegesApi()
  clampPage('college', colleges.value.length)
}
const loadMajors = async () => {
  majors.value = await listMajorsApi(query.collegeId ? { collegeId: query.collegeId } : {})
  clampPage('major', majors.value.length)
  if (query.majorId && !majors.value.some((item) => item.id === query.majorId)) {
    query.majorId = null
    await loadClasses()
  }
}
const loadClasses = async () => {
  classes.value = await listClassesApi(query.majorId ? { majorId: query.majorId } : {})
  clampPage('class', classes.value.length)
}

const onCollegeFilterChange = async () => {
  pages.major = 1
  await loadMajors()
}

const onMajorFilterChange = async () => {
  pages.class = 1
  await loadClasses()
}

const openCollegeCreate = () => {
  dialogType.value = 'college'
  dialogTitle.value = '新增学院'
  Object.assign(form, { id: null, collegeCode: '', collegeName: '', status: 1 })
  dialogVisible.value = true
}
const openCollegeEdit = (row) => {
  dialogType.value = 'college'
  dialogTitle.value = '编辑学院'
  Object.assign(form, row)
  dialogVisible.value = true
}
const removeCollege = async (id) => {
  await ElMessageBox.confirm('确认删除该学院吗？', '提示', { type: 'warning' })
  await deleteCollegeApi(id)
  ElMessage.success('删除成功')
  await loadColleges()
  await loadMajors()
  await loadClasses()
}

const openMajorCreate = () => {
  dialogType.value = 'major'
  dialogTitle.value = '新增专业'
  Object.assign(form, {
    id: null,
    collegeId: query.collegeId || (collegeOptions.value[0] ? collegeOptions.value[0].value : null),
    majorCode: '',
    majorName: '',
    status: 1
  })
  dialogVisible.value = true
}
const openMajorEdit = (row) => {
  dialogType.value = 'major'
  dialogTitle.value = '编辑专业'
  Object.assign(form, row)
  dialogVisible.value = true
}
const removeMajor = async (id) => {
  await ElMessageBox.confirm('确认删除该专业吗？', '提示', { type: 'warning' })
  await deleteMajorApi(id)
  ElMessage.success('删除成功')
  await loadMajors()
  await loadClasses()
}

const openClassCreate = () => {
  dialogType.value = 'class'
  dialogTitle.value = '新增班级'
  Object.assign(form, {
    id: null,
    majorId: query.majorId || (majorOptions.value[0] ? majorOptions.value[0].value : null),
    classCode: '',
    className: '',
    gradeYear: 2022,
    status: 1
  })
  dialogVisible.value = true
}
const openClassEdit = (row) => {
  dialogType.value = 'class'
  dialogTitle.value = '编辑班级'
  Object.assign(form, row)
  dialogVisible.value = true
}
const removeClass = async (id) => {
  await ElMessageBox.confirm('确认删除该班级吗？', '提示', { type: 'warning' })
  await deleteClassApi(id)
  ElMessage.success('删除成功')
  await loadClasses()
}

const submit = async () => {
  if (dialogType.value === 'college') {
    if (form.id) await updateCollegeApi(form.id, form)
    else await createCollegeApi(form)
    await loadColleges()
    await loadMajors()
  }
  if (dialogType.value === 'major') {
    if (!form.collegeId) {
      ElMessage.warning('请选择所属学院')
      return
    }
    if (form.id) await updateMajorApi(form.id, form)
    else await createMajorApi(form)
    await loadMajors()
    await loadClasses()
  }
  if (dialogType.value === 'class') {
    if (!form.majorId) {
      ElMessage.warning('请选择所属专业')
      return
    }
    if (form.id) await updateClassApi(form.id, form)
    else await createClassApi(form)
    await loadClasses()
  }
  dialogVisible.value = false
  ElMessage.success('保存成功')
}

onMounted(async () => {
  await loadColleges()
  await loadMajors()
  await loadClasses()
})
</script>

<style scoped>
.base-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pager-wrap {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
