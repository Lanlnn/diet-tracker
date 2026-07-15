import { useCallback, useEffect, useMemo, useState } from 'react'
import { api, readSession, writeSession } from '../api/client.js'
import { AuthContext } from './context.js'

const INITIAL_SESSION = readSession()

export function AuthProvider({ children }) {
  const initialSession = INITIAL_SESSION
  const [session, setSession] = useState(initialSession)
  const [status, setStatus] = useState(initialSession ? 'loading' : 'anonymous')

  useEffect(() => {
    if (!initialSession) return
    let active = true
    api('/auth/me').then((profile) => {
      if (!active) return
      const next = { ...initialSession, ...profile }
      writeSession(next); setSession(next); setStatus('authenticated')
    }).catch(() => {
      if (!active) return
      writeSession(null); setSession(null); setStatus('anonymous')
    })
    return () => { active = false }
  }, [initialSession])

  const login = useCallback(async (credentials) => {
    const next = await api('/auth/login', { method: 'POST', body: JSON.stringify(credentials) })
    writeSession(next); setSession(next); setStatus('authenticated')
  }, [])

  const logout = useCallback(async () => {
    try { await api('/auth/logout', { method: 'POST' }) } finally {
      writeSession(null); setSession(null); setStatus('anonymous')
    }
  }, [])

  const value = useMemo(() => ({ session, status, login, logout }), [session, status, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
