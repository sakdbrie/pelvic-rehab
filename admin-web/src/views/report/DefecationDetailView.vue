<template>
  <!-- 原型图：07-4 排便日记明细表（网页后台） -->
  <div class="page-main">
    <div class="top-row">
      <el-link type="primary" :underline="false" @click="router.push('/reports')">‹ 返回</el-link>
      <el-button type="primary" size="small" @click="onExport">导出 Excel</el-button>
    </div>

    <div class="prototype-card filter-bar">
      <span class="cap">时间筛选：</span>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起始日期" end-placeholder="结束日期" @change="resetPage" />
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索姓名 / 患者编号" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
      </div>
    </div>

    <div class="prototype-card" style="padding: 0">
      <el-table :data="pagedRows" class="prototype-table" height="calc(100vh - 250px)" style="width: 100%">
        <el-table-column prop="id" label="患者编号" width="110" />
        <el-table-column prop="name" label="患者姓名" width="110" />
        <el-table-column prop="date" label="记录日期" width="110" />
        <el-table-column prop="stoolType" label="大便类型" width="110" />
        <el-table-column prop="speed" label="排便速度" width="110" />
        <el-table-column prop="volume" label="排便量" width="110" />
        <el-table-column prop="other" label="其他（如排不尽感、便失禁等）" min-width="200" show-overflow-tooltip />
        <el-table-column prop="recordTime" label="录入时间" width="160" />
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

// 原型图：07-4 排便日记明细表（网页后台）
const router = useRouter()

// ============ 示例数据（原型占位） ============
// TODO: 替换为接口数据（api/report.ts getReportData, reportType=defecation）
function dateStr(offsetDays: number) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const stoolTypes = ['成型', '稀便', '水样便']
const speeds = ['慢', '正常', '快']
const volumes = ['少', '正常', '多']
const names = ['王丽', '李梅', '张伟', '刘芳', '陈静', '杨帆', '赵敏', '孙磊', '周婷', '吴强']

const allRows = Array.from({ length: 60 }, (_, i) => ({
  id: 'P' + String((i % 10) + 1).padStart(3, '0'),
  name: names[i % names.length],
  date: dateStr(-((i * 5) % 30)),
  stoolType: stoolTypes[i % stoolTypes.length],
  speed: speeds[i % speeds.length],
  volume: volumes[i % volumes.length],
  other: i % 6 === 0 ? '排不尽感' : i % 9 === 0 ? '便失禁' : '—',
  recordTime: `${dateStr(-((i * 5) % 30))} ${String(7 + (i % 14)).padStart(2, '0')}:${String((i * 23) % 60).padStart(2, '0')}`,
}))

const range = ref<[string, string]>([dateStr(-29), dateStr(0)])
const keyword = ref('')

const filterFn = (row: (typeof allRows)[number]) => {
  const [start, end] = range.value
  const dateMatch = row.date >= start && row.date <= end
  const kw = keyword.value.trim().toLowerCase()
  const searchMatch = kw === '' || (row.id + row.name).toLowerCase().includes(kw)
  return dateMatch && searchMatch
}
const { currentPage, total, pagedRows, resetPage } = useTablePage(() => allRows, filterFn, 10)

function doSearch() {
  resetPage()
}

function onExport() {
  // TODO: 联调后端异步导出任务
  void submitExportTask('defecation', {})
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
