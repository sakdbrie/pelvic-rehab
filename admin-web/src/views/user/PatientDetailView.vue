<template>
  <!-- 原型图：02 用户信息详情页面（网页后台-患者） -->
  <div class="page-main">
    <!-- 返回 -->
    <div class="back-row">
      <el-link type="primary" :underline="false" @click="router.push('/user')">‹ 返回</el-link>
    </div>

    <!-- ===================== 基本信息 ===================== -->
    <div class="prototype-card">
      <div class="h1">基本信息</div>
      <div class="ro-grid">
        <span v-for="it in basicInfo" :key="it.k" class="ro-item" :style="it.full ? 'grid-column:1 / -1' : ''">
          <span class="h2">{{ it.k }}</span><span class="h3">{{ it.v }}</span>
        </span>
      </div>
    </div>

    <!-- ===================== 病史 ===================== -->
    <div class="prototype-card">
      <div class="h1">病史</div>
      <div class="ro-grid-2">
        <span v-for="it in historyInfo" :key="it.k" class="ro-item" :style="it.full ? 'grid-column:1 / -1' : ''">
          <span class="h2">{{ it.k }}</span>
          <span class="h3" :class="{ empty: it.v === '—' }">{{ it.v }}</span>
        </span>
      </div>
    </div>

    <!-- ===================== 孕产情况 ===================== -->
    <div class="prototype-card">
      <div class="h1">孕产情况</div>
      <div class="ro-grid-2">
        <span v-for="it in pregnancyInfo" :key="it.k" class="ro-item" :style="it.full ? 'grid-column:1 / -1' : ''">
          <span class="h2">{{ it.k }}</span><span class="h3">{{ it.v }}</span>
        </span>
      </div>
    </div>

    <!-- ===================== 医生补充 · 临床检查（医生端） ===================== -->
    <div class="prototype-card supplement-card">
      <div class="supplement-head">
        <div class="supplement-title">临床检查（医生端）</div>
        <el-button v-if="!editing" type="primary" size="small" @click="editing = true">补 充</el-button>
        <el-button v-else size="small" @click="editing = false">取 消</el-button>
      </div>

      <!-- 只读视图 -->
      <div v-if="!editing" class="read-view">
        <div v-for="s in readSections" :key="s.title" class="read-section">
          <div class="h2">{{ s.title }}</div>
          <div v-if="s.value" class="h3" :class="{ empty: s.value === '—' }" style="margin-top: 4px">{{ s.value }}</div>
          <div v-else class="ro-grid" style="margin-top: 6px">
            <span v-for="(item, i) in s.items" :key="i" class="h3">{{ item }}</span>
          </div>
        </div>
      </div>

      <!-- 编辑视图（点击补充后显示） -->
      <div v-else class="edit-view">
        <!-- 1. 主诉 -->
        <div class="fld">
          <label class="section-label">主诉</label>
          <el-input v-model="form.chief" placeholder="请输入主诉" />
        </div>

        <!-- 2. 腹直肌分离 -->
        <div class="fld">
          <label class="section-label">腹直肌分离</label>
          <el-select v-model="form.drSeparation" placeholder="请选择" style="width: 140px">
            <el-option label="无" value="无" />
            <el-option label="有" value="有" />
          </el-select>
          <div v-if="form.drSeparation === '有'" class="num-grid">
            <div v-for="f in drFields" :key="f" class="fld inline">
              <label>{{ f }}</label>
              <el-input v-model="form.drValues[f]" placeholder="0"><template #append>指</template></el-input>
            </div>
          </div>
        </div>

        <!-- 3. 产后情况 -->
        <div class="fld">
          <label class="section-label">产后情况</label>
          <div class="grid-2">
            <div class="fld">
              <label>小便</label>
              <el-select v-model="form.postpartum.urine" style="width: 100%">
                <el-option v-for="o in ['无', '压力', '急迫', '充溢', '混合']" :key="o" :label="o" :value="o" />
              </el-select>
            </div>
            <div class="fld">
              <label>大便</label>
              <el-select v-model="form.postpartum.stool" style="width: 100%">
                <el-option v-for="o in ['正常', '失禁', '便秘']" :key="o" :label="o" :value="o" />
              </el-select>
            </div>
            <div class="fld">
              <label>盆腔、会阴痛</label>
              <div class="row">
                <el-select v-model="form.postpartum.pelvicPain" style="flex: 1">
                  <el-option label="无" value="无" />
                  <el-option label="有" value="有" />
                </el-select>
                <template v-if="form.postpartum.pelvicPain === '有'">
                  <span class="unit">VAS</span>
                  <el-select v-model="form.postpartum.pelvicVas" style="width: 90px">
                    <el-option v-for="n in 11" :key="n" :label="String(n - 1)" :value="String(n - 1)" />
                  </el-select>
                  <span class="unit">/10</span>
                </template>
              </div>
            </div>
            <div class="fld">
              <label>脏器脱垂</label>
              <el-select v-model="form.postpartum.prolapse" style="width: 100%">
                <el-option v-for="o in ['无', '膀胱', '子宫', '直肠']" :key="o" :label="o" :value="o" />
              </el-select>
            </div>
            <div class="fld">
              <label>瘢痕</label>
              <el-select v-model="form.postpartum.scar" style="width: 100%">
                <el-option label="无" value="无" />
                <el-option label="有" value="有" />
              </el-select>
              <div v-if="form.postpartum.scar === '有'" class="grid-2" style="margin-top: 8px">
                <div class="fld"><label>质地</label><el-input v-model="form.postpartum.scarTexture" placeholder="质地" /></div>
                <div class="fld"><label>颜色</label><el-input v-model="form.postpartum.scarColor" placeholder="颜色" /></div>
                <div class="fld"><label>长度</label><el-input v-model="form.postpartum.scarLength" placeholder="长度" /></div>
                <div class="fld">
                  <label>时间</label>
                  <el-date-picker v-model="form.postpartum.scarDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                </div>
              </div>
            </div>
            <div class="fld">
              <label>尿道不稳定性</label>
              <el-input v-model="form.postpartum.urethra" placeholder="请输入" />
            </div>
            <div class="fld">
              <label>其他</label>
              <el-input v-model="form.postpartum.other" placeholder="请输入" />
            </div>
          </div>
        </div>

        <!-- 4. 疼痛情况（VAS 全部为下拉 0-10） -->
        <div class="fld">
          <label class="section-label">疼痛情况</label>
          <div class="vas-grid">
            <div v-for="p in vasFields" :key="p.key" class="fld">
              <label>{{ p.label }}</label>
              <div class="row">
                <template v-for="side in p.sides" :key="side">
                  <span class="unit">{{ side }}</span>
                  <el-select v-model="form.vas[p.key + side]" size="small" style="flex: 1; min-width: 0">
                    <el-option v-for="n in 11" :key="n" :label="String(n - 1)" :value="String(n - 1)" />
                  </el-select>
                </template>
              </div>
            </div>
          </div>
        </div>

        <!-- 5. 盆底视诊 -->
        <div class="fld">
          <label class="section-label">盆底视诊</label>
          <div class="grid-2">
            <div v-for="f in inspectionFields" :key="f.key" class="fld">
              <label>{{ f.label }}</label>
              <el-select v-model="form.inspection[f.key]" style="width: 100%">
                <el-option v-for="o in f.options" :key="o" :label="o" :value="o" />
              </el-select>
              <template v-if="f.key === 'discharge' && form.inspection.discharge === '有'">
                <div class="grid-2" style="margin-top: 8px">
                  <div class="fld"><label>颜色</label><el-input v-model="form.inspection.dischargeColor" placeholder="颜色" /></div>
                  <div class="fld"><label>气味</label><el-input v-model="form.inspection.dischargeSmell" placeholder="气味" /></div>
                  <div class="fld"><label>质地</label><el-input v-model="form.inspection.dischargeTexture" placeholder="质地" /></div>
                  <div class="fld"><label>量</label><el-input v-model="form.inspection.dischargeAmount" placeholder="量" /></div>
                </div>
              </template>
            </div>
            <div class="fld">
              <label>其他</label>
              <el-input v-model="form.inspection.other" placeholder="请输入" />
            </div>
          </div>
        </div>

        <!-- 6. 特殊检查（每题 有(+)/无(-) 单选） -->
        <div class="fld">
          <label class="section-label">特殊检查</label>
          <div class="grid-2">
            <div v-for="f in specialTestFields" :key="f.key" class="fld">
              <label>{{ f.label }}</label>
              <el-select v-model="form.specialTest[f.key]" style="width: 100%">
                <el-option label="-" value="-" />
                <el-option label="+" value="+" />
              </el-select>
            </div>
            <div class="fld">
              <label>其他</label>
              <el-input v-model="form.specialTest.other" placeholder="请输入" />
            </div>
          </div>
        </div>

        <!-- 7. 神经损伤查体 -->
        <div class="fld">
          <label class="section-label">神经损伤查体</label>
          <div class="grid-2">
            <div class="fld">
              <label>盆底张力</label>
              <el-select v-model="form.nerve.pfTone" style="width: 100%">
                <el-option v-for="o in ['正常', '增高', '降低']" :key="o" :label="o" :value="o" />
              </el-select>
            </div>
            <div v-for="side in ['左', '右']" :key="side" class="fld">
              <label>感觉-{{ side }}</label>
              <el-select v-model="form.nerve['sensation' + side]" style="width: 100%">
                <el-option v-for="o in ['正常', '减弱', '过敏']" :key="o" :label="o" :value="o" />
              </el-select>
            </div>
            <div class="fld">
              <label>导尿管</label>
              <el-select v-model="form.nerve.catheter" style="width: 100%">
                <el-option label="无" value="无" />
                <el-option label="有" value="有" />
              </el-select>
            </div>
          </div>
        </div>

        <!-- 8. 触诊（每题单选 · 下拉） -->
        <div class="fld">
          <label class="section-label">触诊</label>
          <div class="grid-2">
            <div v-for="f in palpationFields" :key="f.key" class="fld">
              <label>{{ f.label }}</label>
              <el-select v-model="form.palpation[f.key]" style="width: 100%">
                <el-option v-for="o in f.options" :key="o" :label="o" :value="o" />
              </el-select>
            </div>
          </div>
          <div class="fld" style="margin-top: 10px">
            <label>牛津肌力</label>
            <el-input v-model="form.palpation.oxford" placeholder="0" style="max-width: 180px"><template #append>级</template></el-input>
          </div>
        </div>

        <!-- 9. 检查资料上传 -->
        <div class="grid-2">
          <div v-for="u in uploadItems" :key="u" class="fld">
            <label class="section-label">{{ u }}（可上传照片）</label>
            <!-- TODO: 联调后端文件上传（COS/MinIO），此处为上传占位 -->
            <el-upload drag action="#" :auto-upload="false" style="height: 80px">
              <el-icon><Plus /></el-icon>
              <div class="upload-text">+ 上传</div>
            </el-upload>
          </div>
        </div>

        <!-- 10. 诊断 -->
        <div class="fld">
          <label class="section-label">诊断</label>
          <el-input v-model="form.diagnosis" placeholder="请输入诊断" />
        </div>

        <!-- 11. 药物治疗（多选 · 列表视图） -->
        <div class="fld">
          <label class="section-label">药物治疗</label>
          <el-checkbox-group v-model="form.medication">
            <el-checkbox v-for="m in medicationOptions" :key="m" :label="m" />
          </el-checkbox-group>
          <el-checkbox v-model="form.medOtherChecked" style="margin-top: 4px">其他</el-checkbox>
          <el-input v-if="form.medOtherChecked" v-model="form.medOther" placeholder="请输入其他药物" style="margin-top: 8px" />
        </div>

        <!-- 12. 其它补充 -->
        <div class="fld">
          <label class="section-label">其它补充（可上传照片）</label>
          <el-input v-model="form.supplement" type="textarea" :rows="3" placeholder="外院检查资料、中期或后期评估资料、患者特殊情况记录" />
          <div class="row" style="margin-top: 8px">
            <!-- TODO: 联调后端文件上传，此处为上传占位 -->
            <el-upload v-for="t in ['图片', 'docx', 'pdf', 'excel']" :key="t" drag action="#" :auto-upload="false" style="width: 110px; height: 80px">
              <div class="upload-text">+ 上传 {{ t }}</div>
            </el-upload>
          </div>
        </div>

        <el-button type="primary" size="small" style="align-self: flex-start" @click="onSave">保存补充</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getPatientDetail, saveSupplement, type PatientDetail } from '@/api/system'

