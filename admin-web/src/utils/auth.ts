/**
 * token 存取（架构文档约定：access token 存内存 + refresh token 走 httpOnly Cookie）
 * 内存 token 丢失（如刷新页面）后由 request.ts 静默刷新恢复。
 */
let accessToken: string | null = null

export function getToken(): string | null {
  return accessToken
}

export function setToken(token: string | null) {
  accessToken = token
}
