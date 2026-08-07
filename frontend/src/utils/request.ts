const BASE_URL = '/api'

class AuthError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'AuthError'
  }
}

export interface ApiResult<T = any> {
  code: number
  message: string
  success: boolean
  data: T
}

/**
 * 基础请求方法：注入 Content-Type 与 Authorization 头，401/403 时清除本地 token 并抛 AuthError
 */
export async function request(url: string, options: RequestInit = {}): Promise<Response> {
    
  const token = localStorage.getItem('token')

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  }

  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }

  const response = await fetch(BASE_URL + url, {
    ...options,
    headers,
  })
  
  if (response.status === 403 || response.status === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
    throw new AuthError('登录已过期，请重新登录')
  }

  return response
}

export { AuthError }

/**
 * 解析响应：非 2xx 状态码包装为失败的 ApiResult，否则返回 JSON 数据
 */
async function parseResult<T>(res: Response): Promise<ApiResult<T>> {
  if (!res.ok) {
    return {
      code: res.status,
      message: `请求失败 (${res.status})`,
      success: false,
      data: null as T,
    }
  }
  return res.json()
}

/**
 * GET 请求
 */
export async function get<T = any>(url: string): Promise<ApiResult<T>> {
  const res = await request(url, { method: 'GET' })
  return parseResult<T>(res)
}

/**
 * POST 请求
 */
export async function post<T = any>(url: string, data?: any): Promise<ApiResult<T>> {
  const res = await request(url, {
    method: 'POST',
    body: data ? JSON.stringify(data) : undefined,
  })
  return parseResult<T>(res)
}

/**
 * PUT 请求
 */
export async function put<T = any>(url: string, data?: any): Promise<ApiResult<T>> {
  const res = await request(url, {
    method: 'PUT',
    body: data ? JSON.stringify(data) : undefined,
  })
  return parseResult<T>(res)
}

/**
 * DELETE 请求
 */
export async function del<T = any>(url: string): Promise<ApiResult<T>> {
  const res = await request(url, { method: 'DELETE' })
  return parseResult<T>(res)
}

/**
 * 文件上传：发送 FormData，不设 Content-Type（由浏览器自动添加 boundary）
 */
export async function upload<T = any>(url: string, formData: FormData): Promise<ApiResult<T>> {
  const token = localStorage.getItem('token')
  const headers: Record<string, string> = {}
  if (token) {
    headers['Authorization'] = 'Bearer ' + token
  }

  const response = await fetch(BASE_URL + url, {
    method: 'POST',
    body: formData,
    headers,
  })

  if (response.status === 403 || response.status === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
    throw new AuthError('登录已过期，请重新登录')
  }

  return parseResult<T>(response)
}
