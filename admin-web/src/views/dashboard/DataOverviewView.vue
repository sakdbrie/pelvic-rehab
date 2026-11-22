<template>
  <!-- 原型图：04 数据总览页面（网页后台） -->
  <div class="page-main">
    <!-- 时间筛选：起止日期 -->
    <div class="prototype-card filter-bar">
      <span class="cap">时间筛选：</span>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起始日期" end-placeholder="结束日期" @change="resetPage" />
    </div>

    <!-- 状态筛选：数据异常/正常 + 打卡完成/未完成 -->
    <div class="prototype-card filter-bar">
      <span class="cap">数据状态：</span>
      <el-radio-group v-model="dataFilter" size="small" @change="resetPage">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="alert">异常</el-radio-button>
        <el-radio-button value="normal">正常</el-radio-button>
      </el-radio-group>
      <span class="divider" />
      <span class="cap">打卡状态：</span>
      <el-radio-group v-model="checkFilter" size="small" @change="resetPage">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="done">已完成</el-radio-button>
        <el-radio-button value="undone">未完成</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 搜索 -->
    <div class="search-row">
      <el-input v-model="keyword" placeholder="搜索姓名 / 患者编号" @keyup.enter="doSearch" />
      <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
    </div>

    <!-- KPI -->
    <div class="kpis">
      <div class="kpi"><div class="k">异常预警</div><div class="v red">{{ alertCount }}</div></div>
      <div class="kpi"><div class="k">今日打卡完成率</div><div class="v">78%</div></div>
    </div>

    <!-- 患者列表 -->
    <div class="prototype-card" style="padding: 0">
      <el-table :data="pagedRows" class="prototype-table" height="calc(100vh - 340px)">
        <el-table-column prop="id" label="患者编号" width="110" />
        <el-table-column prop="date" label="时间" width="110" />
        <el-table-column prop="name" label="姓名" width="110" />
        <el-table-column label="排尿次数" width="120">
          <template #default="{ row }">
            {{ row.urine }} 次
            <el-tag v-if="row.alert" size="small" class="tag-red">异常</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排便次数" width="100">
          <template #default="{ row }">{{ row.stool }} 次</template>
        </el-table-column>
        <el-table-column label="饮水量" width="100">
          <template #default="{ row }">{{ row.water }}ml</template>
        </el-table-column>
        <el-table-column label="打卡" width="110">
          <template #default="{ row }">
            <el-tag size="small" :class="row.done ? 'tag-ok' : 'tag-gray'">{{ row.done ? '已完成' : '未完成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="router.push(`/dashboard/patient/${row.id}`)">详情</el-link>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="prototype-card pager-row">
      <span class="info">共 {{ total }} 条，当前显示第 {{ total === 0 ? 0 : (currentPage - 1) * 30 + 1 }}-{{ Math.min(currentPage * 30, total) }} 条</span>
      <el-pagination v-model:current-page="currentPage" :page-size="30" :total="total" layout="prev, pager, next" background />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTablePage } from '@/composables/useTablePage'

// 原型图：04 数据总览页面（网页后台）——时间/状态筛选 + 搜索 + KPI + 患者列表
const router = useRouter()

interface OverviewRow {
  id: string
  date: string
  name: string
  urine: number
  stool: number
  water: number
  alert: boolean
  done: boolean
}

const dataFilter = ref<'all' | 'alert' | 'normal'>('all')
const checkFilter = ref<'all' | 'done' | 'undone'>('all')
const keyword = ref('')

// 默认展示近 30 天（原型逻辑）
const range = ref<[string, string]>([dateStr(-29), dateStr(0)])

function dateStr(offsetDays: number) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

// ============ 示例数据（原型占位：50 条，含日期 / 异常 / 打卡标记） ============
// TODO: 替换为接口数据（api/report.ts getOverview）
const names = ['王丽', '李梅', '张伟', '刘芳', '陈静', '杨帆', '赵敏', '孙磊', '周婷', '吴强', '郑爽', '王刚', '李娜', '张磊', '刘洋', '陈红', '杨明', '赵丽', '孙华', '周杰', '吴婷', '徐磊', '朱琳', '马超', '胡静', '郭涛', '林芳', '何伟', '高敏', '罗刚']

const allRows: OverviewRow[] = Array.from({ length:50 }, (_, i) => {
  const isAlert = (i + 1) % 7 === 0
  const isDone = (i + 1) % 3 !== 0
  return {
    id: 'P' + String(i + 1).padStart(3, '0'),
    date: dateStr(-(((i + 1) * 7) % 30)),
    name: names[i % names.length],
    urine: isAlert ? 3 : 2 + ((i + 1) % 5),
    stool: 1 + ((i + 1) % 3),
    water: 400 + (i + 1) * 30,
    alert: isAlert,
    done: isDone,
  }
})

const alertCount = computed(() => allRows.filter((r) => r.alert).length)

const filterFn = (row: OverviewRow) => {
  const [start, end] = range.value
  const dateMatch = row.date >= start && row.date <= end
  const dataMatch = dataFilter.value === 'all' || (dataFilter.value === 'alert' ? row.alert : !row.alert)
  const checkMatch = checkFilter.value === 'all' || (checkFilter.value === 'done' ? row.done : !row.done)
  const kw = keyword.value.trim().toLowerCase()
  const searchMatch = kw === '' || (row.id + row.name).toLowerCase().includes(kw)
  return dateMatch && dataMatch && checkMatch && searchMatch
}

const { currentPage, total, pagedRows, resetPage } = useTablePage<OverviewRow>(() => allRows, filterFn, 30)

function doSearch() {
  resetPage()
}
</script>

<style scoped>
.cap {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
}
.divider {
  width: 1px;
  height: 18px;
  background: #e5e7eb;
  margin: 0 6px;
}
.search-row {
  display: flex;
  gap: 8px;
  max-width: 420px;
}
.kpis {
  display: flex;
  gap: 12px;
}
.kpi {
  flex: 1;
  max-width: 260px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.kpi .k {
  font-size: 12px;
  color: #6b7280;
}
.kpi .v {
  font-size: 22px;
  font-weight: 700;
}
.kpi .v.red {
  color: #dc2626;
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
