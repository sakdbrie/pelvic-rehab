/** 全局通用类型 */

/** 当前登录用户信息（由 /api/admin/v1/auth/userinfo 下发，含权限码） */
export interface UserInfo {
  userId: string
  name: string
  /** ADMIN | DOCTOR（原型 01-1 管理员 / 01-2 医生 身份判断依据） */
  role: 'ADMIN' | 'DOCTOR'
  deptName: string
  phone: string
  /** 按钮/接口权限码，如 patient:export */
  permissions: string[]
}

/** 用户管理列表行（原型 01-1/01-2 表格：编号/姓名/角色/手机号） */
export interface UserRow {
  id: string
  name: string
  role: 'patient' | 'doctor'
  phone: string
}