// 原型图：02 用户信息详情页面（网页后台-患者）——只读档案 + 医生"补充"临床检查
const route = useRoute()
const router = useRouter()

interface InfoItem {
  k: string
  v: string
  full?: boolean
}

interface ReadSection {
  title: string
  value?: string
  items?: string[]
}

type Pregnancy = PatientDetail['pregnancies'][number]
type LatestExam = NonNullable<PatientDetail['latestExam']>

/** 字符串/数字空值统一显示为 '—' */
function dash(v: string | number | null | undefined): string {
  return v == null || v === '' ? '—' : String(v)
}

/** 数值格式化：整数不带小数点，小数最多保留两位 */
function num(v: number | null | undefined): string {
  if (v == null) return '—'
  return Number.isInteger(v) ? String(v) : String(Number(v.toFixed(2)))
}

/** 布尔值展示，缺省为 '—' */
function yesNo(v: boolean | null | undefined, yes = '有', no = '无'): string {
  return v == null ? '—' : v ? yes : no
}

/** 特殊检查 '+'/'-' 直取，缺省为 '-' */
function plusMinus(v: string | null | undefined): string {
  return v ? v : '-'
}

const detail = ref<PatientDetail | null>(null)

/** 最近一次分娩（deliveryDate 最大的记录） */
const latestDelivery = computed<Pregnancy | null>(() => {
  const delivered = (detail.value?.pregnancies ?? []).filter((p) => !!p.deliveryDate)
  if (!delivered.length) return null
  return delivered.reduce((a, b) => ((a.deliveryDate as string) >= (b.deliveryDate as string) ? a : b))
})

