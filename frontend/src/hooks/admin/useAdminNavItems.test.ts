import { renderHook } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { useAdminNavItems } from '@/hooks/admin/useAdminNavItems'
import { useAuthStore } from '@/store/useAuthStore'
import { buildUser } from '@/test/builders'

const initialState = useAuthStore.getInitialState()

const labels = () => renderHook(() => useAdminNavItems()).result.current.map((item) => item.label)

describe('useAdminNavItems', () => {
  beforeEach(() => {
    useAuthStore.setState(initialState, true)
    localStorage.clear()
  })

  it('has no items for a visitor', () => {
    expect(labels()).toEqual([])
  })

  it('has no items for a user without permissions', () => {
    useAuthStore.getState().setAuth(buildUser(), 'token')

    expect(labels()).toEqual([])
  })

  it('only has the items the user has permission for, in menu order', () => {
    useAuthStore.getState().setAuth(buildUser({ permissions: ['STOCK_READ', 'DASHBOARD_READ', 'AUDIT_READ'] }), 'token')

    expect(labels()).toEqual(['Dashboard', 'Stock', 'Audit log'])
  })

  it('ignores write permissions on their own', () => {
    useAuthStore.getState().setAuth(buildUser({ permissions: ['PRODUCTS_WRITE'] }), 'token')

    expect(labels()).toEqual([])
  })
})
