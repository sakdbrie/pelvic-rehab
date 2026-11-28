<template>
  <!-- 原型图：07-1 女性基础信息表（网页后台）——64 列明细表 + 搜索 + 分页 + 导出 -->
  <div class="page-main">
    <div class="top-row">
      <el-link type="primary" :underline="false" @click="router.push('/reports')">‹ 返回</el-link>
      <el-button type="primary" size="small" @click="onExport">导出 Excel</el-button>
    </div>

    <div class="prototype-card filter-bar">
      <span class="cap">患者列表</span>
      <div class="search-area">
        <el-input v-model="keyword" placeholder="搜索姓名 / 患者编号" @keyup.enter="doSearch" />
        <el-button type="primary" size="small" @click="doSearch">搜 索</el-button>
      </div>
    </div>

    <div class="prototype-card" style="padding: 0">
      <el-table v-loading="loading" :data="rows" class="prototype-table" height="calc(100vh - 250px)" style="width: 100%">
        <el-table-column
          v-for="c in COLS"
          :key="c[0]"
          :prop="c[0]"
          :label="c[1]"
          :min-width="c[2] ? 180 : 100"
          show-overflow-tooltip
        >
          <template #default="{ row }">{{ row[c[0]] ?? '—' }}</template>
        </el-table-column>
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

// 原型图：07-1 女性基础信息表（网页后台）
const router = useRouter()

// ============ 列定义（与原型表头一致，共 64 列；第三个值 true = 长文本列换行展示） ============
const COLS: [string, string, boolean][] = [
  ['id', '患者编号', false],
  ['name', '姓名', false],
  ['gender', '性别', false],
  ['birth', '出生年月', false],
  ['age', '年龄', false],
  ['phone', '联系电话', false],
  ['height', '身高(cm)', false],
  ['weight', '体重(kg)', false],
  ['exercise', '既往运动情况', true],
  ['marriage', '婚姻状况', false],
  ['education', '教育程度', false],
  ['lifestyle', '生活习惯', true],
  ['job', '从事职业', false],
  ['mPeriod', '月经持续时间', false],
  ['mCycle', '月经周期', false],
  ['dysmenorrhea', '痛经', false],
  ['menopause', '已绝经', false],
  ['lastPeriod', '末次月经', false],
  ['menopauseDate', '绝经时间', false],
  ['fsd', '女性性功能障碍（ICD-11分类）', true],
  ['stoolInfo', '大便情况', false],
  ['urineInfo', '小便情况', false],
  ['sexLife', '性生活', true],
  ['surgery', '手术史', true],
  ['pastHistory', '既往史', true],
  ['deliveryDate', '分娩日期', false],
  ['pregCount', '怀孕次数', false],
  ['birthCount', '分娩次数', false],
  ['abortionCount', '流产次数', false],
  ['deliveryMode', '分娩方式', false],
  ['painless', '无痛分娩', false],
  ['cephalic', '头位分娩', false],
  ['assist', '助产方式', false],
  ['fetalWeight', '胎儿出生体重', false],
  ['preWeight', '孕前体重', false],
  ['anteWeight', '产前体重', false],
  ['gainWeight', '孕期增重', false],
  ['sleepPos', '睡觉体位', false],
  ['urineIncon', '尿失禁', false],
  ['stoolPreg', '大便', false],
  ['bp', '血压', false],
  ['glucose', '血糖', false],
  ['thyroid', '甲状腺功能', false],
  ['backPain', '腰背痛', false],
  ['pubicPain', '耻骨疼痛', false],
  ['legPain', '下肢疼痛', false],
  ['pregnancyReaction', '妊娠反应', false],
  ['itch', '会阴瘙痒', false],
  ['diet', '饮食情况', false],
  ['sexLifePreg', '性生活', true],
  ['chief', '主诉', true],
  ['painInfo', '疼痛情况', true],
  ['abdTension', '腹部张力', false],
  ['inspection', '视诊', true],
  ['specialTest', '特殊检查', true],
  ['nerveTest', '神经损伤查体', true],
  ['analPalpation', '肛门触诊', true],
  ['ultrasound', '盆底超声（可上传照片）', false],
  ['xray', 'X-Ray检查（可上传照片）', false],
  ['urodynamics', '尿动力学检查（可上传照片）', false],
  ['otherExam', '其他检查（可上传照片）', false],
  ['diagnosis', '诊断', true],
  ['medication', '药物治疗', true],
  ['supplement', '其它补充（可上传照片）', true],
]

// ============ 接口数据（api/report.ts getReportData, reportType=female） ============
const keyword = ref('')
const rows = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)
const currentPage = ref(1)

function buildParams() {
  return {
    keyword: keyword.value.trim() || undefined,
    page: currentPage.value,
    size: 10,
  }
}

async function fetch() {
  loading.value = true
  try {
    const res = await getReportData('female', buildParams())
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
    await exportReport('female', buildParams())
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
