<template>
  <!-- 原型图：03 用户信息详情页面（网页后台-医生端） -->
  <div class="page-main">
    <div class="top-row">
      <el-link type="primary" :underline="false" @click="router.push('/user')">‹ 返回</el-link>
      <el-button type="danger" size="small" @click="onRemove">移 除</el-button>
    </div>

    <!-- 医生信息 -->
    <div class="prototype-card">
      <div class="profile-top">
        <div class="avatar">{{ detail?.name?.charAt(0) || '医' }}</div>
        <div>
          <div class="pt-name">
            {{ detail?.name ?? '—' }}
            <el-tag size="small" class="tag-ok">医生</el-tag>
          </div>
          <div class="pt-sub">{{ detail?.deptName ?? '—' }} · 医生编号 {{ detail?.id ?? '—' }}</div>
        </div>
      </div>

      <div class="h1" style="margin-top: 12px">基本信息</div>
      <div class="ro-grid">
        <span class="ro-item"><span class="h2">姓名</span><span class="h3">{{ detail?.name ?? '—' }}</span></span>
        <span class="ro-item"><span class="h2">医生 ID</span><span class="h3">{{ detail?.id ?? '—' }}</span></span>
        <span class="ro-item"><span class="h2">科室</span><span class="h3">{{ detail?.deptName ?? '—' }}</span></span>
        <span class="ro-item"><span class="h2">职称</span><span class="h3">{{ detail?.title ?? '—' }}</span></span>
        <span class="ro-item"><span class="h2">电话号</span><span class="h3">{{ detail?.phone ?? '—' }}</span></span>
        <span class="ro-item"><span class="h2">密码</span><span class="h3 pwd">********</span></span>
      </div>
    </div>

    <!-- 账号说明 -->
    <div class="prototype-card">
      <div class="h1">账号说明</div>
      <div class="h3" style="margin-top: 8px">
        账号信息由管理员统一维护，如有变更请联系系统管理员。为保障数据安全，建议定期修改登录密码，新密码长度不少于 6 位。
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDoctorDetail, removeDoctor } from '@/api/system'
import type { DoctorDetail } from '@/api/system'

const route = useRoute()
const router = useRouter()

const detail = ref<DoctorDetail | null>(null)

onMounted(async () => {
  try {
    detail.value = await getDoctorDetail(String(route.params.id))
  } catch {
    return
  }
})

async function onRemove() {
  await ElMessageBox.confirm('确定要移除该医生账号吗？', '提示', { type: 'warning' })
  try {
    await removeDoctor(String(route.params.id))
    ElMessage.success('已移除')
    router.push('/user')
  } catch {
    return
  }
}
</script>

<style scoped>
.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
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
.ro-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 8px 16px;
  margin-top: 8px;
}
.ro-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.pwd {
  letter-spacing: 2px;
}
</style>
