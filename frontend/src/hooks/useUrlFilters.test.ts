import { act, renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { useUrlFilters } from '@/hooks/useUrlFilters'
import { createRouterWrapper } from '@/test/render'

const renderFilters = (entry: string) => renderHook(() => useUrlFilters(), { wrapper: createRouterWrapper([entry]) })

describe('useUrlFilters', () => {
  it('sets the filters in the URL and keeps the other params', () => {
    const { result } = renderFilters('/?sort=recent')

    act(() => result.current.changeFilters({ category: 'TOPS', q: 'jacket' }))

    expect(result.current.searchParams.toString()).toBe('sort=recent&category=TOPS&q=jacket')
  })

  it('removes a filter when its value is empty', () => {
    const { result } = renderFilters('/?category=TOPS&q=jacket')

    act(() => result.current.changeFilters({ category: '' }))

    expect(result.current.searchParams.toString()).toBe('q=jacket')
  })

  it('goes back to the first page whenever a filter changes', () => {
    const { result } = renderFilters('/?category=TOPS&page=4')

    act(() => result.current.changeFilters({ category: 'BOTTOMS' }))

    expect(result.current.searchParams.toString()).toBe('category=BOTTOMS')
  })
})