/** 最近一次妊娠记录（pregnancyNo 最大的记录） */
const latestPregnancy = computed<Pregnancy | null>(() => {
  const list = detail.value?.pregnancies ?? []
  if (!list.length) return null
  return list.reduce((a, b) => (a.pregnancyNo >= b.pregnancyNo ? a : b))
})

const basicInfo = computed<InfoItem[]>(() => {
  const d = detail.value
  const hp = d?.healthProfile
  const habits = [
    hp?.smokingPerDay != null ? `抽烟 ${num(hp.smokingPerDay)} 支/天` : '',
    hp?.alcoholMlPerDay != null ? `喝酒 ${num(hp.alcoholMlPerDay)} ml/天` : '',
    hp?.teaCoffeeMlPerDay != null ? `茶/咖啡 ${num(hp.teaCoffeeMlPerDay)} ml/天` : '',
    hp?.sedentaryHoursPerDay != null ? `久坐 ${num(hp.sedentaryHoursPerDay)} h/天` : '',
    hp?.standingHoursPerDay != null ? `久站 ${num(hp.standingHoursPerDay)} h/天` : '',
  ]
    .filter(Boolean)
    .join('，')

  return [
    { k: '姓名', v: dash(d?.name) },
    { k: '性别', v: dash(d?.gender) },
    { k: '出生年月', v: d?.birthDate ? d.birthDate.slice(0, 7) : '—' },
    { k: '年龄', v: num(d?.age) },
    { k: '联系电话', v: dash(d?.phone) },
    { k: '身高', v: d?.heightCm != null ? `${num(d.heightCm)}cm` : '—' },
    { k: '体重', v: d?.weightKg != null ? `${num(d.weightKg)}Kg` : '—' },
    { k: '从事职业', v: dash(d?.job) },
    { k: '婚姻状况', v: dash(d?.marriage) },
    { k: '教育程度', v: dash(d?.education) },
    { k: '既往运动情况', v: dash(hp?.pastExerciseLifeSupplement), full: true },
    { k: '生活习惯', v: habits || '—', full: true },
  ]
})

