import { useEffect, useState } from 'react'
import { apiFetch } from '../services/api'

const cachePrefix = 'portfolio-public-api:'

export function useApi(path) {
  const [requestVersion, setRequestVersion] = useState(0)
  const [state, setState] = useState({ path: null, data: null, loading: true, error: null })

  useEffect(() => {
    let active = true
    apiFetch(path)
      .then((data) => {
        writeCache(path, data)
        if (active) setState({ path, data, loading: false, error: null })
      })
      .catch((error) => {
        if (error.status === 404) removeCache(path)
        const cached = readCache(path)
        if (active) setState({ path, data: cached.data, loading: false, error })
      })
    return () => { active = false }
  }, [path, requestVersion])

  const isCurrentRequest = state.path === path
  const cached = isCurrentRequest ? null : readCache(path)
  return {
    data: isCurrentRequest ? state.data : cached?.data ?? null,
    loading: !isCurrentRequest || state.loading,
    error: isCurrentRequest ? state.error : null,
    retry: () => setRequestVersion((version) => version + 1),
  }
}

function readCache(path) {
  try {
    const value = window.localStorage.getItem(`${cachePrefix}${path}`)
    if (value === null) return null
    return { data: JSON.parse(value) }
  } catch {
    return null
  }
}

function writeCache(path, data) {
  try {
    window.localStorage.setItem(`${cachePrefix}${path}`, JSON.stringify(data))
  } catch {
    return
  }
}

function removeCache(path) {
  try {
    window.localStorage.removeItem(`${cachePrefix}${path}`)
  } catch {
    return
  }
}
