import { describe, expect, it } from 'vitest'
import { useAdminDiscountCodes } from '@/hooks/admin/useAdminDiscountCodes'
import { captureRequestParams } from '@/test/requests'

const requestFor = (entry: string) => captureRequestParams('/api/admin/discount-codes', useAdminDiscountCodes, entry)

describe('useAdminDiscountCodes', () => {
  it('does not filter by status when the URL has no active param', async () => {
    expect(await requestFor('/')).toEqual({ page: '0', size: '10' })
  })

  it('asks only for the active codes', async () => {
    expect(await requestFor('/?active=true')).toEqual({ active: 'true', page: '0', size: '10' })
  })

  it('asks only for the inactive codes', async () => {
    expect(await requestFor('/?active=false')).toEqual({ active: 'false', page: '0', size: '10' })
  })

  it('sends the search text and turns the page into a zero-based index', async () => {
    expect(await requestFor('/?q=summer&page=2')).toEqual({ q: 'summer', page: '1', size: '10' })
  })
})
