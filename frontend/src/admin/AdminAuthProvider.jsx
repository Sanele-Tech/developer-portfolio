import { useEffect, useMemo, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { apiFetch } from '../services/api'
import { AdminAuthContext } from './adminAuthContext'

export default function AdminAuthProvider({ children }) {
  const [state, setState] = useState({ user: null, loading: true, error: null })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/auth/me')
      .then((user) => {
        if (active) setState({ user, loading: false, error: null })
      })
      .catch((error) => {
        if (active) setState({ user: null, loading: false, error })
      })
    return () => { active = false }
  }, [attempt])

  const value = useMemo(() => ({
    ...state,
    setUser: (user) => setState({ user, loading: false, error: null }),
  }), [state])

  if (state.loading) return <main className="admin-auth-status" role="status">Checking your session…</main>
  if (state.error && state.error.status !== 401) {
    return <main className="admin-auth-status"><div role="alert"><strong>Could not verify your session.</strong><p>{state.error.message}</p><button type="button" className="admin-primary-button" onClick={() => setAttempt((value) => value + 1)}>Try again</button></div></main>
  }
  if (!state.user) return <Navigate to="/admin/login" replace />
  if (!state.user.roles?.some((role) => ['ADMIN', 'EDITOR'].includes(role))) {
    return <main className="admin-auth-status"><div role="alert"><strong>Admin access required.</strong><p>Your account does not have an ADMIN or EDITOR role.</p><Link className="admin-primary-button" to="/">Return to public site</Link></div></main>
  }
  return <AdminAuthContext.Provider value={value}>{children}</AdminAuthContext.Provider>
}
