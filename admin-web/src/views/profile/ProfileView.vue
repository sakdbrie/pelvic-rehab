<template>
  <!-- 原型图：08 个人主页（网页后台）——个人信息 + 修改密码 + 退出登录 -->
  <div class="page-main">
    <div class="top-row">
      <span class="page-title">个人主页</span>
      <el-button size="small" @click="onLogout">退出登录</el-button>
    </div>

    <!-- 医生信息 -->
    <div class="prototype-card">
      <div class="profile-top">
        <div class="avatar">陈</div>
        <div>
          <div class="pt-name">
            陈医生
            <el-tag size="small" class="tag-ok">医生</el-tag>
          </div>
          <div class="pt-sub">盆底康复科 · 医生编号 D001</div>
        </div>
        <el-button type="primary" size="small" style="margin-left: auto" @click="openModal">修改密码</el-button>
      </div>

      <div class="h1" style="margin-top: 12px">基本信息</div>
      <div class="info-grid">
        <div v-for="it in info" :key="it.k" class="info-item">
          <span class="k">{{ it.k }}</span>
          <span class="v" :class="{ pwd: it.k === '密码' }">{{ it.k === '密码' ? mask : it.v }}</span>
        </div>
      </div>
    </div>

    <!-- 规则说明 -->
    <div class="prototype-card">
      <div class="cap">账号说明</div>
      <div class="tip">
        账号信息由管理员统一维护，如有变更请联系系统管理员。为保障数据安全，建议定期修改登录密码，新密码长度不少于 6 位。
      </div>
    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="modalVisible" title="修改密码" width="360px" @closed="closeModal">
      <el-form label-position="top">
        <el-form-item label="原密码">
          <el-input v-model="oldPwd" type="password" placeholder="请输入原密码" autocomplete="current-password" show-password @keyup.enter="onOk" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="newPwd" type="password" placeholder="请输入新密码（不少于 6 位）" autocomplete="new-password" show-password @keyup.enter="onOk" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="confirmPwd" type="password" placeholder="请再次输入新密码" autocomplete="new-password" show-password @keyup.enter="onOk" />
        </el-form-item>
      </el-form>
      <div v-if="errMsg" class="tipbox">{{ errMsg }}</div>
      <template #footer>
        <el-button @click="modalVisible = false">取 消</el-button>
        <el-button type="primary" @click="onOk">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changePassword, logout } from '@/api/auth'
import { useUserStore } from '@/stores/user'

// 原型图：08 个人主页（网页后台）——信息展示、修改密码弹窗（校验：原密码正确/不少于6位/两次一致）、退出登录
const router = useRouter()
const userStore = useUserStore()

// TODO: 替换为接口数据（当前登录用户信息）
const info = [
  { k: '姓名', v: '陈医生' },
  { k: '医生 ID', v: 'D001' },
  { k: '科室', v: '盆底康复科' },
  { k: '电话号', v: '139****0000' },
  { k: '密码', v: '' },
]

/* 当前密码（原型占位数据） */
let currentPwd = '123456'
const mask = computed(() => '*'.repeat(currentPwd.length))

const modalVisible = ref(false)
const oldPwd = ref('')
const newPwd = ref('')
const confirmPwd = ref('')
const errMsg = ref('')

function openModal() {
  oldPwd.value = ''
  newPwd.value = ''
  confirmPwd.value = ''
  errMsg.value = ''
  modalVisible.value = true
}

function closeModal() {
  errMsg.value = ''
}

function onOk() {
  const o = oldPwd.value.trim()
  const n = newPwd.value.trim()
  const c = confirmPwd.value.trim()

  if (!o) return (errMsg.value = '请输入原密码')
  if (o !== currentPwd) return (errMsg.value = '原密码不正确')
  if (!n) return (errMsg.value = '请输入新密码')
  if (n.length < 6) return (errMsg.value = '新密码长度不少于 6 位')
  if (n !== c) return (errMsg.value = '两次输入的新密码不一致')

  currentPwd = n
  modalVisible.value = false
  // TODO: 联调后端修改密码接口（api/auth.ts changePassword）
  void changePassword({ oldPassword: o, newPassword: n })
  ElMessage.success('密码修改成功（原型占位）')
}

async function onLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  // TODO: 联调后端登出接口（api/auth.ts logout）
  void logout()
  userStore.setUserInfo(null)
  router.push('/login')
}
</script>

<style scoped>
.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-title {
  font-size: 15px;
  font-weight: 600;
}
.profile-top {
  display: flex;
  gap: 12px;
  align-items: center;
}
.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #1e9e8f;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
}
.pt-name {
  font-size: 16px;
  font-weight: 700;
  display: flex;
  gap: 8px;
  align-items: center;
}
.pt-sub {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 8px 16px;
  margin-top: 8px;
}
.info-item {
  display: flex;
  gap: 8px;
  align-items: center;
}
.info-item .k {
  font-size: 12px;
  color: #6b7280;
}
.info-item .v {
  font-size: 13px;
}
.info-item .v.pwd {
  letter-spacing: 2px;
}
.cap {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
  padding-bottom: 6px;
}
.tip {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.8;
}
.tipbox {
  border: 1px dashed #f87171;
  background: #fdecec;
  color: #dc2626;
  font-size: 12px;
  line-height: 1.6;
  padding: 8px 10px;
  border-radius: 8px;
  margin-top: 4px;
}
</style>