const historyInfo = computed<InfoItem[]>(() => {
  const d = detail.value
  const hp = d?.healthProfile

  let menstruation = '—'
  if (hp && hp.menopause != null) {
    menstruation = hp.menopause
      ? `已绝经；绝经年龄 ${num(hp.menopauseAge)}`
      : `未绝经；末次月经：${dash(hp.lastMenstrualDate)}，持续时间 ${num(hp.menstruationDays)} 天，周期 ${num(hp.cycleDays)} 天，痛经VAS ${num(hp.dysmenorrheaVas)}`
  }

  const sexLife =
    hp?.sexFrequencyWeekly != null
      ? `频率 ${num(hp.sexFrequencyWeekly)} 次/周，冶游史：${yesNo(hp.promiscuityHistory)}`
      : '—'

  const surgeries = (d?.surgeries ?? [])
    .map((s) => `${s.surgeryDate ? s.surgeryDate + ' ' : ''}${s.surgeryName}`)
    .join('；')

  const conditions = (d?.conditions ?? []).map((c) => `${c.category}：${c.itemName}`).join('；')

  return [
    { k: '月经史', v: menstruation, full: true },
    { k: '女性性功能障碍（ICD-11分类）', v: '—' },
    { k: '大便情况', v: dash(hp?.stool) },
    { k: '小便情况', v: dash(hp?.urine) },
    { k: '性生活', v: sexLife },
    { k: '手术史', v: surgeries || '—' },
    { k: '既往史', v: conditions || '—' },
  ]
})

