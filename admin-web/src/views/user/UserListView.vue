<template>
  <!-- 原型图：01-1 用户管理页面（网页后台-管理员身份）/ 01-2 用户管理页面（网页后台-医生身份） -->
  <div class="page-main">
    <div class="prototype-card filter-bar">
      <!-- 管理员：角色筛选 + 搜索 + 添加医生；医生：仅患者列表 + 搜索 -->
      <template v-if="isAdmin">
        <span class="cap">角色筛选：</span>
        <el-radio-group v-model="roleFilter" size="small" @change="resetPage">
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
      <el-table :data="pagedRows" class="prototype-table" height="calc(100vh - 190px)">
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
      />
    </div>

    <!-- 添加医生（原型占位 → 功能文档：补充姓名、性别和科室） -->
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
            <el-option label="盆底康复科" value="盆底康复科" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog = false">取 消</el-button>
        <el-button type="primary" @click="onAddDoctor">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useTablePage } from '@/composables/useTablePage'
import { addDoctor } from '@/api/system'
import type { UserRow } from '@/types'

// 原型图：01-1（管理员：角色筛选+添加医生）/ 01-2（医生：仅患者列表）
const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)

const roleFilter = ref<'all' | 'patient' | 'doctor'>('all')
const keyword = ref('')

// ============ 示例数据（原型占位：60 条患者 + 30 条医生） ============
// TODO: 替换为接口数据（api/system.ts getUserList）
const surnames = ['王', '李', '张', '刘', '陈', '杨', '赵', '黄', '周', '吴', '徐', '孙', '马', '朱', '胡', '林', '郭', '何', '高', '罗']
const givenNames = ['丽', '梅', '伟', '芳', '娜', '敏', '静', '强', '磊', '洋', '艳', '勇', '军', '杰', '涛', '婷', '雪', '鹏', '宇', '鑫']
const doctorTitles = ['陈医生', '刘医生', '李医生', '王医生', '张医生', '杨医生', '赵医生', '黄医生', '周医生', '吴医生']

const allUsers: UserRow[] = [
  ...Array.from({ length: 60 }, (_, i) => ({
    id: 'P' + String(i + 1).padStart(3, '0'),
    name: surnames[(i + 1) % surnames.length] + givenNames[(i + 1) % givenNames.length],
    role: 'patient' as const,
    phone: '138' + String(10000000 + ((i + 1) * 137) % 90000000).slice(0, 4) + '****',
  })),
  ...Array.from({ length: 30 }, (_, j) => ({
    id: 'D' + String(j + 1).padStart(3, '0'),
    name: doctorTitles[(j + 1) % doctorTitles.length],
    role: 'doctor' as const,
    phone: '139' + String(10000000 + ((j + 1) * 173) % 90000000).slice(0, 4) + '****',
  })),
]

const filterFn = (row: UserRow) => {
  const roleMatch = roleFilter.value === 'all' || row.role === roleFilter.value
  const kw = keyword.value.trim().toLowerCase()
  const searchMatch = kw === '' || (row.id + row.name).toLowerCase().includes(kw)
  return roleMatch && searchMatch
}

const { currentPage, total, pagedRows, resetPage } = useTablePage<UserRow>(() => allUsers, filterFn, 30)

function doSearch() {
  resetPage()
}

function goDetail(row: UserRow) {
  // 功能文档：查看患者 → 02 患者详情；查看医生 → 03 医生详情
  // TODO: 原型图 01-1 中详情统一指向 #p25d（患者数据详情），与功能文档跳转约定不一致，按功能文档实现
  if (row.role === 'patient') {
    router.push(`/user/patient/${row.id}`)
  } else {
    router.push(`/user/doctor/${row.id}`)
  }
}

const addDialog = ref(false)
const addForm = reactive({ name: '', gender: '', deptId: '' })

function onAddDoctor() {
  if (!addForm.name || !addForm.gender || !addForm.deptId) {
    ElMessage.warning('请补充该医生的姓名、性别和科室')
    return
  }
  // TODO: 联调后端添加医生接口（api/system.ts addDoctor）
  void addDoctor({ name: addForm.name, gender: addForm.gender, deptId: addForm.deptId })
  ElMessage.success('已添加医生（原型占位）')
  addDialog.value = false
  addForm.name = ''
  addForm.gender = ''
  addForm.deptId = ''
}
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
