import request from './request'

/** 用户与医生管理接口（原型 01 用户管理 / 02 患者详情 / 03 医生详情） */

// TODO: 联调后端接口（模块 module-system / module-patient / module-doctor）
export function getUserList(params: { keyword?: string; role?: string; page: number; size: number }) {
  return request.get('/admin/v1/system/users', { params })
}

export function getPatientDetail(patientId: string) {
  return request.get(`/admin/v1/patient/${patientId}`)
}

export function getDoctorDetail(doctorId: string) {
  return request.get(`/admin/v1/doctor/${doctorId}`)
}

/** 患者详情页"保存补充"（临床检查医生补充内容） */
export function saveSupplement(patientId: string, data: Record<string, unknown>) {
  return request.put(`/admin/v1/patient/${patientId}/supplement`, data)
}

/** 医生详情页"移除" */
export function removeDoctor(doctorId: string) {
  return request.delete(`/admin/v1/doctor/${doctorId}`)
}

/** 用户管理页"添加医生"（姓名、性别、科室） */
export function addDoctor(data: { name: string; gender: string; deptId: string }) {
  return request.post('/admin/v1/doctor', data)
}
