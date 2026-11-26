import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook } from '@testing-library/react'
import type { ReactNode } from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { usePollCartAfterCheckout } from '@/hooks/usePollCartAfterCheckout'
import { queryKeys } from '@/queryKeys'

const queryClient = new QueryClient()
const wrapper = ({ children }: { children: ReactNode }) => (
  <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
)
const invalidate = vi.spyOn(queryClient, 'invalidateQueries')

describe('usePollCartAfterCheckout', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('invalidates the cart right away', () => {
    renderHook(() => usePollCartAfterCheckout(), { wrapper })

    expect(invalidate).toHaveBeenCalledTimes(1)
    expect(invalidate).toHaveBeenCalledWith({ queryKey: queryKeys.cart })
  })

  it('invalidates the cart every 2 seconds', () => {
    renderHook(() => usePollCartAfterCheckout(), { wrapper })

    vi.advanceTimersByTime(1999)
    expect(invalidate).toHaveBeenCalledTimes(1)

    vi.advanceTimersByTime(1)
    expect(invalidate).toHaveBeenCalledTimes(2)

    vi.advanceTimersByTime(2000)
    expect(invalidate).toHaveBeenCalledTimes(3)
  })

  it('stops after 5 polls', () => {
    renderHook(() => usePollCartAfterCheckout(), { wrapper })

    vi.advanceTimersByTime(60_000)

    expect(invalidate).toHaveBeenCalledTimes(6)
  })

  it('stops polling when the component unmounts', () => {
    const { unmount } = renderHook(() => usePollCartAfterCheckout(), { wrapper })

    unmount()
    vi.advanceTimersByTime(60_000)

    expect(invalidate).toHaveBeenCalledTimes(1)
  })
})