const pregnancyInfo = computed<InfoItem[]>(() => {
  const list = detail.value?.pregnancies ?? []
  if (!list.length) {
    return [
      { k: '产科史', v: '—', full: true },
      { k: '分娩方式', v: '—' },
      { k: '孕期情况', v: '—', full: true },
    ]
  }

  const total = list.length
  const delivered = list.filter((p) => !!p.deliveryDate).length
  const lastDelivery = latestDelivery.value
  const obstetrics = `怀孕 ${total} 次，分娩 ${delivered} 次，流产 ${total - delivered} 次；最近分娩日期：${dash(lastDelivery?.deliveryDate)}`

  const delivery = lastDelivery
    ? `${dash(lastDelivery.deliveryMethod)}；无痛分娩：${yesNo(lastDelivery.painlessDelivery, '是', '否')}；头位正常分娩：${yesNo(lastDelivery.cephalicDelivery, '是', '否')}；助产方式：${dash(lastDelivery.deliveryAssist)}；胎儿出生体重：${num(lastDelivery.babyBirthWeightKg)} Kg`
    : '—'

  const p = latestPregnancy.value
  const pregnancy = p
    ? `孕前 ${num(p.preWeightKg)} kg · 产前 ${num(p.prenatalWeightKg)} kg · 增重 ${num(p.weightGainKg)} Kg；睡觉体位：${dash(p.sleepPosition)}；孕期尿失禁：${yesNo(p.incontinence)}；孕期大便：${dash(p.stool)}；血压：${dash(p.bloodPressure)}；血糖：${dash(p.bloodSugar)}；甲状腺功能：${dash(p.thyroidFunction)}；腰背痛VAS ${num(p.backPainVas)}，耻骨疼痛VAS ${num(p.pubicPainVas)}，下肢疼痛VAS ${num(p.lowerLimbPainVas)}；妊娠反应：${dash(p.reactionLevel)}；会阴瘙痒：${yesNo(p.perinealItching)}；饮食情况：${dash(p.diet)}；孕期性生活：${yesNo(p.sexLife)}`
    : '—'

  return [
    { k: '产科史', v: obstetrics, full: true },
    { k: '分娩方式', v: delivery },
    { k: '孕期情况', v: pregnancy, full: true },
  ]
})

/** latestExam 为空时的默认只读结构：标题保持不变，值全为 '—' */
function emptyReadSections(): ReadSection[] {
  return [
    { title: '主诉', value: '—' },
    { title: '腹直肌分离', value: '—' },
    { title: '产后情况', items: ['小便：—', '大便：—', '盆腔、会阴痛：—', '脏器脱垂：—', '瘢痕：—', '尿道不稳定性：—', '其他：—'] },
    { title: '疼痛情况', items: ['腰背痛（左/右/中）：— / — / —', '骶髂关节痛（左/右/中）：— / — / —', '耻骨疼痛：—', '尾骨痛（左/右/中）：— / — / —', '坐骨神经痛（左/右）：— / —', '腹股沟疼痛（左/右）：— / —', '颈痛（左/右）：— / —', '上肢痛（左/右）：— / —', '下肢痛（左/右）：— / —'] },
    { title: '盆底视诊', items: ['外阴：—', '阴道裂口：—', '尿道口：—', '阴蒂：—', '阴唇对称性：—', '分泌物情况：—', '阴毛：—', '痔疮：—', 'VALSALVA动作：—'] },
    { title: '特殊检查', items: ['泌尿生殖测试：—', '疤痕试验：—', '4字试验-左：—', '4字试验-右：—', '骨盆分离试验：—', '骨盆挤压试验：—', '骶骨下压试验：—', '股骨冲击试验：—', '直腿抬高试验：—', '直腿抬高加强试验：—', '广泛性触诊试验：—', '其他：—'] },
    { title: '神经损伤查体', items: ['盆底张力：—', '感觉-左：—', '感觉-右：—', '导尿管：—'] },
    { title: '触诊', items: ['肛提肌：—', '坐尾肌：—', '髂尾肌：—', '闭孔内肌：—', '脏器偏移：—', '牛津肌力：— 级'] },
    { title: '盆底超声（可上传照片）', value: '—' },
    { title: 'X-Ray检查（可上传照片）', value: '—' },
    { title: '尿动力学检查（可上传照片）', value: '—' },
    { title: '其他检查（可上传照片）', value: '—' },
    { title: '诊断', value: '—' },
    { title: '药物治疗', value: '—' },
    { title: '其它补充（可上传照片）', value: '—' },
  ]
}

