import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, setToken } from '@/utils/auth'

/**
 * axios 实例：Token 注入 / 401 处理 / 统一错误提示 / Result 解包
 * 架构文档约定：接口前缀 /api/admin/v1/**
 * 联调约定：后端统一响应 { code, message, data }（admin-web/src/api/types.ts 的 Result<T>）：
 *   - code=0 时本实例直接返回 data（已解包，业务代码拿到的就是 data）
 *   - code!=0 时统一弹错并 reject，页面无需重复提示
 *   - responseType=blob（文件下载）不参与解包
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
  (resp) => {
    // 文件下载（blob）原样返回
    if (resp.config.responseType === 'blob') {
      return resp
    }
    const body = resp.data as { code?: number; message?: string; data?: unknown } | undefined
    if (body && typeof body === 'object' && typeof body.code === 'number') {
      if (body.code === 0) {
        return body.data as never
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body as never
  },
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
