import request from './request'
import { ElMessage } from 'element-plus'
import type { PageResult } from './types'

/** 数据总览 / 报表接口（原型 04 数据总览、05 患者数据详情、06 报表总览、07-1~07-7 报表） */

/** 数据总览行（04 页面表格） */
export interface OverviewRow {
  id: string
  date: string
  name: string
  urine: number
  stool: number
  water: number
  alert: boolean
  done: boolean
}

/** 数据总览响应：分页记录 + KPI */
export interface OverviewResult {
  records: OverviewRow[]
  total: number
  alertCount: number
  checkinRate: number
}

/** 患者数据详情（05 页面）每日记录行 */
export interface RecordRow {
  date: string
  urine: number
  stool: number
  water: number
  isAlert: boolean
  isDone: boolean
  scaleName: string | null
}

export interface CheckinItem {
  time: string
  title: string
  sub: string
  done: boolean
}

export interface ScaleInfo {
  key: string
  name: string
  max: number
}

export interface ScalePoint {
  date: string
  /** 占满分百分比 */
  score: number
  name: string
  max: number
}

/** 患者数据详情响应：记录 + 打卡明细（key=yyyy-MM-dd）+ 量表元信息与历史 */
export interface RecordsResult {
  /** 患者姓名（标题展示） */
  patientName: string
  records: RecordRow[]
  checkins: Record<string, CheckinItem[]>
  scales: ScaleInfo[]
  scaleHistory: Record<string, ScalePoint[]>
}

export interface ExportTaskStatus {
  taskId: string
  reportType: string
  status: 'PROCESSING' | 'SUCCESS' | 'FAILED'
  fileName: string | null
  downloadUrl: string | null
  errorMessage: string | null
}

export function getOverview(params: Record<string, unknown>) {
  return request.get('/admin/v1/report/overview', { params }) as unknown as Promise<OverviewResult>
}

export function getPatientRecords(patientId: string, params: Record<string, unknown>) {
  return request.get(`/admin/v1/report/patient/${patientId}/records`, { params }) as unknown as Promise<RecordsResult>
}

export function getReportData(reportType: string, params: Record<string, unknown>) {
  return request.get(`/admin/v1/report/${reportType}`, { params }) as unknown as Promise<PageResult<Record<string, unknown>>>
}

export function submitExportTask(reportType: string, params: Record<string, unknown>) {
  return request.post('/admin/v1/report/export-task', { reportType, params }) as unknown as Promise<{ taskId: string }>
}

export function getExportTaskStatus(taskId: string) {
  return request.get(`/admin/v1/report/export-task/${taskId}`) as unknown as Promise<ExportTaskStatus>
}

/**
 * 异步导出统一流程：提交任务 → 轮询状态 → 下载文件。
 * 报表页 onExport 只调用本函数并传入 reportType 与当前筛选参数。
 */
export async function exportReport(reportType: string, params: Record<string, unknown>) {
  const { taskId } = await submitExportTask(reportType, params)
  ElMessage.info('导出任务已提交，正在生成文件…')
  const deadline = Date.now() + 120_000
  let task: ExportTaskStatus | null = null
  while (Date.now() < deadline) {
    await new Promise((resolve) => setTimeout(resolve, 1500))
    task = await getExportTaskStatus(taskId)
    if (task.status === 'SUCCESS') break
    if (task.status === 'FAILED') {
      ElMessage.error(task.errorMessage || '导出失败，请稍后重试')
      return
    }
  }
  if (!task || task.status !== 'SUCCESS' || !task.downloadUrl) {
    ElMessage.error('导出超时，请稍后重试')
    return
  }
  // 后端返回完整路径 /api/...，request 的 baseURL 已含 /api，需去重前缀
  const downloadPath = task.downloadUrl.replace(/^\/api/, '')
  const resp = (await request.get(downloadPath, { responseType: 'blob' })) as unknown as { data: Blob }
  const url = URL.createObjectURL(resp.data)
  const a = document.createElement('a')
  a.href = url
  a.download = task.fileName || `${reportType}.xlsx`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出完成')
}
