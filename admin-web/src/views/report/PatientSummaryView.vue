<template>
  <!-- 原型图：07-7 患者数据汇总表（网页后台） -->
  <div class="page-main">
    <div class="top-row">
      <el-link type="primary" :underline="false" @click="router.push('/reports')">‹ 返回</el-link>
      <el-button type="primary" size="small" @click="onExport">导出 Excel</el-button>
    </div>

    <div class="prototype-card filter-bar">
      <span class="cap">统计开始日期：</span>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起始日期" end-placeholder="结束日期" @change="doSearch" />
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索姓名 / 患者编号" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
      </div>
    </div>

    <div class="prototype-card" style="padding: 0">
      <el-table v-loading="loading" :data="rows" class="prototype-table" height="calc(100vh - 250px)" style="width: 100%">
        <el-table-column prop="id" label="患者编号" width="110" />
        <el-table-column prop="name" label="患者姓名" width="110" />
        <el-table-column prop="statDate" label="统计开始日期" width="120" />
        <el-table-column prop="scaleName" label="自评表名称" width="120" />
        <el-table-column prop="scaleScore" label="自评表得分" width="110" />
        <el-table-column prop="assessDate" label="评估日期" width="120" />
        <el-table-column prop="urineCount" label="排尿日记（每日记录次数）" width="190" />
        <el-table-column prop="stoolCount" label="排便日记" width="100" />
        <el-table-column prop="abnormalCount" label="异常（漏尿、导尿）排尿次数" width="190" />
        <el-table-column prop="checkinStatus" label="训练打卡完成情况" width="150" />
        <el-table-column prop="alertCount" label="异常预警次数" width="120" />
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

// 原型图：07-7 患者数据汇总表（网页后台）
const router = useRouter()

// ============ 接口数据（api/report.ts getReportData, reportType=summary） ============
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
    const res = await getReportData('summary', buildParams())
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
    await exportReport('summary', buildParams())
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
