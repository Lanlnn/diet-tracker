const TOKEN_KEY = 'diet-admin-session-v1'

export function readSession() {
  try { return JSON.parse(sessionStorage.getItem(TOKEN_KEY) || 'null') }
  catch { return null }
}

export function writeSession(session) {
  if (session) sessionStorage.setItem(TOKEN_KEY, JSON.stringify(session))
  else sessionStorage.removeItem(TOKEN_KEY)
}

export class ApiFailure extends Error {
  constructor(status, body) {
    super(body?.message || '服务暂时不可用')
    this.status = status
    this.code = body?.code || 'REQUEST_FAILED'
    this.requestId = body?.requestId
    this.fields = body?.fields || {}
  }
}

export async function api(path, options = {}) {
  const session = readSession()
  const headers = { Accept: 'application/json', ...options.headers }
  if (options.body && !headers['Content-Type']) headers['Content-Type'] = 'application/json'
  if (session?.token) headers.Authorization = `Bearer ${session.token}`
  const response = await fetch(`/api/admin${path}`, { ...options, headers })
  if (response.status === 204) return null
  const body = await response.json().catch(() => null)
  if (!response.ok) throw new ApiFailure(response.status, body)
  return body
}
