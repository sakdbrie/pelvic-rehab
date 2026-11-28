import request from './request'

/** 登录认证接口（原型 00 后台登录页 / 08 个人主页修改密码、退出登录） */

export interface LoginParams {
  phone: string
  password?: string
  smsCode?: string
}

/** 登录成功响应（后端实现逻辑.md 第 2.5 节“响应数据”） */
export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  account: {
    accountId: number
    nickname: string
  }
  user: {
    userId: string | null
    roleCode: string | null
    roleName: string | null
    permissions: string[]
  }
}

/** 当前登录用户信息（GET /auth/me） */
export interface MeInfo {
  accountId: number
  accountNo: string
  nickname: string
  name: string
  userId: string | null
  roleCode: string | null
  roleName: string | null
  phone: string
  deptName: string | null
  title: string | null
  permissions: string[]
}

export function loginByPassword(data: LoginParams) {
  return request.post('/admin/v1/auth/login/password', data) as unknown as Promise<LoginResult>
}

export function loginBySms(data: LoginParams) {
  return request.post('/admin/v1/auth/login/sms', data) as unknown as Promise<LoginResult>
}

/** 发送短信验证码（登录场景） */
export function sendSmsCode(phone: string) {
  return request.post('/admin/v1/auth/sms-code', { phone, scene: 'LOGIN' }) as unknown as Promise<void>
}

export function logout() {
  return request.post('/admin/v1/auth/logout') as unknown as Promise<void>
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.post('/admin/v1/auth/change-password', data) as unknown as Promise<void>
}

/** 当前登录用户信息（个人主页） */
export function getMe() {
  return request.get('/admin/v1/auth/me') as unknown as Promise<MeInfo>
}
