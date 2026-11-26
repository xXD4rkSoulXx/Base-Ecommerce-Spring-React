import { renderHook } from '@testing-library/react'
import { beforeEach, describe, expect, it } from 'vitest'
import { APP_NAME } from '@/constants'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'

describe('useDocumentTitle', () => {
  beforeEach(() => {
    document.title = 'initial'
  })

  it('sets the title followed by the app name', () => {
    renderHook(() => useDocumentTitle('Cart'))

    expect(document.title).toBe(`Cart - ${APP_NAME}`)
  })

  it('leaves the title alone when there is none', () => {
    renderHook(() => useDocumentTitle(undefined))

    expect(document.title).toBe('initial')
  })

  it('updates the title when it changes', () => {
    const { rerender } = renderHook(({ title }) => useDocumentTitle(title), { initialProps: { title: 'Cart' } })

    rerender({ title: 'Orders' })

    expect(document.title).toBe(`Orders - ${APP_NAME}`)
  })
})
