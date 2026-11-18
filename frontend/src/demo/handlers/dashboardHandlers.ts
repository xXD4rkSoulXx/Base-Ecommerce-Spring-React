import { http, HttpResponse, type HttpHandler } from 'msw'
import type { AdminDashboardEndpoints } from '@/types/admin/adminDashboard'
import type { OrderStatus } from '@/types/order'
import type { DemoOrder } from '@/demo/types'
import { getProductCover } from '@/demo/data/catalog'
import { DAY, HOUR, roundMoney } from '@/demo/data/random'
import { getFixtures } from '@/demo/data/fixtures'
import { LOW_STOCK_THRESHOLD } from '@/demo/handlers/catalogHandlers'
import { PENDING_ORDER_STATUSES, PENDING_RETURN_STATUSES } from '@/demo/handlers/salesHandlers'
import { notFound } from '@/demo/handlers/support'

const LIST_LIMIT = 5
const DEFAULT_DAYS = 30
const MAX_DAYS = 365

const STATUS_ORDER: OrderStatus[] = ['PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'RETURN_REQUESTED', 'REFUNDED']

interface Period {
  start: number
  end: number
  previousStart: number
}

const readDays = (url: URL) =>
  Math.min(Math.max(Number(url.searchParams.get('days')) || DEFAULT_DAYS, 1), MAX_DAYS)

const periodOf = (days: number): Period => {
  const tomorrow = new Date()
  tomorrow.setHours(24, 0, 0, 0)

  const end = tomorrow.getTime()
  const start = end - days * DAY

  return { start, end, previousStart: start - days * DAY }
}

const isWithin = (timestamp: number, from: number, to: number) => timestamp >= from && timestamp < to

const pad = (value: number) => String(value).padStart(2, '0')

const dateKey = (timestamp: number) => {
  const date = new Date(timestamp)

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const ordersBetween = (from: number, to: number) =>
  getFixtures().orders.filter((order) => isWithin(order.createdAt, from, to))

const refundsBetween = (from: number, to: number) =>
  getFixtures().returns.filter((item) => item.refundedAt !== undefined && isWithin(item.refundedAt, from, to))

const sum = (values: number[]) => roundMoney(values.reduce((total, value) => total + value, 0))

const totals = (from: number, to: number) => {
  const orders = ordersBetween(from, to)
  const gross = sum(orders.map((order) => order.details.totalAmount))
  const refunded = sum(refundsBetween(from, to).map((item) => item.response.refundAmount ?? 0))

  return {
    netRevenue: roundMoney(gross - refunded),
    refunded,
    orders: orders.length,
    averageOrderValue: orders.length > 0 ? roundMoney(gross / orders.length) : 0
  }
}

const groupUnits = <K extends string | number>(orders: DemoOrder[], keyOf: (productId: number) => K) => {
  const units = new Map<K, number>()

  orders.forEach((order) =>
    order.lines.forEach((line) => {
      const key = keyOf(line.productId)

      units.set(key, (units.get(key) ?? 0) + line.quantity)
    })
  )

  return [...units.entries()].sort((a, b) => b[1] - a[1])
}

const lowStockSkus = () =>
  getFixtures()
    .products.filter((product) => product.active)
    .flatMap((product) => product.skus.filter((sku) => sku.active && sku.stock <= LOW_STOCK_THRESHOLD).map((sku) => ({ product, sku })))
    .sort((a, b) => a.sku.stock - b.sku.stock || a.product.name.localeCompare(b.product.name))

const pendingReturns = () =>
  getFixtures().returns.filter((item) => PENDING_RETURN_STATUSES.includes(item.response.status))

type EndpointResolvers = {
  [E in keyof AdminDashboardEndpoints]: (days: number) => AdminDashboardEndpoints[E]
}

const resolvers: EndpointResolvers = {
  summary: (days) => {
    const { start, end, previousStart } = periodOf(days)
    const current = totals(start, end)
    const previous = totals(previousStart, start)

    return {
      netRevenue: current.netRevenue,
      previousNetRevenue: previous.netRevenue,
      refunded: current.refunded,
      previousRefunded: previous.refunded,
      orders: current.orders,
      previousOrders: previous.orders,
      averageOrderValue: current.averageOrderValue,
      previousAverageOrderValue: previous.averageOrderValue
    }
  },

  sales: (days) => {
    const { start, end } = periodOf(days)
    const points = new Map(
      Array.from({ length: days }, (_, index) => [
        dateKey(start + index * DAY + 12 * HOUR),
        { date: dateKey(start + index * DAY + 12 * HOUR), sales: 0, refunds: 0, orders: 0 }
      ])
    )

    ordersBetween(start, end).forEach((order) => {
      const point = points.get(dateKey(order.createdAt))

      if (point) {
        point.sales = roundMoney(point.sales + order.details.totalAmount)
        point.orders += 1
      }
    })

    refundsBetween(start, end).forEach((item) => {
      const point = points.get(dateKey(item.refundedAt!))

      if (point) {
        point.refunds = roundMoney(point.refunds + (item.response.refundAmount ?? 0))
      }
    })

    return [...points.values()]
  },

  'orders-by-status': (days) => {
    const { start, end } = periodOf(days)
    const orders = ordersBetween(start, end)

    return STATUS_ORDER.map((status) => ({
      status,
      count: orders.filter((order) => order.status === status).length
    })).filter((entry) => entry.count > 0)
  },

  'sales-by-category': (days) => {
    const { start, end } = periodOf(days)
    const productsById = new Map(getFixtures().products.map((product) => [product.id, product]))

    return groupUnits(ordersBetween(start, end), (productId) => productsById.get(productId)!.category).map(
      ([category, unitsSold]) => ({ category, unitsSold })
    )
  },

  'recent-orders': () =>
    [...getFixtures().orders]
      .sort((a, b) => b.createdAt - a.createdAt)
      .slice(0, LIST_LIMIT)
      .map((order) => ({
        id: order.id,
        customerName: order.customerName,
        status: order.status,
        totalAmount: order.details.totalAmount,
        createdAt: order.details.createdAt
      })),

  'top-products': (days) => {
    const { start, end } = periodOf(days)
    const productsById = new Map(getFixtures().products.map((product) => [product.id, product]))

    return groupUnits(ordersBetween(start, end), (productId) => productId)
      .slice(0, LIST_LIMIT)
      .map(([productId, unitsSold]) => {
        const product = productsById.get(productId)!

        return {
          productId,
          ref: product.ref,
          name: product.name,
          cover: getProductCover(product),
          unitsSold
        }
      })
  },

  'top-discount-codes': (days) => {
    const { start, end } = periodOf(days)
    const counts = new Map<string, number>()

    ordersBetween(start, end).forEach((order) => {
      if (order.discountCode) {
        counts.set(order.discountCode, (counts.get(order.discountCode) ?? 0) + 1)
      }
    })

    return [...counts.entries()]
      .sort((a, b) => b[1] - a[1])
      .slice(0, LIST_LIMIT)
      .map(([code, orders]) => ({ code, orders }))
  },

  'pending-orders': () => ({
    count: getFixtures().orders.filter((order) => PENDING_ORDER_STATUSES.includes(order.status)).length
  }),

  'pending-returns': () => ({ count: pendingReturns().length }),

  'low-stock': () => ({ count: lowStockSkus().length }),

  'returns-to-handle': () => {
    const { orders } = getFixtures()

    return pendingReturns()
      .sort((a, b) => a.createdAt - b.createdAt)
      .slice(0, LIST_LIMIT)
      .map((item) => ({
        id: item.response.id,
        orderId: item.response.orderId,
        customerName: orders.find((order) => order.id === item.response.orderId)?.customerName ?? item.response.customerEmail,
        status: item.response.status,
        createdAt: item.response.createdAt
      }))
  },

  'lowest-stock': () =>
    lowStockSkus()
      .slice(0, LIST_LIMIT)
      .map(({ product, sku }) => ({
        skuId: sku.id,
        productId: product.id,
        productName: product.name,
        ref: product.ref,
        size: sku.size,
        color: sku.color,
        stock: sku.stock
      })),

  customers: (days) => {
    const { start, end, previousStart } = periodOf(days)
    const { customers } = getFixtures()

    return {
      newCustomers: customers.filter((customer) => isWithin(customer.createdAt, start, end)).length,
      previousNewCustomers: customers.filter((customer) => isWithin(customer.createdAt, previousStart, start)).length,
      totalCustomers: customers.length
    }
  },

  reviews: (days) => {
    const { start, end, previousStart } = periodOf(days)
    const { reviews } = getFixtures()

    const average = (from: number, to: number) => {
      const ratings = reviews.filter((review) => isWithin(review.createdAt, from, to)).map((review) => review.rating)

      return ratings.length > 0 ? roundMoney(ratings.reduce((total, rating) => total + rating, 0) / ratings.length) : undefined
    }

    return {
      newReviews: reviews.filter((review) => isWithin(review.createdAt, start, end)).length,
      previousNewReviews: reviews.filter((review) => isWithin(review.createdAt, previousStart, start)).length,
      averageRating: average(start, end),
      previousAverageRating: average(previousStart, start)
    }
  },

  'recent-activity': () =>
    [...getFixtures().auditLogs].sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt)).slice(0, LIST_LIMIT)
}

const isEndpoint = (value: string): value is keyof AdminDashboardEndpoints => Object.hasOwn(resolvers, value)

export const dashboardHandlers: HttpHandler[] = [
  http.get('*/api/admin/dashboard/:endpoint', ({ params, request }) => {
    const endpoint = String(params.endpoint)

    if (!isEndpoint(endpoint)) {
      return notFound()
    }

    return HttpResponse.json(resolvers[endpoint](readDays(new URL(request.url))))
  })
]
