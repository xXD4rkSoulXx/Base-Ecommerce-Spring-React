import { renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { DEFAULT_DASHBOARD_PERIOD } from '@/constants'
import { useAdminDashboardDays } from '@/hooks/admin/useAdminDashboardDays'
import { createRouterWrapper } from '@/test/render'

const renderDays = (entry: string) =>
  renderHook(() => useAdminDashboardDays(), { wrapper: createRouterWrapper([entry]) })

describe('useAdminDashboardDays', () => {
  it.each([7, 30, 90])('accepts the period of %s days', (days) => {
    const { result } = renderDays(`/?days=${days}`)

    expect(result.current).toBe(days)
  })

  it.each(['/', '/?days=15', '/?days=abc', '/?days=-7', '/?days='])(
    'falls back to the default period for %s',
    (entry) => {
      const { result } = renderDays(entry)

      expect(result.current).toBe(DEFAULT_DASHBOARD_PERIOD)
    }
  )
})
