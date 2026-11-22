import request from './request'

/** 登录认证接口（原型 00 后台登录页 / 08 个人主页修改密码、退出登录） */

export interface LoginParams {
  phone: string
  password?: string
  smsCode?: string
}

// TODO: 联调后端接口（模块 module-auth，前缀 /api/admin/v1/auth/**）
export function loginByPassword(data: LoginParams) {
  return request.post('/admin/v1/auth/login/password', data)
}

export function loginBySms(data: LoginParams) {
  return request.post('/admin/v1/auth/login/sms', data)
}

// 短信验证码登录
export function sendSmsCode(phone: string) {
  return request.post('/admin/v1/auth/sms-code', { phone, scene: 'LOGIN' })
}

export function logout() {
  return request.post('/admin/v1/auth/logout')
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.post('/admin/v1/auth/change-password', data)
}
