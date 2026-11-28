import request from './request'
import type { PageResult } from './types'

/** 用户与医生管理接口（原型 01 用户管理 / 02 患者详情 / 03 医生详情） */

/** 用户列表行（与 types/index.ts 的 UserRow 一致） */
export interface UserListRow {
  id: string
  name: string
  role: 'patient' | 'doctor'
  phone: string
}

/** 医生详情（后端 DoctorDetailResponse） */
export interface DoctorDetail {
  id: string
  name: string
  gender: string
  title: string | null
  deptId: number
  deptName: string
  phone: string
  auditStatus: string
  auditReason: string | null
}

export function getUserList(params: { keyword?: string; role?: string; page: number; size: number }) {
  return request.get('/admin/v1/system/users', { params }) as unknown as Promise<PageResult<UserListRow>>
}

/**
 * 患者详情（后端 PatientDetailResponse，结构见 PatientDetailView.vue 中定义的 PatientDetail 类型）
 * 返回 data 为后端聚合实体（字段与后端实体一致）
 */
export function getPatientDetail(patientId: string): Promise<PatientDetail> {
  return request.get(`/admin/v1/patient/${patientId}`) as unknown as Promise<PatientDetail>
}

/** 患者详情页“保存补充”（临床检查医生补充内容），data 与页面 form 结构一致 */
export function saveSupplement(patientId: string, data: Record<string, unknown>) {
  return request.put(`/admin/v1/patient/${patientId}/supplement`, data) as unknown as Promise<void>
}

export function getDoctorDetail(doctorId: string) {
  return request.get(`/admin/v1/doctor/${doctorId}`) as unknown as Promise<DoctorDetail>
}

/** 医生详情页“移除” */
export function removeDoctor(doctorId: string) {
  return request.delete(`/admin/v1/doctor/${doctorId}`) as unknown as Promise<void>
}

/** 用户管理页“添加医生”（姓名、性别、科室、手机号） */
export function addDoctor(data: { name: string; gender: string; deptId: number; phone: string }) {
  return request.post('/admin/v1/doctor', data) as unknown as Promise<DoctorDetail>
}

/** 患者详情响应（后端 PatientDetailResponse 的 TS 映射） */
export interface PatientDetail {
  patientId: string
  name: string
  gender: string
  birthDate: string | null
  age: number | null
  phone: string
  heightCm: number | null
  weightKg: number | null
  marriage: string | null
  education: string | null
  job: string | null
  remark: string | null
  healthProfile: HealthProfile | null
  conditions: PatientCondition[]
  surgeries: SurgeryHistory[]
  pregnancies: Pregnancy[]
  latestExam: LatestExam | null
}

export interface HealthProfile {
  chief: string | null
  stool: string | null
  urine: string | null
  sexFrequencyWeekly: number | null
  sexFrequencyMonthly: number | null
  promiscuityHistory: boolean | null
  pastExerciseLifeSupplement: string | null
  menopause: boolean | null
  menstruationDays: number | null
  cycleDays: number | null
  dysmenorrheaVas: number | null
  lastMenstrualDate: string | null
  menopauseAge: number | null
  smokingPerDay: number | null
  alcoholMlPerDay: number | null
  teaCoffeeMlPerDay: number | null
  sedentaryHoursPerDay: number | null
  standingHoursPerDay: number | null
  otherHabits: string | null
}

export interface PatientCondition {
  id: number
  patientId: number
  category: string
  itemName: string
  detail: string | null
}

export interface SurgeryHistory {
  id: number
  patientId: number
  surgeryCategory: string
  surgeryName: string
  surgeryDate: string | null
  remark: string | null
}

export interface Pregnancy {
  id: number
  patientId: number
  pregnancyNo: number
  outcome: string | null
  preWeightKg: number | null
  prenatalWeightKg: number | null
  weightGainKg: number | null
  sleepPosition: string | null
  incontinence: boolean | null
  stool: string | null
  bloodPressure: string | null
  bloodSugar: string | null
  thyroidFunction: string | null
  backPainVas: number | null
  pubicPainVas: number | null
  lowerLimbPainVas: number | null
  reactionLevel: string | null
  perinealItching: boolean | null
  diet: string | null
  sexLife: boolean | null
  deliveryDate: string | null
  deliveryMethod: string | null
  perineumCondition: string | null
  painlessDelivery: boolean | null
  cephalicDelivery: boolean | null
  deliveryAssist: string | null
  babyBirthWeightKg: number | null
  remark: string | null
}

export interface LatestExam {
  examination: {
    id: number
    examTime: string | null
    chief: string | null
    abdominalTension: string | null
    diagnosis: string | null
    medication: string | null
    otherSupplement: string | null
  } | null
  diastasis: {
    restingUmbilicus: number | null
    restingAbove: number | null
    restingBelow: number | null
    contractionUmbilicus: number | null
    contractionAbove: number | null
    contractionBelow: number | null
  } | null
  postpartum: {
    urine: string | null
    stool: string | null
    pelvicPerinealPainVas: number | null
    organProlapse: string | null
    scar: boolean | null
    scarTexture: string | null
    scarColor: string | null
    scarDate: string | null
    scarLengthCm: number | null
    urethralInstability: boolean | null
    other: string | null
  } | null
  painAssessment: {
    backPainLeftVas: number | null
    backPainRightVas: number | null
    backPainCenterVas: number | null
    siJointPainLeftVas: number | null
    siJointPainRightVas: number | null
    siJointPainCenterVas: number | null
    pubicPainVas: number | null
    coccyxPainLeftVas: number | null
    coccyxPainRightVas: number | null
    coccyxPainCenterVas: number | null
    sciaticPainLeftVas: number | null
    sciaticPainRightVas: number | null
    groinPainLeftVas: number | null
    groinPainRightVas: number | null
    neckPainLeftVas: number | null
    neckPainRightVas: number | null
    armPainLeftVas: number | null
    armPainRightVas: number | null
    legPainLeftVas: number | null
    legPainRightVas: number | null
  } | null
  pelvicInspection: {
    vulva: string | null
    vaginalTear: string | null
    urethralMeatus: string | null
    clitoris: string | null
    labiaSymmetry: boolean | null
    discharge: boolean | null
    dischargeColor: string | null
    dischargeSmell: string | null
    dischargeTexture: string | null
    dischargeAmount: number | null
    pubicHair: string | null
    hemorrhoids: string | null
    valsalva: string | null
  } | null
  specialExam: {
    urogenitalTest: string | null
    scarTest: string | null
    faberLeft: string | null
    faberRight: string | null
    pelvicSeparation: string | null
    pelvicCompression: string | null
    sacralCompression: string | null
    femoralImpaction: string | null
    straightLegRaise: string | null
    straightLegRaiseReinforced: string | null
    palpationTest: string | null
    other: string | null
  } | null
  neurologicalExam: {
    pelvicFloorTone: string | null
    leftSensation: string | null
    rightSensation: string | null
    catheter: boolean | null
  } | null
  analPalpation: {
    levatorAni: string | null
    coccygeus: string | null
    iliococcygeus: string | null
    obturatorInternus: string | null
    organShift: string | null
    oxfordGrade: number | null
    coccyxImpaction: boolean | null
    perianalTemperature: string | null
    perianalMoisture: string | null
    perinealMuscleTone: string | null
  } | null
  notes: {
    id: number
    noteType: string
    content: string
    visibility: string
  }[]
}
