<template>
  <!-- 原型图：00 后台登录页（网页后台） -->
  <div class="login-bg">
    <div class="login-card">
      <div class="brand">
        <el-icon :size="44" color="#1E9E8F">
          <Lock />
        </el-icon>
        <div class="brand-title">盆底康复管理 · 网页后台</div>
      </div>

      <!-- 登录方式切换 -->
      <div class="tabs">
        <div class="tab" :class="{ on: tab === 'pwd' }" @click="tab = 'pwd'">密码登录</div>
        <div class="tab" :class="{ on: tab === 'sms' }" @click="tab = 'sms'">短信验证码登录</div>
      </div>

      <!-- 密码登录 -->
      <div v-show="tab === 'pwd'" class="panel">
        <el-input v-model="form.phone" placeholder="请输入手机号" autocomplete="username" />
        <el-input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password"
          show-password @keyup.enter="onLogin" />
        <el-button type="primary" class="login-btn" @click="onLogin">登 录</el-button>
      </div>

      <!-- 短信验证码登录 -->
      <div v-show="tab === 'sms'" class="panel">
        <el-input v-model="form.phone" placeholder="请输入手机号" autocomplete="tel" />
        <div class="code-row">
          <el-input v-model="form.smsCode" placeholder="请输入短信验证码" inputmode="numeric" />
          <el-button class="sms-btn" @click="onSendCode">获取验证码</el-button>
        </div>
        <el-button type="primary" class="login-btn" @click="onLogin">登 录</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
import { loginByPassword, loginBySms, sendSmsCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { setToken } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()

const tab = ref<'pwd' | 'sms'>('pwd')
const form = reactive({
  password: '',
  phone: '',
  smsCode: '',
})

async function onSendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning(form.phone ? '请输入正确的手机号格式' : '请输入手机号')
    return
  }
  try {
    await sendSmsCode(form.phone)
    ElMessage.success('验证码已发送')
  } catch {
    return
  }
}

async function onLogin() {
  // 校验 phone、password、smsCode 是否已填且格式正确
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning(form.phone ? '请输入正确的手机号格式' : '请输入手机号')
    return
  }
  if (tab.value === 'pwd') {
    if (!form.password) {
      ElMessage.warning('请输入密码')
      return
    }
  } else if (tab.value === 'sms') {
    if (!form.smsCode || form.smsCode.length !== 6) {
      ElMessage.warning('请输入 6 位短信验证码')
      return
    }
  }

  try {
    const res = tab.value === 'pwd'
      ? await loginByPassword({ phone: form.phone, password: form.password })
      : await loginBySms({ phone: form.phone, smsCode: form.smsCode })
    setToken(res.accessToken)
    userStore.setUserInfo({
      userId: res.user.userId ?? '',
      name: res.account.nickname,
      role: res.user.roleCode === 'ADMIN' ? 'ADMIN' : 'DOCTOR',
      deptName: '',
      phone: '',
      permissions: res.user.permissions,
    })
    router.push('/user')
  } catch {
    /* 拦截器已提示 */
  }
}
</script>

<style scoped>
.login-bg {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eaf6f4, #f7f8fa);
}

.login-card {
  width: 360px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.brand-title {
  font-size: 17px;
  font-weight: 700;
}

.tabs {
  display: flex;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 3px;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 6px 0;
  border-radius: 6px;
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
}

.tab.on {
  background: #fff;
  color: #1e9e8f;
  font-weight: 600;
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.code-row {
  display: flex;
  gap: 8px;
}

.sms-btn {
  flex-shrink: 0;
}

.login-btn {
  width: 100%;
}
</style>
