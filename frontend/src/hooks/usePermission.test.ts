import { act, renderHook } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { usePermission } from '@/hooks/usePermission'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'

const initialState = useAuthStore.getInitialState()

describe('usePermission', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
  })

  it('is false for a visitor', () => {
    const { result } = renderHook(() => usePermission('DASHBOARD_READ'))

    expect(result.current).toBe(false)
  })

  it('is true when the user has the permission', () => {
    useAuthStore.getState().setAuth(buildUser({ permissions: ['DASHBOARD_READ'] }), 'token')

    const { result } = renderHook(() => usePermission('DASHBOARD_READ'))

    expect(result.current).toBe(true)
  })

  it('is false when the user has other permissions only', () => {
    useAuthStore.getState().setAuth(buildUser({ permissions: ['PRODUCTS_READ'] }), 'token')

    const { result } = renderHook(() => usePermission('DASHBOARD_READ'))

    expect(result.current).toBe(false)
  })

  it('follows the session when the user logs in and out', () => {
    const { result } = renderHook(() => usePermission('DASHBOARD_READ'))

    act(() => useAuthStore.getState().setAuth(buildUser({ permissions: ['DASHBOARD_READ'] }), 'token'))
    expect(result.current).toBe(true)

    act(() => useAuthStore.getState().clearAuth())
    expect(result.current).toBe(false)
  })
})