/** notes 中是否有包含关键词且标记“已上传”的记录 */
function uploadedStatus(notes: LatestExam['notes'], keyword: string): string {
  return notes.some((n) => n.content.includes(keyword) && n.content.includes('已上传')) ? '已上传' : '未上传'
}

const readSections = computed<ReadSection[]>(() => {
  const exam = detail.value?.latestExam
  if (!exam) return emptyReadSections()

  const ex = exam.examination
  const dia = exam.diastasis
  const pp = exam.postpartum
  const pain = exam.painAssessment
  const insp = exam.pelvicInspection
  const sp = exam.specialExam
  const neuro = exam.neurologicalExam
  const palp = exam.analPalpation

  return [
    { title: '主诉', value: dash(ex?.chief) },
    {
      title: '腹直肌分离',
      value: dia
        ? `有：静息（肚脐 ${num(dia.restingUmbilicus)} / 脐上 ${num(dia.restingAbove)} / 脐下 ${num(dia.restingBelow)}），收缩（肚脐 ${num(dia.contractionUmbilicus)} / 脐上 ${num(dia.contractionAbove)} / 脐下 ${num(dia.contractionBelow)}）`
        : '无',
    },
    {
      title: '产后情况',
      items: [
        `小便：${dash(pp?.urine)}`,
        `大便：${dash(pp?.stool)}`,
        `盆腔、会阴痛：${num(pp?.pelvicPerinealPainVas ?? 0)}`,
        `脏器脱垂：${dash(pp?.organProlapse)}`,
        `瘢痕：${
          pp?.scar
            ? `有（质地 ${dash(pp.scarTexture)}、颜色 ${dash(pp.scarColor)}、时间 ${dash(pp.scarDate)}、长度 ${num(pp.scarLengthCm)}cm）`
            : '无'
        }`,
        `尿道不稳定性：${yesNo(pp?.urethralInstability)}`,
        `其他：${dash(pp?.other)}`,
      ],
    },
    {
      title: '疼痛情况',
      items: [
        `腰背痛（左/右/中）：${num(pain?.backPainLeftVas ?? 0)} / ${num(pain?.backPainRightVas ?? 0)} / ${num(pain?.backPainCenterVas ?? 0)}`,
        `骶髂关节痛（左/右/中）：${num(pain?.siJointPainLeftVas ?? 0)} / ${num(pain?.siJointPainRightVas ?? 0)} / ${num(pain?.siJointPainCenterVas ?? 0)}`,
        `耻骨疼痛：${num(pain?.pubicPainVas ?? 0)}`,
        `尾骨痛（左/右/中）：${num(pain?.coccyxPainLeftVas ?? 0)} / ${num(pain?.coccyxPainRightVas ?? 0)} / ${num(pain?.coccyxPainCenterVas ?? 0)}`,
        `坐骨神经痛（左/右）：${num(pain?.sciaticPainLeftVas ?? 0)} / ${num(pain?.sciaticPainRightVas ?? 0)}`,
        `腹股沟疼痛（左/右）：${num(pain?.groinPainLeftVas ?? 0)} / ${num(pain?.groinPainRightVas ?? 0)}`,
        `颈痛（左/右）：${num(pain?.neckPainLeftVas ?? 0)} / ${num(pain?.neckPainRightVas ?? 0)}`,
        `上肢痛（左/右）：${num(pain?.armPainLeftVas ?? 0)} / ${num(pain?.armPainRightVas ?? 0)}`,
        `下肢痛（左/右）：${num(pain?.legPainLeftVas ?? 0)} / ${num(pain?.legPainRightVas ?? 0)}`,
      ],
    },
    {
      title: '盆底视诊',
      items: [
        `外阴：${dash(insp?.vulva)}`,
        `阴道裂口：${dash(insp?.vaginalTear)}`,
        `尿道口：${dash(insp?.urethralMeatus)}`,
        `阴蒂：${dash(insp?.clitoris)}`,
        `阴唇对称性：${yesNo(insp?.labiaSymmetry, '是', '否')}`,
        `分泌物情况：${
          insp?.discharge
            ? `有（颜色 ${dash(insp.dischargeColor)}、气味 ${dash(insp.dischargeSmell)}、质地 ${dash(insp.dischargeTexture)}、量 ${dash(insp.dischargeAmount)}）`
            : '无'
        }`,
        `阴毛：${dash(insp?.pubicHair)}`,
        `痔疮：${dash(insp?.hemorrhoids)}`,
        `VALSALVA动作：${dash(insp?.valsalva)}`,
      ],
    },
    {
      title: '特殊检查',
      items: [
        `泌尿生殖测试：${plusMinus(sp?.urogenitalTest)}`,
        `疤痕试验：${plusMinus(sp?.scarTest)}`,
        `4字试验-左：${plusMinus(sp?.faberLeft)}`,
        `4字试验-右：${plusMinus(sp?.faberRight)}`,
        `骨盆分离试验：${plusMinus(sp?.pelvicSeparation)}`,
        `骨盆挤压试验：${plusMinus(sp?.pelvicCompression)}`,
        `骶骨下压试验：${plusMinus(sp?.sacralCompression)}`,
        `股骨冲击试验：${plusMinus(sp?.femoralImpaction)}`,
        `直腿抬高试验：${plusMinus(sp?.straightLegRaise)}`,
        `直腿抬高加强试验：${plusMinus(sp?.straightLegRaiseReinforced)}`,
        `广泛性触诊试验：${plusMinus(sp?.palpationTest)}`,
        `其他：${dash(sp?.other)}`,
      ],
    },
    {
      title: '神经损伤查体',
      items: [
        `盆底张力：${dash(neuro?.pelvicFloorTone)}`,
        `感觉-左：${dash(neuro?.leftSensation)}`,
        `感觉-右：${dash(neuro?.rightSensation)}`,
        `导尿管：${yesNo(neuro?.catheter)}`,
      ],
    },
    {
      title: '触诊',
      items: [
        `肛提肌：${dash(palp?.levatorAni)}`,
        `坐尾肌：${dash(palp?.coccygeus)}`,
        `髂尾肌：${dash(palp?.iliococcygeus)}`,
        `闭孔内肌：${dash(palp?.obturatorInternus)}`,
        `脏器偏移：${dash(palp?.organShift)}`,
        `牛津肌力：${palp?.oxfordGrade != null ? `${num(palp.oxfordGrade)} 级` : '— 级'}`,
      ],
    },
    { title: '盆底超声（可上传照片）', value: uploadedStatus(exam.notes, '盆底超声') },
    { title: 'X-Ray检查（可上传照片）', value: uploadedStatus(exam.notes, 'X-Ray') },
    { title: '尿动力学检查（可上传照片）', value: uploadedStatus(exam.notes, '尿动力学') },
    { title: '其他检查（可上传照片）', value: uploadedStatus(exam.notes, '其他检查') },
    { title: '诊断', value: dash(ex?.diagnosis) },
    { title: '药物治疗', value: dash(ex?.medication) },
    { title: '其它补充（可上传照片）', value: dash(ex?.otherSupplement) },
  ]
})

