import request from './request'

/** 数据总览 / 报表接口（原型 04 数据总览、05 患者数据详情、06 报表总览、07-1~07-7 报表） */

// TODO: 联调后端接口（模块 module-report）
export function getOverview(params: Record<string, unknown>) {
  return request.get('/admin/v1/report/overview', { params })
}

export function getPatientRecords(patientId: string, params: Record<string, unknown>) {
  return request.get(`/admin/v1/report/patient/${patientId}/records`, { params })
}

export function getReportData(reportType: string, params: Record<string, unknown>) {
  return request.get(`/admin/v1/report/${reportType}`, { params })
}

/**
 * 导出：架构文档约定统一走"提交导出任务 → 轮询任务状态 → 下载签名 URL"
 * TODO: 联调后端异步导出任务接口
 */
export function submitExportTask(reportType: string, params: Record<string, unknown>) {
  return request.post('/admin/v1/report/export-task', { reportType, params })
}

export function getExportTaskStatus(taskId: string) {
  return request.get(`/admin/v1/report/export-task/${taskId}`)
}
