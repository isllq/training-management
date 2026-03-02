<template>
  <div class="module-page">
    <div class="module-head page-card">
      <div class="module-head-title">实训小组管理</div>
      <div class="module-head-desc">教师按开设规则统一建组，学生按项目选择小组加入。</div>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-select v-model="query.publishId" placeholder="按项目班级筛选" clearable filterable>
          <el-option v-for="item in publishOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-button v-if="canManageTeam" type="primary" @click="openCreate">新增小组</el-button>
        <el-button v-if="canManageTeam" @click="generateByRule">按规则生成小组</el-button>
      </div>

      <el-alert
        v-if="selectedPublish"
        :title="`当前规则：计划小组 ${selectedPublish.groupCount || 0} 个，每组上限 ${selectedPublish.groupSizeLimit > 0 ? selectedPublish.groupSizeLimit : '不限'} 人`"
        type="info"
        :closable="false"
        show-icon
        class="team-tip"
      />
      <el-alert
        v-if="isStudentRole && query.publishId"
        :title="studentTeamTip"
        type="success"
        :closable="false"
        show-icon
        class="team-tip"
      />

      <el-table :data="teams" border stripe>
        <el-table-column label="小组名称" min-width="180">
          <template #default="{ row }">{{ displayTeamName(row.teamName, row.id) }}</template>
        </el-table-column>
        <el-table-column label="实训项目" min-width="180">
          <template #default="{ row }">{{ projectNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="所属学期" width="130">
          <template #default="{ row }">{{ termNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="适用班级" width="120">
          <template #default="{ row }">{{ classNameByPublish(row.publishId) }}</template>
        </el-table-column>
        <el-table-column label="人数" width="130">
          <template #default="{ row }">
            {{ row.memberCount || 0 }}/{{ row.groupSizeLimit > 0 ? row.groupSizeLimit : '不限' }}
          </template>
        </el-table-column>
        <el-table-column label="组长" width="120">
          <template #default="{ row }">{{ studentName(row.leaderStudentId) }}</template>
        </el-table-column>
        <el-table-column :width="canManageTeam ? 270 : 220" label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="openMembers(row)">成员</el-button>
            <template v-if="canManageTeam">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
            </template>
            <template v-else>
              <el-button v-if="canStudentJoin(row)" size="small" type="primary" @click="joinTeam(row)">加入</el-button>
              <el-button v-if="canStudentQuit(row)" size="small" type="warning" @click="quitTeam(row)">退出</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog
      v-if="canManageTeam"
      v-model="dialogVisible"
      :title="form.id ? '编辑小组' : '新增小组'"
      width="540px"
      append-to-body
    >
      <el-form :model="form" label-width="95px" class="dialog-form">
        <el-form-item label="所属项目班级">
          <el-select v-model="form.publishId" filterable placeholder="请选择项目/班级/学期">
            <el-option v-for="item in publishOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="小组名称"><el-input v-model="form.teamName" /></el-form-item>
        <el-form-item label="组长（选填）">
          <el-select v-model="form.leaderStudentId" clearable filterable placeholder="可不填，后续再设置">
            <el-option v-for="item in studentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="memberDrawer"
      :title="`小组成员（${currentTeamName || '-'}）`"
      size="70%"
      append-to-body
    >
      <template v-if="canManageTeam">
        <div class="toolbar">
          <el-select v-model="memberForm.studentId" placeholder="请选择学生" filterable>
            <el-option v-for="item in studentOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
          <el-input v-model="memberForm.roleInTeam" placeholder="团队角色，如后端开发/测试" />
          <el-button type="primary" @click="addMember">添加成员</el-button>
        </div>
      </template>
      <el-table :data="members" border stripe style="margin-top: 12px">
        <el-table-column label="成员姓名" min-width="120">
          <template #default="{ row }">{{ studentName(row.studentId) }}</template>
        </el-table-column>
        <el-table-column prop="roleInTeam" label="团队角色" min-width="160" />
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <el-button
              v-if="canTransferLeader && row.studentId !== currentLeaderId"
              link
              type="primary"
              @click="setLeader(row)"
            >
              设为组长
            </el-button>
            <el-button v-if="canManageTeam" link type="danger" @click="removeMember(row.id)">移除</el-button>
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
  addMemberApi,
  createTeamApi,
  deleteMemberApi,
  deleteTeamApi,
  generateTeamsApi,
  joinTeamApi,
  listMembersApi,
  listTeamsApi,
  myTeamApi,
  quitTeamApi,
  transferTeamLeaderApi,
  updateTeamApi
} from '../api/teams'
import { listProjectsApi, listPublishesApi } from '../api/projects'
import { listUserOptionsApi } from '../api/auth'
import { ROLE, canManageTeaching, hasAnyRole } from '../utils/auth'
import { toNameMap, fallbackName } from '../utils/dicts'

