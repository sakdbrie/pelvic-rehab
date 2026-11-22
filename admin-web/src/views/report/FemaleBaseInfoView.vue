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

// ============ 示例数据（原型占位：60 位女性患者） ============
// TODO: 替换为接口数据（api/report.ts getReportData, reportType=female）
const surnames = ['王', '李', '张', '刘', '陈', '杨', '赵', '黄', '周', '吴', '徐', '孙', '马', '朱', '胡', '林', '郭', '何', '高', '罗']
const givenNames = ['丽', '梅', '伟', '芳', '娜', '敏', '静', '强', '磊', '洋', '艳', '勇', '军', '杰', '涛', '婷', '雪', '鹏', '宇', '鑫']
const exerciseArr = ['无规律运动', '每周跑步 3 次', '每周瑜伽 2 次', '每日快走 30 分钟']
const marriageArr = ['已婚', '未婚', '离异']
const educationArr = ['初中', '高中', '大学', '研究生']
const jobArr = ['全职类工作', '教师', '公务员', '自由职业', '服务业', '企业职员']
const stoolArr = ['正常', '便秘', '腹泻']
const urineArr = ['正常', '尿失禁', '尿频', '尿急']
const sleepArr = ['左侧', '右侧', '平躺']
const reactionArr = ['无', '轻微', '明显']
const assistArr = ['无', '会阴侧切', '产钳']
const deliveryArr = ['顺产', '剖宫产']
const yesNo = ['是', '否']
const painArr = ['无', '有']

function pad2(n: number) {
  return n < 10 ? '0' + n : String(n)
}
function ymd(y: number, m: number) {
  return `${y}-${pad2(m)}`
}

const allData: Record<string, string | number>[] = Array.from({ length: 60 }, (_, idx) => {
  const i = idx + 1
  const birthY = 1980 + (i % 18)
  const preW = 46 + (i % 16)
  const anteW = preW + 10 + (i % 8)
  const isMenopause = i % 10 === 0
  return {
    id: 'P' + String(i).padStart(3, '0'),
    name: surnames[i % surnames.length] + givenNames[(i * 3) % givenNames.length],
    gender: '女',
    birth: ymd(birthY, 1 + (i % 12)),
    age: 2026 - birthY,
    phone: '138' + String(1000 + i * 7).slice(-4) + '****',
    height: 152 + (i % 20) + ' cm',
    weight: 46 + (i % 24) + ' Kg',
    exercise: exerciseArr[i % exerciseArr.length],
    marriage: marriageArr[i % marriageArr.length],
    education: educationArr[i % educationArr.length],
    lifestyle: `抽烟 ${i % 5} 支/天，喝酒 ${(i * 10) % 100} ml/天，茶/咖啡 ${100 + ((i * 20) % 300)} ml/天，久坐 ${4 + (i % 6)} h/天，久站 ${1 + (i % 4)} h/天`,
    job: jobArr[i % jobArr.length],
    mPeriod: 3 + (i % 5) + ' 天',
    mCycle: 25 + (i % 8) + ' 天',
    dysmenorrhea: i % 3 === 0 ? '有' : '无',
    menopause: isMenopause ? '是' : '否',
    lastPeriod: ymd(2026, 1 + (i % 9)),
    menopauseDate: isMenopause ? ymd(2018 + (i % 6), 1 + (i % 12)) : '—',
    fsd: i % 4 === 0 ? '性交疼痛' : '—',
    stoolInfo: stoolArr[i % stoolArr.length],
    urineInfo: urineArr[i % urineArr.length],
    sexLife: `频率 ${i % 4} 次/周，无冶游史`,
    surgery: i % 5 === 0 ? `${ymd(2015 + (i % 8), 1 + (i % 12))} 腔镜手术` : '无',
    pastHistory: i % 6 === 0 ? '高血压' : '无特殊',
    deliveryDate: ymd(2012 + (i % 12), 1 + (i % 12)),
    pregCount: 1 + (i % 3) + ' 次',
    birthCount: 1 + (i % 2) + ' 次',
    abortionCount: (i % 3) + ' 次',
    deliveryMode: deliveryArr[i % 2],
    painless: yesNo[i % 2],
    cephalic: yesNo[(i + 1) % 2],
    assist: assistArr[i % assistArr.length],
    fetalWeight: (2.8 + (i % 10) / 10).toFixed(1) + ' Kg',
    preWeight: preW + ' Kg',
    anteWeight: anteW + ' Kg',
    gainWeight: anteW - preW + ' Kg',
    sleepPos: sleepArr[i % sleepArr.length],
    urineIncon: i % 3 === 0 ? '压力性' : '无',
    stoolPreg: i % 2 === 0 ? '正常' : '便秘',
    bp: '正常',
    glucose: '正常',
    thyroid: '—',
    backPain: painArr[i % 2],
    pubicPain: painArr[(i + 1) % 2],
    legPain: painArr[(i + 2) % 2],
    pregnancyReaction: reactionArr[i % reactionArr.length],
    itch: painArr[i % 2],
    diet: '正常',
    sexLifePreg: '无',
    chief: '—',
    painInfo: '—',
    abdTension: '正常',
    inspection: '正常',
    specialTest: '-',
    nerveTest: '正常',
    analPalpation: '正常',
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
  // TODO: 联调后端异步导出任务（架构文档：提交导出任务 → 轮询 → 下载签名 URL）
  void submitExportTask('female', {})
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