onMounted(async () => {
  try {
    detail.value = await getPatientDetail(String(route.params.id))
  } catch {
    return
  }
})

const editing = ref(false)

const drFields = ['静息·肚脐', '静息·脐上', '静息·脐下', '收缩·肚脐', '收缩·脐上', '收缩·脐下']

const vasFields = [
  { key: 'back', label: '腰背痛', sides: ['左', '右', '中'] },
  { key: 'si', label: '骶髂关节痛', sides: ['左', '右', '中'] },
  { key: 'pubis', label: '耻骨疼痛', sides: ['VAS'] },
  { key: 'coccyx', label: '尾骨痛', sides: ['左', '右', '中'] },
  { key: 'sciatic', label: '坐骨神经痛', sides: ['左', '右'] },
  { key: 'groin', label: '腹股沟疼痛', sides: ['左', '右'] },
  { key: 'neck', label: '颈痛', sides: ['左', '右'] },
  { key: 'arm', label: '上肢痛', sides: ['左', '右'] },
  { key: 'leg', label: '下肢痛', sides: ['左', '右'] },
]

const inspectionFields = [
  { key: 'vulva', label: '外阴', options: ['正常', '红', '肿', '热', '痛'] },
  { key: 'vaginalTear', label: '阴道裂口', options: ['正常', '增大'] },
  { key: 'urethral', label: '尿道口', options: ['正常', '增大', '发红'] },
  { key: 'clitoris', label: '阴蒂', options: ['正常', '萎缩'] },
  { key: 'labiaSym', label: '阴唇对称性', options: ['是', '否'] },
  { key: 'discharge', label: '分泌物情况', options: ['无', '有'] },
  { key: 'pubicHair', label: '阴毛', options: ['正常', '过少', '过多'] },
  { key: 'hemorrhoids', label: '痔疮', options: ['无', '外痔', '内痔', '混合痔'] },
  { key: 'valsalva', label: 'VALSALVA动作', options: ['正常', '会阴体下降', '阴道前壁膨出', '阴道后壁膨出', '膀胱脱垂', '子宫脱垂', '直肠脱垂'] },
]

