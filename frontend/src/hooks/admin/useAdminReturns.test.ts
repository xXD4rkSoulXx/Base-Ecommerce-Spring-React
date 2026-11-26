import { describe, expect, it } from 'vitest'
import { useAdminReturns } from '@/hooks/admin/useAdminReturns'
import { captureRequestParams } from '@/test/requests'

const requestFor = (entry: string) => captureRequestParams('/api/admin/returns', useAdminReturns, entry)

describe('useAdminReturns', () => {
  it('asks for the pending returns by default', async () => {
    expect(await requestFor('/')).toEqual({ pending: 'true', page: '0', size: '10' })
  })

  it('asks for every return with the ALL filter', async () => {
    expect(await requestFor('/?status=ALL')).toEqual({ pending: 'false', page: '0', size: '10' })
  })

  it('asks for a single status', async () => {
    expect(await requestFor('/?status=REFUNDED')).toEqual({
      pending: 'false',
      status: 'REFUNDED',
      page: '0',
      size: '10'
    })
  })

  it('turns the page into a zero-based index', async () => {
    expect(await requestFor('/?page=3')).toEqual({ pending: 'true', page: '2', size: '10' })
  })
})
