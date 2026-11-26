import { hashKey, QueryClient } from '@tanstack/react-query'
import { describe, expect, it } from 'vitest'
import { queryKeys } from '@/queryKeys'

const params = { page: 1 }

const wasInvalidated = async (invalidateWith: readonly unknown[], cachedKey: readonly unknown[]) => {
  const client = new QueryClient()

  client.setQueryData(cachedKey, [])
  await client.invalidateQueries({ queryKey: invalidateWith })

  return client.getQueryState(cachedKey)?.isInvalidated
}

describe('queryKeys invalidation', () => {
  it.each([
    ['orders', queryKeys.orders.all, queryKeys.orders.list(params)],
    ['admin.auditLogs', ['admin', 'audit-logs'], queryKeys.admin.auditLogs(params)],
    ['admin.customerOrders', queryKeys.admin.customerOrders.all, queryKeys.admin.customerOrders.list(1, params)],
    ['admin.customers', queryKeys.admin.customers.all, queryKeys.admin.customers.list(params)],
    ['admin.dashboard', queryKeys.admin.dashboard.all, queryKeys.admin.dashboard.query('summary', 7)],
    ['admin.discountCodes', queryKeys.admin.discountCodes.all, queryKeys.admin.discountCodes.list(params)],
    ['admin.order', queryKeys.admin.order.all, queryKeys.admin.order.detail(1)],
    ['admin.orders', queryKeys.admin.orders.all, queryKeys.admin.orders.list(params)],
    ['admin.products', queryKeys.admin.products.all, queryKeys.admin.products.list(params)],
    ['admin.productSkus', queryKeys.admin.productSkus.all, queryKeys.admin.productSkus.byProduct(1)],
    ['admin.returns', queryKeys.admin.returns.all, queryKeys.admin.returns.list(params)],
    ['admin.reviews', queryKeys.admin.reviews.all, queryKeys.admin.reviews.list(params)],
    ['admin.stock', queryKeys.admin.stock.all, queryKeys.admin.stock.list(params)]
  ])('invalidating %s also invalidates its more specific keys', async (_, all, specific) => {
    expect(await wasInvalidated(all, specific)).toBe(true)
  })

  it.each([
    ['orders.all', queryKeys.orders.all, queryKeys.admin.orders.list(params)],
    ['admin.order.all', queryKeys.admin.order.all, queryKeys.admin.orders.list(params)],
    ['admin.orders.all', queryKeys.admin.orders.all, queryKeys.admin.order.detail(1)],
    ['admin.customers.all', queryKeys.admin.customers.all, queryKeys.admin.customer(1)],
    ['admin.customers.all', queryKeys.admin.customers.all, queryKeys.admin.customerOrders.list(1, params)],
    ['admin.productSkus.all', queryKeys.admin.productSkus.all, queryKeys.admin.productSkuImages.byProduct(1)],
    ['admin.products.all', queryKeys.admin.products.all, queryKeys.admin.product(1)],
    ['products', ['products'], queryKeys.productsSearch(params)]
  ])('invalidating %s does not touch unrelated keys', async (_, invalidateWith, cachedKey) => {
    expect(await wasInvalidated(invalidateWith, cachedKey)).toBe(false)
  })

  it('invalidating the images of a product also invalidates the images of its SKUs', async () => {
    const images = queryKeys.admin.productSkuImages

    expect(await wasInvalidated(images.byProduct(1), images.bySku(1, 2))).toBe(true)
  })

  it('does not invalidate the images of another product', async () => {
    const images = queryKeys.admin.productSkuImages

    expect(await wasInvalidated(images.byProduct(2), images.bySku(1, 2))).toBe(false)
  })
})

describe('queryKeys cache identity', () => {
  it('gives every key a different cache entry', () => {
    const keys = [
      queryKeys.bestSellers,
      queryKeys.cart,
      queryKeys.dealProducts,
      queryKeys.featuredProducts,
      queryKeys.newArrivals,
      queryKeys.profile,
      queryKeys.topRated,
      queryKeys.wishlist,
      queryKeys.order(1),
      queryKeys.orders.all,
      queryKeys.orders.list(params),
      queryKeys.product('1'),
      queryKeys.productRelated('1'),
      queryKeys.productReviews('1'),
      queryKeys.products(params),
      queryKeys.productsSearch(params),
      queryKeys.reviewEligibility('1'),
      queryKeys.admin.auditLogs(params),
      queryKeys.admin.customer(1),
      queryKeys.admin.customerOrders.list(1, params),
      queryKeys.admin.customers.list(params),
      queryKeys.admin.dashboard.query('summary', 7),
      queryKeys.admin.discountCodes.list(params),
      queryKeys.admin.order.detail(1),
      queryKeys.admin.orders.list(params),
      queryKeys.admin.product(1),
      queryKeys.admin.products.list(params),
      queryKeys.admin.productSkuImages.byProduct(1),
      queryKeys.admin.productSkuImages.bySku(1, 1),
      queryKeys.admin.productSkus.byProduct(1),
      queryKeys.admin.returns.list(params),
      queryKeys.admin.reviews.list(params),
      queryKeys.admin.stock.list(params),
      queryKeys.admin.team
    ]

    expect(new Set(keys.map((key) => hashKey(key))).size).toBe(keys.length)
  })

  it('separates entries by params', () => {
    expect(hashKey(queryKeys.products({ page: 1 }))).not.toBe(hashKey(queryKeys.products({ page: 2 })))
  })

  it('ignores the order of the properties in the params', () => {
    expect(hashKey(queryKeys.products({ page: 1, size: 12 }))).toBe(hashKey(queryKeys.products({ size: 12, page: 1 })))
  })

  it('separates the dashboard entries by period', () => {
    expect(hashKey(queryKeys.admin.dashboard.query('summary', 7))).not.toBe(
      hashKey(queryKeys.admin.dashboard.query('summary', 30))
    )
  })
})
