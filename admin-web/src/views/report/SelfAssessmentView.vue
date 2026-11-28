<template>
  <!-- 原型图：07-5 盆底自评数据表（网页后台） -->
  <div class="page-main">
    <div class="top-row">
      <el-link type="primary" :underline="false" @click="router.push('/reports')">‹ 返回</el-link>
      <el-button type="primary" size="small" @click="onExport">导出 Excel</el-button>
    </div>

    <div class="prototype-card filter-bar">
      <span class="cap">填写时间：</span>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起始日期" end-placeholder="结束日期" @change="doSearch" />
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索姓名 / 患者编号 / 记录ID" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
      </div>
    </div>

    <div class="prototype-card" style="padding: 0">
      <el-table v-loading="loading" :data="rows" class="prototype-table" height="calc(100vh - 250px)" style="width: 100%">
        <el-table-column prop="recordId" label="记录唯一ID" width="120" />
        <el-table-column prop="patientId" label="患者唯一ID" width="120" />
        <el-table-column prop="name" label="患者姓名" width="110" />
        <el-table-column prop="scaleName" label="量表名称" width="150" />
        <el-table-column prop="fillTime" label="填写时间" width="160" />
        <el-table-column prop="totalScore" label="总得分" width="100" />
        <el-table-column prop="dimScores" label="各维度得分" min-width="220" show-overflow-tooltip />
        <el-table-column prop="result" label="评估结果" min-width="180" show-overflow-tooltip />
      </el-table>
    </div>

    <div class="prototype-card pager-row">
      <span class="info">共 {{ total }} 条，当前显示第 {{ total === 0 ? 0 : (currentPage - 1) * 10 + 1 }}-{{ Math.min(currentPage * 10, total) }} 条</span>
      <el-pagination v-model:current-page="currentPage" :page-size="10" :total="total" layout="prev, pager, next" background @current-change="onPageChange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getReportData, exportReport } from '@/api/report'

// 原型图：07-5 盆底自评数据表（网页后台）
const router = useRouter()

// ============ 接口数据（api/report.ts getReportData, reportType=assessment） ============
function dateStr(offsetDays: number) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const range = ref<[string, string]>([dateStr(-29), dateStr(0)])
const keyword = ref('')
const rows = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)

function buildParams() {
  return {
    startDate: range.value?.[0],
    endDate: range.value?.[1],
    keyword: keyword.value.trim() || undefined,
    page: currentPage.value,
    size: 10,
  }
}

async function fetch() {
  loading.value = true
  try {
    const res = await getReportData('assessment', buildParams())
    rows.value = res.records
    total.value = res.total
  } catch {
    /* 错误已由 request 统一提示 */
  } finally {
    loading.value = false
  }
}

function doSearch() {
  currentPage.value = 1
  fetch()
}

function onPageChange(page: number) {
  currentPage.value = page
  fetch()
}

async function onExport() {
  try {
    await exportReport('assessment', buildParams())
  } catch {
    return
  }
}

onMounted(fetch)
</script>

<style scoped>
.top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.cap {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
}
.search-area {
  display: flex;
  gap: 8px;
  margin-left: auto;
  max-width: 420px;
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
