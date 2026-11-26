import { describe, expect, it } from 'vitest'
import { useAdminReviews } from '@/hooks/admin/useAdminReviews'
import { captureRequestParams } from '@/test/requests'

const requestFor = (entry: string) => captureRequestParams('/api/admin/reviews', useAdminReviews, entry)

describe('useAdminReviews', () => {
  it('does not filter by visibility when the URL has no hidden param', async () => {
    expect(await requestFor('/')).toEqual({ page: '0', size: '10' })
  })

  it('asks only for the hidden reviews', async () => {
    expect(await requestFor('/?hidden=true')).toEqual({ hidden: 'true', page: '0', size: '10' })
  })

  it('asks only for the visible reviews', async () => {
    expect(await requestFor('/?hidden=false')).toEqual({ hidden: 'false', page: '0', size: '10' })
  })

  it('sends the search text and turns the page into a zero-based index', async () => {
    expect(await requestFor('/?q=ana&page=2')).toEqual({ q: 'ana', page: '1', size: '10' })
  })
})
