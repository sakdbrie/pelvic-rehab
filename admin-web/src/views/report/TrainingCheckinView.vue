<template>
  <!-- 原型图：07-6 康复训练打卡数据表（网页后台） -->
  <div class="page-main">
    <div class="top-row">
      <el-link type="primary" :underline="false" @click="router.push('/reports')">‹ 返回</el-link>
      <el-button type="primary" size="small" @click="onExport">导出 Excel</el-button>
    </div>

    <div class="prototype-card filter-bar">
      <span class="cap">打卡日期：</span>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起始日期" end-placeholder="结束日期" @change="resetPage" />
    </div>

    <div class="prototype-card filter-bar">
      <span class="cap">训练类型：</span>
      <el-radio-group v-model="typeFilter" size="small" @change="resetPage">
        <el-radio-button value="全部">全部</el-radio-button>
        <el-radio-button value="慢肌训练">慢肌训练</el-radio-button>
        <el-radio-button value="快肌训练">快肌训练</el-radio-button>
        <el-radio-button value="感知训练">感知训练</el-radio-button>
        <el-radio-button value="呼吸训练">呼吸训练</el-radio-button>
      </el-radio-group>
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索姓名 / 患者编号 / 项目名称" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
      </div>
    </div>

    <div class="prototype-card" style="padding: 0">
      <el-table :data="pagedRows" class="prototype-table" height="calc(100vh - 300px)" style="width: 100%">
        <el-table-column prop="id" label="患者编号" width="110" />
        <el-table-column prop="title" label="训练项目名称" min-width="160" />
        <el-table-column prop="type" label="训练类型" width="120" />
        <el-table-column prop="date" label="打卡日期" width="120" />
        <el-table-column prop="duration" label="训练时长(分钟)" width="140" />
      </el-table>
    </div>

    <div class="prototype-card pager-row">
      <span class="info">共 {{ total }} 条，当前显示第 {{ total === 0 ? 0 : (currentPage - 1) * 10 + 1 }}-{{ Math.min(currentPage * 10, total) }} 条</span>
      <el-pagination v-model:current-page="currentPage" :page-size="10" :total="total" layout="prev, pager, next" background />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useTablePage } from '@/composables/useTablePage'
import { submitExportTask } from '@/api/report'

// 原型图：07-6 康复训练打卡数据表（网页后台）
const router = useRouter()

// ============ 示例数据（原型占位） ============
// TODO: 替换为接口数据（api/report.ts getReportData, reportType=training）
function dateStr(offsetDays: number) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const TRAIN_POOL = [
  { title: '3s慢肌训练', type: '慢肌训练', duration: 3 },
  { title: '快肌训练', type: '快肌训练', duration: 3 },
  { title: '盆底肌感知训练', type: '感知训练', duration: 5 },
  { title: '三维腹式呼吸训练', type: '呼吸训练', duration: 4 },
]

const allRows = Array.from({ length: 80 }, (_, i) => {
  const t = TRAIN_POOL[i % TRAIN_POOL.length]
  return {
    id: 'P' + String((i % 10) + 1).padStart(3, '0'),
    title: t.title,
    type: t.type,
    date: dateStr(-((i * 4) % 30)),
    duration: t.duration + ' 分钟',
  }
})

const range = ref<[string, string]>([dateStr(-29), dateStr(0)])
const typeFilter = ref('全部')
const keyword = ref('')

const filterFn = (row: (typeof allRows)[number]) => {
  const [start, end] = range.value
  const dateMatch = row.date >= start && row.date <= end
  const typeMatch = typeFilter.value === '全部' || row.type === typeFilter.value
  const kw = keyword.value.trim().toLowerCase()
  const searchMatch = kw === '' || (row.id + row.title).toLowerCase().includes(kw)
  return dateMatch && typeMatch && searchMatch
}
const { currentPage, total, pagedRows, resetPage } = useTablePage(() => allRows, filterFn, 10)

function doSearch() {
  resetPage()
}

function onExport() {
  // TODO: 联调后端异步导出任务
  void submitExportTask('training', {})
  ElMessage.success('导出任务已提交（原型占位）')
}
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
