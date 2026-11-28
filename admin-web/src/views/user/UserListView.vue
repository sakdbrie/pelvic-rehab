<template>
  <!-- 原型图：01-1 用户管理页面（网页后台-管理员身份）/ 01-2 用户管理页面（网页后台-医生身份） -->
  <div class="page-main">
    <div class="prototype-card filter-bar">
      <!-- 管理员：角色筛选 + 搜索 + 添加医生；医生：仅患者列表 + 搜索 -->
      <template v-if="isAdmin">
        <span class="cap">角色筛选：</span>
        <el-radio-group v-model="roleFilter" size="small" @change="onRoleChange">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button value="patient">患者</el-radio-button>
          <el-radio-button value="doctor">医生</el-radio-button>
        </el-radio-group>
      </template>
      <template v-else>
        <span class="cap">患者列表</span>
      </template>

      <div class="search-area">
        <el-input v-model="keyword" placeholder="输入姓名或编号搜索" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
      </div>

      <el-button v-if="isAdmin" type="primary" size="small" @click="addDialog = true">+ 添加医生</el-button>
    </div>

    <div class="prototype-card" style="padding: 0">
      <el-table v-loading="loading" :data="rows" class="prototype-table" height="calc(100vh - 190px)">
        <el-table-column prop="id" label="编号" width="120" />
        <el-table-column prop="name" label="姓名" width="140" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :class="row.role === 'patient' ? 'tag-ok' : ''" size="small">{{ row.role === 'patient' ? '患者' : '医生' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="goDetail(row)">详情</el-link>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="prototype-card pager-row">
      <span class="info">共 {{ total }} 条，当前显示第 {{ total === 0 ? 0 : (currentPage - 1) * 30 + 1 }}-{{ Math.min(currentPage * 30, total) }} 条</span>
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="30"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="onPageChange"
      />
    </div>

    <!-- 添加医生 -->
    <el-dialog v-model="addDialog" title="添加医生" width="420px">
      <el-form :model="addForm" label-width="70px">
        <el-form-item label="姓名"><el-input v-model="addForm.name" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="addForm.gender" placeholder="请选择">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="addForm.deptId" placeholder="请选择">
            <el-option label="盆底康复科" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="addForm.phone" placeholder="请输入手机号" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog = false">取 消</el-button>
        <el-button type="primary" @click="onAddDoctor">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { addDoctor, getUserList } from '@/api/system'
import type { UserListRow } from '@/api/system'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const roleFilter = ref<'all' | 'patient' | 'doctor'>('all')
const keyword = ref('')

const rows = ref<UserListRow[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: keyword.value.trim() || undefined,
      role: roleFilter.value === 'all' ? undefined : roleFilter.value,
      page: currentPage.value,
      size: 30,
    })
    rows.value = res.records
    total.value = res.total
  } catch {
    /* 拦截器已提示 */
  } finally {
    loading.value = false
  }
}

function doSearch() {
  currentPage.value = 1
  fetchUsers()
}

function onRoleChange() {
  currentPage.value = 1
  fetchUsers()
}

function onPageChange(page: number) {
  currentPage.value = page
  fetchUsers()
}

function goDetail(row: UserListRow) {
  // 功能文档：查看患者 → 02 患者详情；查看医生 → 03 医生详情
  // TODO: 原型图 01-1 中详情统一指向 #p25d（患者数据详情），与功能文档跳转约定不一致，按功能文档实现
  if (row.role === 'patient') {
    router.push(`/user/patient/${row.id}`)
  } else {
    router.push(`/user/doctor/${row.id}`)
  }
}

const addDialog = ref(false)
const addForm = reactive<{ name: string; gender: string; deptId: number | ''; phone: string }>({
  name: '',
  gender: '',
  deptId: '',
  phone: '',
})

async function onAddDoctor() {
  if (!addForm.name.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (!addForm.gender) {
    ElMessage.warning('请选择性别')
    return
  }
  if (!addForm.deptId) {
    ElMessage.warning('请选择科室')
    return
  }
  if (!addForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(addForm.phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }
  try {
    await addDoctor({ name: addForm.name.trim(), gender: addForm.gender, deptId: 1, phone: addForm.phone })
    ElMessage.success('已添加医生')
    addDialog.value = false
    addForm.name = ''
    addForm.gender = ''
    addForm.deptId = ''
    addForm.phone = ''
    fetchUsers()
  } catch {
    /* 已提示 */
  }
}

onMounted(fetchUsers)
</script>

<style scoped>
.cap {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
}
.search-area {
  display: flex;
  gap: 8px;
  margin-left: auto;
  max-width: 360px;
  flex: 1;
}
.pager-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.info {
  font-size: 12px;
  color: #6b7280;
}
</style>
