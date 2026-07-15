import { useState } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/context.js'
import { Icon } from '../components/Icons.jsx'

export function LoginPage() {
  const { status, login } = useAuth()
  const [form, setForm] = useState({ username: '', password: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const location = useLocation()
  if (status === 'authenticated') return <Navigate to={location.state?.from || '/'} replace />
  async function submit(event) {
    event.preventDefault(); setError(null); setSubmitting(true)
    try { await login(form) } catch (failure) { setError(failure) } finally { setSubmitting(false) }
  }
  return <main className="login-page">
    <section className="login-brand"><div className="login-mark"><svg viewBox="0 0 64 64"><path d="M17 46C5 34 10 15 27 12c1 9 6 15 15 18-9 2-15 9-16 17"/><path d="M31 52c3-14 11-26 24-35 2 18-6 30-24 35Z"/></svg></div><h1>轻养管理后台</h1><p>维护可信的系统食品数据，让每一次营养计算都有一致来源。</p><dl><div><dt>独立身份</dt><dd>管理端会话与小程序完全隔离</dd></div><div><dt>最小权限</dt><dd>菜单与接口执行同一角色边界</dd></div><div><dt>全程审计</dt><dd>食品写入保留原因与变更摘要</dd></div></dl></section>
    <section className="login-panel"><div className="login-card"><span className="login-lock"><Icon name="lock" size={22}/></span><h2>管理员登录</h2><p>请输入由系统管理员分配的独立账号。</p>
      <form onSubmit={submit}><label><span>用户名</span><input autoFocus autoComplete="username" required minLength="3" maxLength="64" value={form.username} onChange={(e) => setForm((v) => ({ ...v, username: e.target.value }))}/></label><label><span>密码</span><input type="password" autoComplete="current-password" required minLength="8" maxLength="128" value={form.password} onChange={(e) => setForm((v) => ({ ...v, password: e.target.value }))}/></label>
        {error ? <div className="inline-error" role="alert"><Icon name="alert"/><span>{error.message}{error.requestId ? <small>请求 ID：{error.requestId}</small> : null}</span></div> : null}
        <button className="button primary full" disabled={submitting} type="submit">{submitting ? '正在验证…' : '安全登录'}</button></form>
      <small className="login-help">连续失败会触发临时限速；如需重置会话，请联系超级管理员。</small></div></section>
  </main>
}
