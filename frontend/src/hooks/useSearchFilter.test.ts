import { act, renderHook } from '@testing-library/react'
import type { SubmitEvent } from 'react'
import { useLocation } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useSearchFilter } from '@/hooks/useSearchFilter'
import { createRouterWrapper } from '@/test/render'

const renderSearch = (entry = '/') =>
  renderHook(() => ({ search: useSearchFilter(), location: useLocation() }), {
    wrapper: createRouterWrapper([entry])
  })

describe('useSearchFilter', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('starts with the search text from the URL', () => {
    const { result } = renderSearch('/?q=jacket')

    expect(result.current.search.inputValue).toBe('jacket')
  })

  it('only updates the URL after 400 ms without typing', () => {
    const { result } = renderSearch()

    act(() => result.current.search.setInputValue('jac'))
    act(() => vi.advanceTimersByTime(399))
    expect(result.current.location.search).toBe('')

    act(() => vi.advanceTimersByTime(1))
    expect(result.current.location.search).toBe('?q=jac')
  })

  it('restarts the wait while the user keeps typing', () => {
    const { result } = renderSearch()

    act(() => result.current.search.setInputValue('ja'))
    act(() => vi.advanceTimersByTime(300))
    act(() => result.current.search.setInputValue('jac'))
    act(() => vi.advanceTimersByTime(300))
    expect(result.current.location.search).toBe('')

    act(() => vi.advanceTimersByTime(100))
    expect(result.current.location.search).toBe('?q=jac')
  })

  it('trims the text and removes the filter when it is empty', () => {
    const { result } = renderSearch('/?q=jacket')

    act(() => result.current.search.setInputValue('   '))
    act(() => vi.advanceTimersByTime(400))

    expect(result.current.location.search).toBe('')
  })

  it('submits right away without waiting for the debounce', () => {
    const { result } = renderSearch()
    const preventDefault = vi.fn()

    act(() => result.current.search.setInputValue(' coat '))
    act(() => result.current.search.submitSearch({ preventDefault } as unknown as SubmitEvent<HTMLFormElement>))

    expect(preventDefault).toHaveBeenCalled()
    expect(result.current.location.search).toBe('?q=coat')
  })
})