const specialTestFields = [
  { key: 'urogenital', label: '泌尿生殖测试' },
  { key: 'scarTest', label: '疤痕试验' },
  { key: 'faberL', label: '4字试验-左' },
  { key: 'faberR', label: '4字试验-右' },
  { key: 'pelvicSep', label: '骨盆分离试验' },
  { key: 'pelvicComp', label: '骨盆挤压试验' },
  { key: 'sacralPress', label: '骶骨下压试验' },
  { key: 'femoralImp', label: '股骨冲击试验' },
  { key: 'slr', label: '直腿抬高试验' },
  { key: 'slrReinforced', label: '直腿抬高加强试验' },
  { key: 'palpationTest', label: '广泛性触诊试验' },
]

const palpationFields = [
  { key: 'levatorAni', label: '肛提肌', options: ['正常', '压痛', '条索', '颗粒', '薄弱'] },
  { key: 'coccygeus', label: '坐尾肌', options: ['正常', '压痛', '条索', '颗粒', '薄弱'] },
  { key: 'iliococcygeus', label: '髂尾肌', options: ['正常', '压痛', '条索', '颗粒', '薄弱'] },
  { key: 'obturatorInternus', label: '闭孔内肌', options: ['正常', '压痛', '条索', '颗粒', '薄弱'] },
  { key: 'organShift', label: '脏器偏移', options: ['正常', '膀胱', '尿道', '宫颈', '肛管'] },
]

const uploadItems = ['盆底超声', 'X-Ray检查', '尿动力学检查', '其他检查']
const medicationOptions = ['阴道炎', '精神科药物', '甲状腺类药物', '高血压药物', '糖尿病药物', '泌尿系统药物', '抑制妊娠反应药物', '神经系统药物', 'HRT类药物']

const form = reactive({
  chief: '',
  drSeparation: '无',
  drValues: Object.fromEntries(drFields.map((f) => [f, ''])),
  postpartum: {
    urine: '无',
    stool: '正常',
    pelvicPain: '无',
    pelvicVas: '0',
    prolapse: '无',
    scar: '无',
    scarTexture: '',
    scarColor: '',
    scarLength: '',
    scarDate: '',
    urethra: '',
    other: '',
  },
  vas: Object.fromEntries(vasFields.flatMap((p) => p.sides.map((s) => [p.key + s, '0']))),
  inspection: Object.fromEntries(inspectionFields.map((f) => [f.key, f.options[0]]).concat([
    ['dischargeColor', ''], ['dischargeSmell', ''], ['dischargeTexture', ''], ['dischargeAmount', ''], ['other', ''],
  ])),
  specialTest: Object.fromEntries(specialTestFields.map((f) => [f.key, '-']).concat([['other', '']])),
  nerve: { pfTone: '正常', sensation左: '正常', sensation右: '正常', catheter: '无' } as Record<string, string>,
  palpation: Object.fromEntries(palpationFields.map((f) => [f.key, f.options[0]]).concat([['oxford', '']])),
  diagnosis: '',
  medication: [] as string[],
  medOtherChecked: false,
  medOther: '',
  supplement: '',
})

async function onSave() {
  try {
    await saveSupplement(String(route.params.id), { ...form })
    ElMessage.success('补充已保存')
    editing.value = false
  } catch {
    return
  }
}
</script>

<style scoped>
.back-row {
  display: flex;
}
.ro-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 8px 16px;
  margin-top: 8px;
}
.ro-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 16px;
  margin-top: 8px;
}
.ro-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.h3.empty {
  color: #9ca3af;
}
.supplement-card {
  border-style: dashed;
}
.supplement-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #1e9e8f;
  padding-bottom: 6px;
  margin-bottom: 12px;
}
.supplement-title {
  font-size: 16px;
  font-weight: 700;
  color: #6b7280;
}
.read-view,
.edit-view {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.read-section {
  display: flex;
  flex-direction: column;
}
.fld {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.fld label {
  font-size: 12px;
  color: #374151;
}
.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}
.num-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px 16px;
  margin-top: 10px;
}
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.vas-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 36px;
  row-gap: 12px;
}
.row {
  display: flex;
  gap: 6px;
  align-items: center;
}
.unit {
  font-size: 12px;
  color: #6b7280;
  flex-shrink: 0;
}
.upload-text {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.4;
}
</style>
