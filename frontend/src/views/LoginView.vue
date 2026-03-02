<template>
  <div class="login-page">
    <div class="bg-grid"></div>
    <div class="bg-orb orb-a"></div>
    <div class="bg-orb orb-b"></div>
    <div class="bg-orb orb-c"></div>

    <div class="login-shell">
      <section class="brand-panel">
        <div class="brand-chip">2025-2026-2 学期</div>
        <h1>专业综合实训管理系统</h1>
        <p>公告、任务、小组、提交、成绩全流程在线协同。</p>
        <div class="brand-line"></div>
        <div class="brand-points">
          <span>过程留痕</span>
          <span>班级隔离</span>
          <span>附件归档</span>
          <span>进度可追踪</span>
        </div>
      </section>

      <section class="form-panel">
        <div class="form-head">
          <h2>账号登录</h2>
          <small>请输入账号密码进入系统</small>
        </div>

        <el-form :model="form" @submit.prevent>
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" placeholder="密码" type="password" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="full login-btn" :loading="loading" @click="login">登录系统</el-button>
        </el-form>

        <div class="quick">
          <div class="quick-title">演示账号</div>
          <div class="quick-buttons">
            <el-button size="small" @click="fill('admin')">管理员</el-button>
            <el-button size="small" @click="fill('teacher01')">教师</el-button>
            <el-button size="small" @click="fill('student01')">信安学院学生</el-button>
            <el-button size="small" @click="fill('student07')">计算机学院学生</el-button>
          </div>
        </div>
        <div class="tip">默认密码：123456</div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, User } from '@element-plus/icons-vue'
import { loginApi } from '../api/auth'
import { defaultHomeByRole } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})

const fill = (username) => {
  form.username = username
  form.password = '123456'
}

const login = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await loginApi(form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data))
    ElMessage.success('登录成功')
    router.push(defaultHomeByRole(data.userType))
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px;
  overflow: hidden;
  background:
    radial-gradient(circle at 18% 20%, rgba(42, 112, 160, 0.16), transparent 30%),
    radial-gradient(circle at 84% 76%, rgba(26, 95, 135, 0.14), transparent 34%),
    linear-gradient(150deg, #e7eef4 0%, #f6fafc 52%, #e8f0f5 100%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(30, 74, 110, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(30, 74, 110, 0.05) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at center, black 42%, transparent 86%);
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  pointer-events: none;
}

.orb-a {
  width: 300px;
  height: 300px;
  left: -80px;
  top: -50px;
  background: radial-gradient(circle, rgba(46, 129, 177, 0.32), rgba(46, 129, 177, 0.02) 70%);
  animation: floatA 8s ease-in-out infinite alternate;
}

.orb-b {
  width: 260px;
  height: 260px;
  right: -90px;
  top: 20%;
  background: radial-gradient(circle, rgba(19, 82, 132, 0.28), rgba(19, 82, 132, 0.02) 70%);
  animation: floatB 9s ease-in-out infinite alternate;
}

.orb-c {
  width: 220px;
  height: 220px;
  left: 35%;
  bottom: -100px;
  background: radial-gradient(circle, rgba(65, 145, 188, 0.2), rgba(65, 145, 188, 0.02) 70%);
  animation: floatC 10s ease-in-out infinite alternate;
}

.login-shell {
  position: relative;
  z-index: 2;
  width: 1040px;
  max-width: 100%;
  display: grid;
  grid-template-columns: 1.06fr 0.94fr;
  border-radius: 24px;
  border: 1px solid #d2e0eb;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  box-shadow: 0 34px 76px rgba(13, 44, 72, 0.16);
  animation: shellIn 520ms cubic-bezier(.2, .8, .25, 1) both;
}

.brand-panel {
  padding: 46px;
  color: #eaf4fc;
  background:
    radial-gradient(circle at 18% 12%, rgba(147, 211, 244, 0.2), transparent 38%),
    linear-gradient(160deg, #102f46 0%, #194766 62%, #236188 100%);
}

.brand-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.2px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(255, 255, 255, 0.14);
}

.brand-panel h1 {
  margin: 20px 0 10px;
  font-size: 34px;
  line-height: 1.22;
}

.brand-panel p {
  margin: 0;
  max-width: 420px;
  font-size: 15px;
  line-height: 1.75;
  color: rgba(231, 242, 251, 0.92);
}

.brand-line {
  margin-top: 26px;
  width: 86px;
  height: 4px;
  border-radius: 8px;
  background: linear-gradient(90deg, rgba(170, 220, 246, 0.95), rgba(120, 188, 223, 0.4));
}

.brand-points {
  margin-top: 22px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.brand-points span {
  padding: 7px 11px;
  border-radius: 999px;
  font-size: 12px;
  color: #f1f8fd;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.14);
}

.form-panel {
  padding: 42px 38px 34px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.88), rgba(248, 251, 253, 0.92)),
    radial-gradient(circle at 98% 0%, rgba(25, 102, 149, 0.06), transparent 45%);
}

.form-head h2 {
  margin: 2px 0 6px;
  font-size: 28px;
  color: #16324a;
}

.form-head small {
  color: #60788d;
}

.login-btn {
  width: 100%;
  margin-top: 4px;
}

.quick {
  margin-top: 18px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #dbe7f0;
  background: #f3f8fc;
}

.quick-title {
  font-size: 12px;
  font-weight: 700;
  color: #3a566f;
}

.quick-buttons {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tip {
  margin-top: 10px;
  color: #5f7387;
  font-size: 13px;
}

@keyframes shellIn {
  from {
    opacity: 0;
    transform: translateY(14px) scale(0.988);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes floatA {
  from {
    transform: translateY(0) translateX(0);
  }
  to {
    transform: translateY(14px) translateX(10px);
  }
}

@keyframes floatB {
  from {
    transform: translateY(0) translateX(0);
  }
  to {
    transform: translateY(-16px) translateX(-8px);
  }
}

@keyframes floatC {
  from {
    transform: translateY(0) translateX(0);
  }
  to {
    transform: translateY(-12px) translateX(10px);
  }
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    padding: 30px 24px;
  }

  .brand-panel h1 {
    font-size: 28px;
  }

  .form-panel {
    padding: 30px 22px 24px;
  }
}
</style>
