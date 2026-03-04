import { describe, it, expect, beforeEach } from 'vitest'
import { act } from '@testing-library/react'
import { useAuthStore } from '@/store/auth.store'

// Reset store state between tests
beforeEach(() => {
  act(() => {
    useAuthStore.setState({ token: null, user: null, isAuthenticated: false })
  })
  localStorage.clear()
})

describe('useAuthStore — initial state', () => {
  it('starts with token null', () => {
    expect(useAuthStore.getState().token).toBeNull()
  })

  it('starts unauthenticated', () => {
    expect(useAuthStore.getState().isAuthenticated).toBe(false)
  })

  it('starts with user null', () => {
    expect(useAuthStore.getState().user).toBeNull()
  })
})

describe('useAuthStore — login', () => {
  const loginData = {
    token: 'eyJhbGciOiJIUzI1NiJ9.test',
    tipo: 'Bearer',
    expiresIn: 900,
    username: 'admin',
    roles: ['ROLE_ADMIN'],
  }

  it('sets token after login', () => {
    act(() => useAuthStore.getState().login(loginData))
    expect(useAuthStore.getState().token).toBe(loginData.token)
  })

  it('sets isAuthenticated to true after login', () => {
    act(() => useAuthStore.getState().login(loginData))
    expect(useAuthStore.getState().isAuthenticated).toBe(true)
  })

  it('sets user after login', () => {
    act(() => useAuthStore.getState().login(loginData))
    expect(useAuthStore.getState().user?.username).toBe('admin')
    expect(useAuthStore.getState().user?.roles).toEqual(['ROLE_ADMIN'])
  })

  it('persists token in localStorage', () => {
    act(() => useAuthStore.getState().login(loginData))
    expect(localStorage.getItem('token')).toBe(loginData.token)
  })
})

describe('useAuthStore — logout', () => {
  it('clears state after logout', () => {
    act(() => {
      useAuthStore.getState().login({
        token: 'some-token',
        tipo: 'Bearer',
        expiresIn: 900,
        username: 'admin',
        roles: ['ROLE_ADMIN'],
      })
    })
    act(() => useAuthStore.getState().logout())

    expect(useAuthStore.getState().token).toBeNull()
    expect(useAuthStore.getState().isAuthenticated).toBe(false)
    expect(useAuthStore.getState().user).toBeNull()
  })

  it('removes token from localStorage after logout', () => {
    localStorage.setItem('token', 'some-token')
    act(() => useAuthStore.getState().logout())
    expect(localStorage.getItem('token')).toBeNull()
  })
})

describe('useAuthStore — hasRole', () => {
  beforeEach(() => {
    act(() => {
      useAuthStore.getState().login({
        token: 'tok',
        tipo: 'Bearer',
        expiresIn: 900,
        username: 'admin',
        roles: ['ROLE_ADMIN'],
      })
    })
  })

  it('returns true for ADMIN when user has ROLE_ADMIN', () => {
    expect(useAuthStore.getState().hasRole('ADMIN')).toBe(true)
  })

  it('returns true for ROLE_ADMIN (accepts full prefix)', () => {
    expect(useAuthStore.getState().hasRole('ROLE_ADMIN')).toBe(true)
  })

  it('returns false for role not in list', () => {
    expect(useAuthStore.getState().hasRole('CLIENTE')).toBe(false)
  })

  it('returns false when not authenticated', () => {
    act(() => useAuthStore.getState().logout())
    expect(useAuthStore.getState().hasRole('ADMIN')).toBe(false)
  })
})
