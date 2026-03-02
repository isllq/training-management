<template>
  <div class="system-page">
    <div class="module-head page-card">
      <div class="module-head-title">系统监控</div>
      <div class="module-head-desc">查看运行指标与登录日志，定位系统异常与访问风险。</div>
    </div>

    <div class="page-card">
      <h3>运行状态</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Java版本">{{ runtime.javaVersion }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ runtime.osName }}</el-descriptions-item>
        <el-descriptions-item label="CPU核数">{{ runtime.availableProcessors }}</el-descriptions-item>
        <el-descriptions-item label="运行时长(ms)">{{ runtime.uptimeMs }}</el-descriptions-item>
        <el-descriptions-item label="最大内存(MB)">{{ runtime.maxMemoryMB }}</el-descriptions-item>
        <el-descriptions-item label="总内存(MB)">{{ runtime.totalMemoryMB }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="page-card">
      <div class="toolbar">
        <el-input v-model="query.username" placeholder="用户名" clearable />
        <el-select v-model="query.status" placeholder="状态" clearable>
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-button @click="loadLogs">查询</el-button>
      </div>
      <el-table :data="logs" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="IP地址" width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="说明" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listLoginLogsApi, runtimeStatusApi } from '../api/system'

const runtime = reactive({})
const logs = ref([])
const query = reactive({
  username: '',
  status: undefined
})

const loadRuntime = async () => {
  const data = await runtimeStatusApi()
  Object.assign(runtime, data)
}

const loadLogs = async () => {
  const params = {}
  if (query.username) params.username = query.username
  if (query.status !== undefined) params.status = query.status
  logs.value = await listLoginLogsApi(params)
}

onMounted(async () => {
  await loadRuntime()
  await loadLogs()
})
</script>

<style scoped>
.system-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

h3 {
  margin-top: 0;
}
</style>
