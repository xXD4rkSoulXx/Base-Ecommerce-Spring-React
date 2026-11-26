import { renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { LIST_PAGE_SIZE } from '@/constants'
import { usePageParams } from '@/hooks/usePageParams'
import { createRouterWrapper } from '@/test/render'

const renderParams = (entry: string, defaultSize?: number) =>
  renderHook(() => usePageParams(defaultSize), { wrapper: createRouterWrapper([entry]) })

describe('usePageParams', () => {
  it('uses page 1 and the default size when the URL has no params', () => {
    const { result } = renderParams('/')

    expect(result.current).toEqual({ page: 1, size: LIST_PAGE_SIZE })
  })

  it('reads the page and the size from the URL', () => {
    const { result } = renderParams('/?page=3&size=25')

    expect(result.current).toEqual({ page: 3, size: 25 })
  })

  it.each(['abc', '0', '-2', ''])('falls back to page 1 for the invalid page %j', (page) => {
    const { result } = renderParams(`/?page=${page}`)

    expect(result.current.page).toBe(1)
  })

  it('falls back to the default size when the size is invalid', () => {
    const { result } = renderParams('/?size=abc')

    expect(result.current.size).toBe(LIST_PAGE_SIZE)
  })

  it('uses the default size it was given', () => {
    const { result } = renderParams('/', 12)

    expect(result.current.size).toBe(12)
  })
})
