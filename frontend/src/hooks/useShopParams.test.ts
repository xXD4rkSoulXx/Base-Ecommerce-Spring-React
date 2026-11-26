import { renderHook } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ALL_FILTER, DEFAULT_SORT, PRODUCT_PAGE_SIZE } from '@/constants'
import { useShopParams } from '@/hooks/useShopParams'
import { createRouterWrapper } from '@/test/render'

const renderParams = (entry: string) => renderHook(() => useShopParams(), { wrapper: createRouterWrapper([entry]) })

describe('useShopParams', () => {
  it('uses all categories, the default sort and the product page size by default', () => {
    const { result } = renderParams('/')

    expect(result.current).toEqual({ category: ALL_FILTER, sortBy: DEFAULT_SORT, size: PRODUCT_PAGE_SIZE })
  })

  it('reads the category, the sort and the size from the URL', () => {
    const { result } = renderParams('/?category=TOPS&sort=price_asc&size=24')

    expect(result.current).toEqual({ category: 'TOPS', sortBy: 'price_asc', size: 24 })
  })
})
