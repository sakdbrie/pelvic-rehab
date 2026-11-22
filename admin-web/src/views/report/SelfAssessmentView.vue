<template>
  <!-- 原型图：07-5 盆底自评数据表（网页后台） -->
  <div class="page-main">
    <div class="top-row">
      <el-link type="primary" :underline="false" @click="router.push('/reports')">‹ 返回</el-link>
      <el-button type="primary" size="small" @click="onExport">导出 Excel</el-button>
    </div>

    <div class="prototype-card filter-bar">
      <span class="cap">填写时间：</span>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起始日期" end-placeholder="结束日期" @change="resetPage" />
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索姓名 / 患者编号 / 记录ID" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
      </div>
    </div>

    <div class="prototype-card" style="padding: 0">
      <el-table :data="pagedRows" class="prototype-table" height="calc(100vh - 250px)" style="width: 100%">
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

// 原型图：07-5 盆底自评数据表（网页后台）
const router = useRouter()

// ============ 示例数据（原型占位；量表清单与页面 05 一致） ============
// TODO: 替换为接口数据（api/report.ts getReportData, reportType=assessment）
function dateStr(offsetDays: number) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const SCALES = [
  { name: 'PFDI-20', fullName: '盆底功能障碍简表' },
  { name: 'PISQ-12', fullName: '盆腔脏器脱垂/尿失禁性功能问卷' },
  { name: 'IIQ-7', fullName: '尿失禁影响问卷简表' },
  { name: '盆底肌力', fullName: '盆底肌力自评量表' },
  { name: 'FSFI', fullName: '女性性功能指数' },
  { name: 'PAC-SYM', fullName: '便秘患者症状自评量表' },
  { name: 'ICI-Q-SF', fullName: '国际尿失禁咨询问卷简表' },
  { name: 'OAB-q', fullName: '膀胱过度活动症问卷' },
  { name: 'PFIQ-7', fullName: '盆底影响问卷简表' },
]
const names = ['王丽', '李梅', '张伟', '刘芳', '陈静', '杨帆', '赵敏', '孙磊', '周婷', '吴强']

const allRows = Array.from({ length: 90 }, (_, i) => {
  const s = SCALES[i % SCALES.length]
  const date = dateStr(-((i * 7) % 30))
  return {
    recordId: 'R' + String(100000 + i * 137),
    patientId: 'P' + String((i % 10) + 1).padStart(3, '0'),
    name: names[i % names.length],
    scaleName: s.name,
    fillTime: `${date} ${String(9 + (i % 12)).padStart(2, '0')}:${String((i * 19) % 60).padStart(2, '0')}`,
    totalScore: String(20 + (i * 31) % 80) + '.0',
    dimScores: `维度一：${10 + (i % 20)}；维度二：${8 + (i % 15)}`,
    result: i % 5 === 0 ? '轻度异常，建议随访' : '无明显异常',
  }
})

const range = ref<[string, string]>([dateStr(-29), dateStr(0)])
const keyword = ref('')

const filterFn = (row: (typeof allRows)[number]) => {
  const [start, end] = range.value
  const dateMatch = row.fillTime.slice(0, 10) >= start && row.fillTime.slice(0, 10) <= end
  const kw = keyword.value.trim().toLowerCase()
  const searchMatch = kw === '' || (row.patientId + row.name + row.recordId).toLowerCase().includes(kw)
  return dateMatch && searchMatch
}
const { currentPage, total, pagedRows, resetPage } = useTablePage(() => allRows, filterFn, 10)

function doSearch() {
  resetPage()
}

function onExport() {
  // TODO: 联调后端异步导出任务
  void submitExportTask('assessment', {})
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
