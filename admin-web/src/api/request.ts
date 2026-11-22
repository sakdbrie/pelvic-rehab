import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, setToken } from '@/utils/auth'

/**
 * axios 实例：Token 注入 / 401 静默刷新 / 统一错误提示
 * 架构文档约定：接口前缀 /api/admin/v1/**
 * TODO: 联调后端时接入 refresh token（httpOnly Cookie）刷新流程
 */
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000,
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (resp) => resp,
  async (error: AxiosError) => {
    // TODO: 联调后端时在此实现 401 静默刷新（refresh token 走 httpOnly Cookie），失败跳登录
    const status = error.response?.status
    if (status === 401) {
      setToken(null)
      ElMessage.error('登录已失效，请重新登录')
      window.location.href = '/login'
      return Promise.reject(error)
    }
    const msg = (error.response?.data as { message?: string } | undefined)?.message
    ElMessage.error(msg || '请求失败，请稍后重试')
    return Promise.reject(error)
  },
)

export default request
