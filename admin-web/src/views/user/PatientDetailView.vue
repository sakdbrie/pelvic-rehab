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
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { saveSupplement } from '@/api/system'

// 原型图：02 用户信息详情页面（网页后台-患者）——只读档案 + 医生"补充"临床检查
const route = useRoute()
const router = useRouter()

// TODO: 替换为接口数据（api/system.ts getPatientDetail）
const basicInfo = [
  { k: '姓名', v: '王丽' },
  { k: '性别', v: '女' },
  { k: '出生年月', v: '1994-03' },
  { k: '年龄', v: '32' },
  { k: '联系电话', v: '138****0000' },
  { k: '身高', v: '162cm' },
  { k: '体重', v: '55Kg' },
  { k: '从事职业', v: '全职类工作' },
  { k: '婚姻状况', v: '已婚' },
  { k: '教育程度', v: '大学' },
  { k: '既往运动情况', v: '每周跑步 3 次', full: true },
  { k: '生活习惯', v: '抽烟 0 支/天，喝酒 0 ml/天，茶/咖啡 200 ml/天，久坐 6 h/天，久站 2 h/天', full: true },
]

const historyInfo = [
  { k: '月经史', v: '未绝经；末次月经：2026-09，持续时间 5 天，周期 28 天，无痛经', full: true },
  { k: '女性性功能障碍（ICD-11分类）', v: '—' },
  { k: '大便情况', v: '正常' },
  { k: '小便情况', v: '尿失禁' },
  { k: '性生活', v: '频率 1 次/周，无冶游史' },
  { k: '手术史', v: '2020-06 腔镜手术' },
  { k: '既往史', v: '无特殊' },
]

const pregnancyInfo = [
  { k: '产科史', v: '怀孕 2 次，分娩 1 次，流产 1 次；最近分娩日期：2020-06' },
  { k: '分娩方式', v: '剖宫产；无痛分娩：否；头位正常分娩：否；助产方式：无；胎儿出生体重：3.2 Kg' },
  { k: '孕期情况', v: '孕前 50 kg · 产前 62 kg · 增重 12 Kg；睡觉体位：左侧；孕期尿失禁：无；孕期大便：便秘；血压：正常；血糖：正常；甲状腺功能：—；无腰背痛，无耻骨疼痛，无下肢疼痛；妊娠反应：轻微；会阴瘙痒：无；饮食情况：正常；孕期性生活：无', full: true },
]

const readSections = [
  { title: '主诉', value: '—' },
  { title: '腹直肌分离', value: '无' },
  { title: '产后情况', items: ['小便：无', '大便：正常', '盆腔、会阴痛：无', '脏器脱垂：无', '瘢痕：无', '尿道不稳定性：—', '其他：—'] },
  { title: '疼痛情况', items: ['腰背痛（左/右/中）：0 / 0 / 0', '骶髂关节痛（左/右/中）：0 / 0 / 0', '耻骨疼痛：0', '尾骨痛（左/右/中）：0 / 0 / 0', '坐骨神经痛（左/右）：0 / 0', '腹股沟疼痛（左/右）：0 / 0', '颈痛（左/右）：0 / 0', '上肢痛（左/右）：0 / 0', '下肢痛（左/右）：0 / 0'] },
  { title: '盆底视诊', items: ['外阴：正常', '阴道裂口：正常', '尿道口：正常', '阴蒂：正常', '阴唇对称性：是', '分泌物情况：无', '阴毛：正常', '痔疮：无', 'VALSALVA动作：正常', '其他：—'] },
  { title: '特殊检查', items: ['泌尿生殖测试：-', '疤痕试验：-', '4字试验-左：-', '4字试验-右：-', '骨盆分离试验：-', '骨盆挤压试验：-', '骶骨下压试验：-', '股骨冲击试验：-', '直腿抬高试验：-', '直腿抬高加强试验：-', '广泛性触诊试验：-', '其他：—'] },
  { title: '神经损伤查体', items: ['盆底张力：正常', '感觉-左：正常', '感觉-右：正常', '导尿管：无'] },
  { title: '触诊', items: ['肛提肌：正常', '坐尾肌：正常', '髂尾肌：正常', '闭孔内肌：正常', '脏器偏移：正常', '牛津肌力：— 级'] },
  { title: '盆底超声（可上传照片）', value: '未上传' },
  { title: 'X-Ray检查（可上传照片）', value: '未上传' },
  { title: '尿动力学检查（可上传照片）', value: '未上传' },
  { title: '其他检查（可上传照片）', value: '未上传' },
  { title: '诊断', value: '—' },
  { title: '药物治疗', value: '—' },
  { title: '其它补充（可上传照片）', value: '—' },
]

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

function onSave() {
  // TODO: 联调后端保存接口（api/system.ts saveSupplement）
  void saveSupplement(String(route.params.id), { ...form })
  ElMessage.success('补充已保存（原型占位）')
  editing.value = false
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
