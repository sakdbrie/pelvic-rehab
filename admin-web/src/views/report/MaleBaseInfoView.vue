<template>
  <!-- 原型图：07-2 男性基础信息表（网页后台）——35 列明细表 + 搜索 + 分页 + 导出 -->
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
      <el-table :data="pagedRows" class="prototype-table" height="calc(100vh - 250px)" style="width: 100%">
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

// 原型图：07-2 男性基础信息表（网页后台）
const router = useRouter()

// ============ 列定义（与原型表头一致，共 35 列） ============
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
  ['anorectal', '肛肠史', true],
  ['msd', '男性性功能障碍', true],
  ['sexLife', '性生活', true],
  ['stoolInfo', '大便情况', false],
  ['urineInfo', '小便情况', false],
  ['surgery', '手术史', true],
  ['pastHistory', '既往史', true],
  ['chief', '主诉', true],
  ['drSeparation', '腹直肌分离', false],
  ['postpartum', '产后情况', true],
  ['painInfo', '疼痛情况', true],
  ['inspection', '盆底视诊', true],
  ['specialTest', '特殊检查', true],
  ['nerveTest', '神经损伤查体', true],
  ['palpation', '触诊', true],
  ['ultrasound', '盆底超声（可上传照片）', false],
  ['xray', 'X-Ray检查（可上传照片）', false],
  ['urodynamics', '尿动力学检查（可上传照片）', false],
  ['otherExam', '其他检查（可上传照片）', false],
  ['diagnosis', '诊断', true],
  ['medication', '药物治疗', true],
  ['supplement', '其它补充（可上传照片）', true],
]

// ============ 示例数据（原型占位：40 位男性患者） ============
// TODO: 替换为接口数据（api/report.ts getReportData, reportType=male）
const surnames = ['王', '李', '张', '刘', '陈', '杨', '赵', '黄', '周', '吴', '徐', '孙', '马', '朱', '胡', '林', '郭', '何', '高', '罗']
const givenNames = ['伟', '强', '磊', '军', '勇', '杰', '涛', '鹏', '宇', '鑫', '刚', '明', '超', '华', '建', '峰', '波', '辉', '斌', '浩']
const exerciseArr = ['无规律运动', '每周跑步 3 次', '每周游泳 2 次', '每日快走 30 分钟']
const marriageArr = ['已婚', '未婚', '离异']
const educationArr = ['初中', '高中', '大学', '研究生']
const jobArr = ['全职类工作', '教师', '公务员', '自由职业', '服务业', '企业职员', '司机']
const stoolArr = ['正常', '便秘', '腹泻']
const urineArr = ['正常', '尿失禁', '尿频', '尿急', '排尿困难']
const anorectalArr = ['无特殊', '内痔', '外痔', '混合痔', '肛裂', '直肠脱垂']
const msdArr = ['—', '勃起功能障碍', '早泄', '射精障碍']

function pad2(n: number) {
  return n < 10 ? '0' + n : String(n)
}
function ymd(y: number, m: number) {
  return `${y}-${pad2(m)}`
}

const allData: Record<string, string | number>[] = Array.from({ length: 40 }, (_, idx) => {
  const i = idx + 1
  const birthY = 1972 + (i % 24)
  return {
    id: 'M' + String(i).padStart(3, '0'),
    name: surnames[i % surnames.length] + givenNames[(i * 3) % givenNames.length],
    gender: '男',
    birth: ymd(birthY, 1 + (i % 12)),
    age: 2026 - birthY,
    phone: '139' + String(2000 + i * 9).slice(-4) + '****',
    height: 165 + (i % 20) + ' cm',
    weight: 60 + (i % 28) + ' Kg',
    exercise: exerciseArr[i % exerciseArr.length],
    marriage: marriageArr[i % marriageArr.length],
    education: educationArr[i % educationArr.length],
    lifestyle: `抽烟 ${i % 8} 支/天，喝酒 ${(i * 15) % 200} ml/天，茶/咖啡 ${100 + ((i * 25) % 400)} ml/天，久坐 ${4 + (i % 6)} h/天，久站 ${1 + (i % 5)} h/天`,
    job: jobArr[i % jobArr.length],
    anorectal: i % 4 === 0 ? anorectalArr[i % anorectalArr.length] : '无特殊',
    msd: msdArr[i % msdArr.length],
    sexLife: `频率 ${i % 4} 次/周，无冶游史`,
    stoolInfo: stoolArr[i % stoolArr.length],
    urineInfo: urineArr[i % urineArr.length],
    surgery: i % 5 === 0 ? `${ymd(2014 + (i % 9), 1 + (i % 12))} 腹部手术` : '无',
    pastHistory: i % 6 === 0 ? '高血压' : '无特殊',
    chief: i % 5 === 0 ? '下腹坠胀 1 月余' : '—',
    drSeparation: i % 7 === 0 ? '有' : '无',
    postpartum: '—',
    painInfo: '—',
    inspection: '正常',
    specialTest: '-',
    nerveTest: '正常',
    palpation: '正常',
    ultrasound: '未上传',
    xray: '未上传',
    urodynamics: '未上传',
    otherExam: '未上传',
    diagnosis: '—',
    medication: '—',
    supplement: '—',
  }
})

const keyword = ref('')
const filterFn = (row: (typeof allData)[number]) => {
  const kw = keyword.value.trim().toLowerCase()
  return kw === '' || (row.id + ' ' + row.name).toLowerCase().includes(kw)
}
const { currentPage, total, pagedRows, resetPage } = useTablePage(() => allData, filterFn, 10)

function doSearch() {
  resetPage()
}

function onExport() {
  // TODO: 联调后端异步导出任务
  void submitExportTask('male', {})
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
