import { describe, expect, it } from 'vitest'
import { useOrders } from '@/hooks/useOrders'
import { captureRequestParams } from '@/test/requests'

const requestFor = (entry: string) => captureRequestParams('/api/orders', useOrders, entry)

describe('useOrders', () => {
  it('asks for the first page with the default size when the URL has no params', async () => {
    expect(await requestFor('/')).toEqual({ page: '0', size: '10' })
  })

  it('sends the search text and turns the page into a zero-based index', async () => {
    expect(await requestFor('/?q=abc&page=3&size=5')).toEqual({ q: 'abc', page: '2', size: '5' })
  })

  it('turns the dates into the start and the end of the day', async () => {
    expect(await requestFor('/?from=2026-03-01&to=2026-03-05')).toEqual({
      from: '2026-03-01T00:00:00.000Z',
      to: '2026-03-05T23:59:59.999Z',
      page: '0',
      size: '10'
    })
  })

  it('ignores an invalid date', async () => {
    expect(await requestFor('/?from=abc&to=2026-13-45')).toEqual({ page: '0', size: '10' })
  })
})