const canManageTeam = computed(() => canManageTeaching())
const isStudentRole = computed(() => hasAnyRole([ROLE.STUDENT]))

const query = reactive({ publishId: null })
const teams = ref([])
const projects = ref([])
const publishes = ref([])
const students = ref([])
const myTeam = ref({ inTeam: false, teamId: null, leader: false })

const dialogVisible = ref(false)
const form = reactive({
  id: null,
  publishId: null,
  teamName: '',
  leaderStudentId: null,
  status: 1
})

const memberDrawer = ref(false)
const currentTeamId = ref(null)
const currentTeamName = ref('')
const currentLeaderId = ref(null)
const members = ref([])
const memberForm = reactive({ studentId: null, roleInTeam: '成员' })

const publishOptions = computed(() =>
  publishes.value.map((item) => ({ value: item.id, label: publishOptionLabel(item) }))
)
const studentOptions = computed(() =>
  students.value.map((item) => ({ value: item.id, label: item.name || `学生${item.id}` }))
)

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
const projectNameMap = computed(() => toNameMap(projects.value, 'id', 'projectName'))
const studentNameMap = computed(() => toNameMap(students.value, 'id', 'name'))

const selectedPublish = computed(() => publishes.value.find((item) => item.id === query.publishId) || null)
const selectedPublishClass = computed(() => (selectedPublish.value ? selectedPublish.value.className : ''))

const publishName = (id) => fallbackName(id, publishNameMap.value, '开设')
const projectName = (id) => fallbackName(id, projectNameMap.value, '项目')
const termNameByPublish = (id) => publishDetailMap.value[id]?.termName || '-'
const classNameByPublish = (id) => publishDetailMap.value[id]?.className || '-'
const projectIdByPublish = (id) => publishDetailMap.value[id]?.projectId || null
const projectNameByPublish = (id) => {
  const pid = projectIdByPublish(id)
  return pid ? projectName(pid) : publishName(id)
}
const studentName = (id) => fallbackName(id, studentNameMap.value, '学生')

const publishOptionLabel = (item) => {
  const projectText = projectName(item.projectId)
  const classText = item.className || '未设班级'
  const termText = item.termName || '未设学期'
  return `${projectText}｜${classText}｜${termText}`
}

const displayTeamName = (teamName, teamId) => {
  const value = (teamName || '').trim()
  if (!value) return teamId ? `第${teamId}组` : '-'
  const match = value.match(/^([A-Za-z])组([\-－—_].*)?$/)
  if (!match) {
    return value
  }
  const letter = match[1].toUpperCase()
  const index = letter.charCodeAt(0) - 64
  if (index < 1 || index > 26) {
    return value
  }
  return `第${index}组${match[2] || ''}`
}

const studentTeamTip = computed(() => {
  if (!query.publishId) return '请选择项目班级后查看可加入小组。'
  if (!myTeam.value.inTeam) return '你当前未加入小组，可从列表中选择加入。'
  return '你已加入小组，可在当前小组点击“退出”。'
})

const canStudentJoin = (row) => {
  if (!isStudentRole.value || !query.publishId || myTeam.value.inTeam) return false
  if (row.publishId !== query.publishId) return false
  if (row.groupSizeLimit > 0 && (row.memberCount || 0) >= row.groupSizeLimit) return false
  return true
}

const canStudentQuit = (row) =>
  isStudentRole.value && myTeam.value.inTeam && myTeam.value.teamId === row.id

