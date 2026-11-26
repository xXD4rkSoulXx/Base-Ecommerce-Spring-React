import { describe, expect, it } from 'vitest'
import { useAdminOrders } from '@/hooks/admin/useAdminOrders'
import { captureRequestParams } from '@/test/requests'

const requestFor = (entry: string) => captureRequestParams('/api/admin/orders', useAdminOrders, entry)

describe('useAdminOrders', () => {
  it('asks for the pending orders by default', async () => {
    expect(await requestFor('/')).toEqual({ pending: 'true', page: '0', size: '10' })
  })

  it('asks for every order with the ALL filter', async () => {
    expect(await requestFor('/?status=ALL')).toEqual({ pending: 'false', page: '0', size: '10' })
  })

  it('asks for a single status', async () => {
    expect(await requestFor('/?status=SHIPPED')).toEqual({
      pending: 'false',
      status: 'SHIPPED',
      page: '0',
      size: '10'
    })
  })

  it('sends the search text and turns the page into a zero-based index', async () => {
    expect(await requestFor('/?q=ana&page=2')).toEqual({ q: 'ana', pending: 'true', page: '1', size: '10' })
  })
})
