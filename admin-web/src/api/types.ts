/** 后端统一响应/分页类型（架构文档 pelvic-common 约定 Result<T> / PageResult<T>） */

export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T = unknown> {
  total: number
  records: T[]
}