const canTransferLeader = computed(() =>
  canManageTeam.value || (isStudentRole.value && myTeam.value.inTeam && myTeam.value.leader && myTeam.value.teamId === currentTeamId.value)
)

const loadDicts = async () => {
  projects.value = await listProjectsApi({})
  publishes.value = await listPublishesApi({})
  if (!query.publishId && publishOptions.value.length) {
    query.publishId = publishOptions.value[0].value
  }
  await loadStudents()
}

const loadStudents = async () => {
  const params = { userType: 'STUDENT' }
  if (canManageTeam.value && selectedPublishClass.value) {
    params.className = selectedPublishClass.value
  }
  students.value = await listUserOptionsApi(params)
}

const loadTeams = async () => {
  teams.value = await listTeamsApi(query.publishId ? { publishId: query.publishId } : {})
}

const loadMyTeam = async () => {
  if (!isStudentRole.value || !query.publishId) {
    myTeam.value = { inTeam: false, teamId: null, leader: false }
    return
  }
  myTeam.value = await myTeamApi({ publishId: query.publishId })
}

const openCreate = () => {
  Object.assign(form, {
    id: null,
    publishId: query.publishId || (publishOptions.value.length ? publishOptions.value[0].value : null),
    teamName: '',
    leaderStudentId: null,
    status: 1
  })
  dialogVisible.value = true
}

const openEdit = (row) => {
  Object.assign(form, row, { teamName: displayTeamName(row.teamName, row.id) })
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.publishId || !form.teamName) {
    ElMessage.warning('请完整填写所属项目班级和小组名称')
    return
  }
  const payload = { ...form, status: 1 }
  if (form.id) {
    await updateTeamApi(form.id, payload)
  } else {
    await createTeamApi(payload)
  }
  dialogVisible.value = false
  ElMessage.success('小组保存成功')
  await loadTeams()
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该小组吗？', '提示', { type: 'warning' })
  await deleteTeamApi(id)
  ElMessage.success('小组删除成功')
  await loadTeams()
}

const generateByRule = async () => {
  if (!query.publishId) {
    ElMessage.warning('请先选择项目班级')
    return
  }
  const result = await generateTeamsApi(query.publishId)
  ElMessage.success(result.message || '小组生成完成')
  await loadTeams()
}

const joinTeam = async (row) => {
  await joinTeamApi(row.id)
  ElMessage.success('已加入小组')
  await loadTeams()
  await loadMyTeam()
}

const quitTeam = async (row) => {
  await ElMessageBox.confirm('确认退出当前小组吗？', '提示', { type: 'warning' })
  await quitTeamApi(row.id)
  ElMessage.success('已退出小组')
  await loadTeams()
  await loadMyTeam()
}

const openMembers = async (row) => {
  currentTeamId.value = row.id
  currentTeamName.value = displayTeamName(row.teamName, row.id)
  currentLeaderId.value = row.leaderStudentId || null
  memberDrawer.value = true
  members.value = await listMembersApi(row.id)
}

const addMember = async () => {
  if (!currentTeamId.value || !memberForm.studentId) {
    ElMessage.warning('请选择要加入的学生')
    return
  }
  await addMemberApi(currentTeamId.value, memberForm)
  ElMessage.success('成员添加成功')
  memberForm.studentId = null
  members.value = await listMembersApi(currentTeamId.value)
  await loadTeams()
}

const removeMember = async (id) => {
  await deleteMemberApi(id)
  ElMessage.success('成员移除成功')
  members.value = await listMembersApi(currentTeamId.value)
  await loadTeams()
}

const setLeader = async (row) => {
  if (!currentTeamId.value || !row?.studentId) {
    return
  }
  await transferTeamLeaderApi(currentTeamId.value, row.studentId)
  ElMessage.success('组长已转让')
  members.value = await listMembersApi(currentTeamId.value)
  await loadTeams()
  await loadMyTeam()
  currentLeaderId.value = row.studentId
}

watch(
  () => query.publishId,
  async () => {
    await loadStudents()
    await loadTeams()
    await loadMyTeam()
  }
)

onMounted(async () => {
  await loadDicts()
  await loadTeams()
  await loadMyTeam()
})
</script>

<style scoped>
.team-tip {
  margin-bottom: 12px;
}
</style>
