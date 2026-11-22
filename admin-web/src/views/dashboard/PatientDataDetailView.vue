<template>
  <!-- 原型图：05 患者数据详情页面（网页后台）——左记录表格 / 右折线图或打卡日历 -->
  <div class="page-main">
    <div class="back-row">
      <el-link type="primary" :underline="false" @click="router.push('/dashboard')">‹ 返回</el-link>
      <span class="detail-title">患者数据详情：王丽</span>
    </div>

    <div class="detail-wrap">
      <!-- ============ 左侧面板 ============ -->
      <div class="left-panel">
        <div class="prototype-card">
          <!-- 时间筛选 -->
          <div class="filter-line">
            <span class="cap">时间：</span>
            <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="起始日期" end-placeholder="结束日期" size="small" />
          </div>
          <!-- 数据状态筛选 -->
          <div class="filter-line">
            <span class="cap">数据状态：</span>
            <el-radio-group v-model="dataFilter" size="small">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="alert">异常</el-radio-button>
              <el-radio-button value="normal">正常</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="panel-cap">记录数据（点击列名或单元格查看趋势 / 打卡记录 / 自评量表）</div>

        <div class="prototype-card" style="padding: 0">
          <el-table :data="filteredRecords" class="prototype-table" height="calc(100vh - 320px)" @cell-click="onCellClick" @header-click="onHeaderClick">
            <el-table-column prop="date" label="时间" width="105" />
            <el-table-column prop="urine" label="排尿次数" width="110" :class-name="colClass('urine')">
              <template #default="{ row }">
                {{ row.urine }} 次
                <el-tag v-if="row.isAlert" size="small" class="tag-red">异常</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="stool" label="排便次数" width="95" :class-name="colClass('stool')">
              <template #default="{ row }">{{ row.stool }} 次</template>
            </el-table-column>
            <el-table-column prop="water" label="饮水量" width="95" :class-name="colClass('water')">
              <template #default="{ row }">{{ row.water }} ml</template>
            </el-table-column>
            <el-table-column prop="check" label="打卡记录" width="100" :class-name="colClass('check')">
              <template #default="{ row }">
                <el-tag size="small" :class="row.isDone ? 'tag-ok' : 'tag-gray'">{{ row.isDone ? '已完成' : '未完成' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scale" label="自评量表（最近填写）" min-width="140" :class-name="colClass('scale')">
              <template #default="{ row }">
                <span v-if="scaleByDate[row.date]" class="scale-chip" :style="{ color: scaleByDate[row.date].color }">{{ scaleByDate[row.date].name }}</span>
                <span v-else style="color: #9ca3af">—</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- ============ 右侧面板 ============ -->
      <div class="right-panel">
        <!-- 折线图视图（排尿/排便/饮水单指标 或 9 量表对比） -->
        <ChartCard v-if="activeCol !== 'check'" :title="chartTitle" :sub="chartSub" :tip="chartTip" :option="chartOption">
          <template #extra>
            <div v-if="activeCol === 'scale'" class="scale-legend">
              <span v-for="s in SCALES" :key="s.key"><i :style="{ background: s.color }"></i>{{ s.name }}</span>
            </div>
          </template>
        </ChartCard>

        <!-- 日历视图（打卡记录） -->
        <div v-else class="prototype-card cal-card">
          <div class="chart-title">打卡记录</div>
          <div class="cal-head">
            <span class="cal-title">{{ calYear }}年{{ calMonth + 1 }}月</span>
            <span class="cal-nav">
              <span @click="navMonth(-1)">‹</span>
              <span @click="navMonth(1)">›</span>
            </span>
          </div>
          <div class="calwk">
            <span v-for="w in ['一', '二', '三', '四', '五', '六', '日']" :key="w">{{ w }}</span>
          </div>
          <div class="cal">
            <div v-for="(d, i) in calCells" :key="i" class="d" :class="{ off: d.off, on: d.date === selDate }" @click="d.date && pickDay(d.date)">
              {{ d.day }}
              <i v-if="d.date && checkMap[d.date]" />
            </div>
          </div>
          <div class="day-list">
            <div class="cap">{{ selDate }} 打卡记录</div>
            <div v-for="(r, i) in checkMap[selDate] || []" :key="i" class="row">
              <div class="grow">
                <div>{{ r.time }} {{ r.title }}</div>
                <div class="s">{{ r.sub }}</div>
              </div>
              <el-tag size="small" :class="r.done ? 'tag-ok' : 'tag-gray'">{{ r.tag }}</el-tag>
            </div>
            <div v-if="!(checkMap[selDate] || []).length" class="empty">当日暂无打卡记录</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { EChartsOption } from 'echarts'
import ChartCard from '@/components/ChartCard.vue'

// 原型图：05 患者数据详情页面（网页后台）——记录表 + 点击列查看趋势/打卡日历/自评量表
const router = useRouter()

interface DayRecord {
  date: string
  urine: number
  stool: number
  water: number
  isAlert: boolean
  isDone: boolean
}

const COLUMNS: Record<string, { name: string; unit: string; color: string }> = {
  urine: { name: '排尿次数', unit: '次', color: '#1E9E8F' },
  stool: { name: '排便次数', unit: '次', color: '#F59E0B' },
  water: { name: '饮水量', unit: 'ml', color: '#3B82F6' },
}

const SCALES = [
  { key: 'pfdi20', name: 'PFDI-20', fullName: '盆底功能障碍简表', color: '#1E9E8F', max: 300 },
  { key: 'pisq12', name: 'PISQ-12', fullName: '盆腔脏器脱垂/尿失禁性功能问卷', color: '#F59E0B', max: 48 },
  { key: 'iiq7', name: 'IIQ-7', fullName: '尿失禁影响问卷简表', color: '#3B82F6', max: 21 },
  { key: 'pfmuscle', name: '盆底肌力', fullName: '盆底肌力自评量表', color: '#7C3AED', max: 100 },
  { key: 'fsfi', name: 'FSFI', fullName: '女性性功能指数', color: '#EC4899', max: 36 },
  { key: 'pacsym', name: 'PAC-SYM', fullName: '便秘患者症状自评量表', color: '#10B981', max: 60 },
  { key: 'iciq', name: 'ICI-Q-SF', fullName: '国际尿失禁咨询问卷简表', color: '#EF4444', max: 21 },
  { key: 'oabq', name: 'OAB-q', fullName: '膀胱过度活动症问卷', color: '#8B5CF6', max: 100 },
  { key: 'pfiq7', name: 'PFIQ-7', fullName: '盆底影响问卷简表', color: '#0EA5E9', max: 300 },
]

const TRAIN_POOL = [
  { title: '3s慢肌训练', sub: '语音视频 · 3分钟' },
  { title: '快肌训练', sub: '语音视频 · 3分钟' },
  { title: '盆底肌感知训练', sub: '动画视频 · 5分钟' },
  { title: '三维腹式呼吸训练', sub: '动画视频 · 4分钟' },
]
const TRAIN_TIMES = ['09:00', '15:00', '20:30']

// ============ 示例数据（原型占位：近 30 天记录 + 打卡明细 + 量表历史） ============
// TODO: 替换为接口数据（api/report.ts getPatientRecords）
function dateStr(offsetDays: number) {
  const d = new Date()
  d.setDate(d.getDate() + offsetDays)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const records: DayRecord[] = Array.from({ length: 30 }, (_, idx) => {
  const i = 29 - idx
  const seed = 30 - i
  const urine = 3 + Math.round(2 * Math.sin(seed / 3.2) + 2 * Math.random())
  return {
    date: dateStr(-i),
    urine,
    stool: Math.max(1, Math.round(1.5 * Math.sin(seed / 5) + Math.random() + 1)),
    water: 800 + Math.round(300 * Math.sin(seed / 4) + 250 * Math.random()),
    isAlert: urine >= 6,
    isDone: seed % 4 !== 0,
  }
})

const checkMap: Record<string, { time: string; title: string; sub: string; tag: string; done: boolean }[]> = {}
records.forEach((r) => {
  if (!r.isDone) return
  const seed = parseInt(r.date.slice(-2), 10)
  const count = 1 + (seed % 2)
  checkMap[r.date] = Array.from({ length: count }, (_, k) => {
    const t = TRAIN_POOL[(seed + k) % TRAIN_POOL.length]
    return { time: TRAIN_TIMES[k % TRAIN_TIMES.length], title: t.title, sub: t.sub, tag: '已完成', done: true }
  })
})

const scaleByDate: Record<string, (typeof SCALES)[number]> = {}
records.forEach((r, idx) => {
  if (idx % 2 === 0) scaleByDate[r.date] = SCALES[idx % SCALES.length]
})

const SHARED_DATES = Array.from({ length: 5 }, (_, k) => dateStr(-(4 - k) * 7))
const scaleHistory: Record<string, { date: string; score: number }[]> = {}
SCALES.forEach((s, si) => {
  scaleHistory[s.key] = SHARED_DATES.map((ds, k) => {
    const seed = si * 37 + k * 17 + s.key.length
    return { date: ds, score: Math.round(s.max * (0.3 + Math.abs(Math.sin(seed)) * 0.6)) }
  })
})

// ============ 筛选状态 ============
const dataFilter = ref<'all' | 'alert' | 'normal'>('all')
const activeCol = ref<'urine' | 'stool' | 'water' | 'check' | 'scale'>('urine')
const range = ref<[string, string]>([dateStr(-29), dateStr(0)])

const filteredRecords = computed(() => {
  const [start, end] = range.value
  return records.filter((r) => {
    const dateMatch = r.date >= start && r.date <= end
    const dataMatch = dataFilter.value === 'all' || (dataFilter.value === 'alert' ? r.isAlert : !r.isAlert)
    return dateMatch && dataMatch
  })
})

function colClass(key: string) {
  return activeCol.value === key ? 'active-col' : ''
}

function onCellClick(row: DayRecord, column: { property?: string }) {
  const key = column.property
  if (key && ['urine', 'stool', 'water', 'check', 'scale'].includes(key)) {
    activeCol.value = key as typeof activeCol.value
  }
}

function onHeaderClick(column: { property?: string }) {
  const key = column.property
  if (key && ['urine', 'stool', 'water', 'check', 'scale'].includes(key)) {
    activeCol.value = key as typeof activeCol.value
  }
}

// ============ 折线图（ECharts，悬停显示该时间点全部指标 / 全部量表） ============
const chartTitle = computed(() => {
  if (activeCol.value === 'scale') return '自评量表评分趋势'
  return COLUMNS[activeCol.value].name + '趋势'
})
const chartSub = computed(() => {
  if (activeCol.value === 'scale') return `共 ${SCALES.length} 个量表 · 每个量表近 ${SHARED_DATES.length} 次填写`
  return `共 ${filteredRecords.value.length} 条记录`
})
const chartTip = computed(() => {
  if (activeCol.value === 'scale') return ''
  const c = COLUMNS[activeCol.value]
  return `横轴：时间；纵轴：${c.name}（${c.unit}）。悬停可查看该时间点的全部指标数据。`
})

const chartOption = computed<EChartsOption>(() => {
  if (activeCol.value === 'scale') {
    return {
      tooltip: {
        trigger: 'axis',
        formatter: (params: unknown) => {
          const p = params as { axisValue: string; data: number; seriesName: string; dataIndex: number }[]
          const first = p[0]
          const idx = first.dataIndex
          const rows = p.map((item) => {
            const s = SCALES.find((x) => x.name === item.seriesName)!
            const score = scaleHistory[s.key][idx].score
            const pct = Math.round((score / s.max) * 100)
            return `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${s.color};margin-right:4px"></span>${s.name}：${score} / ${s.max}（${pct}%）`
          })
          return `<div>${first.axisValue}</div>${rows.join('<br/>')}`
        },
      },
      legend: { data: SCALES.map((s) => s.name), bottom: 0, textStyle: { fontSize: 10 } },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: { type: 'category', data: SHARED_DATES.map((d) => d.slice(5)) },
      yAxis: { type: 'value', name: '评分(占满分%)', min: 0, max: 100 },
      series: SCALES.map((s) => ({
        name: s.name,
        type: 'line',
        data: scaleHistory[s.key].map((p) => Math.round((p.score / s.max) * 100)),
        itemStyle: { color: s.color },
        lineStyle: { color: s.color, width: 1.8 },
      })),
    }
  }
  const col = COLUMNS[activeCol.value]
  const list = filteredRecords.value
  return {
    tooltip: {
      trigger: 'axis',
      formatter: (params: unknown) => {
        const p = params as { axisValue: string; data: number; dataIndex: number }[]
        const idx = p[0].dataIndex
        const r = list[idx]
        return `<div>${r.date}</div>排尿次数：${r.urine} 次<br/>排便次数：${r.stool} 次<br/>饮水量：${r.water} ml`
      },
    },
    grid: { left: 50, right: 40, top: 36, bottom: 40 },
    xAxis: { type: 'category', data: list.map((r) => r.date.slice(5)) },
    yAxis: { type: 'value', name: `${col.name}(${col.unit})` },
    series: [
      {
        name: col.name,
        type: 'line',
        smooth: true,
        data: list.map((r) => (r as unknown as Record<string, number>)[activeCol.value]),
        itemStyle: { color: col.color },
        lineStyle: { color: col.color, width: 2.5 },
      },
    ],
  }
})

// ============ 日历视图 ============
const now = new Date()
const calYear = ref(now.getFullYear())
const calMonth = ref(now.getMonth())
const selDate = ref(dateStr(0))

const calCells = computed(() => {
  const first = new Date(calYear.value, calMonth.value, 1)
  const startWeek = (first.getDay() + 6) % 7
  const daysInMon = new Date(calYear.value, calMonth.value + 1, 0).getDate()
  const prevMonDays = new Date(calYear.value, calMonth.value, 0).getDate()
  const cells: { day: number; off: boolean; date?: string }[] = []
  for (let i = startWeek - 1; i >= 0; i--) cells.push({ day: prevMonDays - i, off: true })
  for (let d = 1; d <= daysInMon; d++) {
    cells.push({ day: d, off: false, date: `${calYear.value}-${String(calMonth.value + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}` })
  }
  const total = startWeek + daysInMon
  const tail = (7 - (total % 7)) % 7
  for (let k = 1; k <= tail; k++) cells.push({ day: k, off: true })
  return cells
})

function navMonth(delta: number) {
  const d = new Date(calYear.value, calMonth.value + delta, 1)
  calYear.value = d.getFullYear()
  calMonth.value = d.getMonth()
}

function pickDay(date: string) {
  selDate.value = date
}
</script>

<style scoped>
.back-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.detail-title {
  font-size: 15px;
  font-weight: 600;
}
.detail-wrap {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
}
.left-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.right-panel {
  width: 520px;
  flex-shrink: 0;
}
.filter-line {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.filter-line:last-child {
  margin-bottom: 0;
}
.cap {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
}
.panel-cap {
  font-size: 12px;
  color: #6b7280;
  padding: 0 2px;
}
.scale-chip {
  font-weight: 600;
}
:deep(.active-col) {
  background: #eef7f6 !important;
}
.scale-legend {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 11px;
  color: #6b7280;
}
.scale-legend i {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 3px;
}
.cal-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
}
.cal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 6px 0;
}
.cal-title {
  font-size: 14px;
  font-weight: 600;
}
.cal-nav span {
  cursor: pointer;
  padding: 0 8px;
  color: #1e9e8f;
  font-size: 16px;
}
.calwk {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  font-size: 11px;
  color: #9ca3af;
  text-align: center;
  margin-bottom: 4px;
}
.cal {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.cal .d {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  position: relative;
}
.cal .d.off {
  background: #f3f4f6;
  color: #c0c4cc;
  cursor: default;
}
.cal .d.on {
  background: #1e9e8f;
  color: #fff;
  font-weight: 600;
}
.cal .d i {
  position: absolute;
  bottom: 3px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #f59e0b;
}
.day-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  overflow-y: auto;
}
.day-list .cap {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
}
.day-list .row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 6px 8px;
  font-size: 12px;
}
.day-list .s {
  color: #9ca3af;
  font-size: 11px;
  margin-top: 2px;
}
.day-list .empty {
  color: #9ca3af;
  font-size: 12px;
  text-align: center;
  padding: 16px 0;
}
</style>
