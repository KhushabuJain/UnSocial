import { createContext, useContext, useState, useCallback } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('unsocial-user')
      return saved ? JSON.parse(saved) : null
    } catch { return null }
  })

  const login = useCallback((userData) => {
    // userData = { token, email, fullName, role }
    setUser(userData)
    localStorage.setItem('unsocial-user', JSON.stringify(userData))
    localStorage.setItem('unsocial-token', userData.token)
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    localStorage.removeItem('unsocial-user')
    localStorage.removeItem('unsocial-token')
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
