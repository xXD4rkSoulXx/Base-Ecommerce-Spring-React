import { renderHook, waitFor, act } from '@testing-library/react'
import { useLocation } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { usePagination } from '@/hooks/usePagination'
import { createRouterWrapper } from '@/test/render'

const renderPagination = (entry: string, totalPages: number) =>
  renderHook(() => ({ pagination: usePagination(totalPages), location: useLocation() }), {
    wrapper: createRouterWrapper([entry])
  })

describe('usePagination', () => {
  it('reads the current page from the URL', () => {
    const { result } = renderPagination('/?page=2', 5)

    expect(result.current.pagination.currentPage).toBe(2)
  })

  it('goes to another page and keeps the other params', () => {
    const { result } = renderPagination('/?q=jacket&page=1', 5)

    act(() => result.current.pagination.changePage(3))

    expect(result.current.location.search).toBe('?q=jacket&page=3')
    expect(result.current.pagination.currentPage).toBe(3)
  })

  it.each([0, 6])('ignores the page %s, which is out of range', (page) => {
    const { result } = renderPagination('/?page=2', 5)

    act(() => result.current.pagination.changePage(page))

    expect(result.current.location.search).toBe('?page=2')
  })

  it('moves to the last page when the URL points beyond it', async () => {
    const { result } = renderPagination('/?page=9', 5)

    await waitFor(() => expect(result.current.location.search).toBe('?page=5'))

    expect(result.current.pagination.currentPage).toBe(5)
  })

  it('leaves the URL alone while the total is not known yet', () => {
    const { result } = renderPagination('/?page=9', 0)

    expect(result.current.location.search).toBe('?page=9')
  })
})
