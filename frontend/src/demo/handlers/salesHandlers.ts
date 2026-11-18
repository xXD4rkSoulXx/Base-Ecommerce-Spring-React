import { http, HttpResponse, type HttpHandler } from 'msw'
import type { AdminCustomerResponse } from '@/types/admin/adminCustomer'
import type { AdminOrderDetailsResponse, AdminOrderSummaryResponse } from '@/types/admin/adminOrder'
import type { OrderStatus, OrderSummaryResponse, ReturnStatus } from '@/types/order'
import type { DemoCustomer, DemoOrder } from '@/demo/types'
import { getShipmentActions } from '@/demo/data/orders'
import { getFixtures } from '@/demo/data/fixtures'
import { toIso } from '@/demo/data/random'
import { matchesQuery, notFound, paginate, readBoolean, readId } from '@/demo/handlers/support'

export const PENDING_ORDER_STATUSES: OrderStatus[] = ['PAID', 'PROCESSING']
export const PENDING_RETURN_STATUSES: ReturnStatus[] = ['REQUESTED', 'APPROVED', 'RECEIVED']

const toOrderSummary = (order: DemoOrder): AdminOrderSummaryResponse => ({
  id: order.id,
  status: order.status,
  createdAt: order.details.createdAt,
  totalAmount: order.details.totalAmount,
  currency: order.details.currency,
  customerEmail: order.customerEmail,
  recipientName: order.details.recipientName
})

const toOrderDetails = (order: DemoOrder): AdminOrderDetailsResponse => ({
  customerEmail: order.customerEmail,
  customerName: order.customerName,
  nextStatus: order.status === 'PAID' ? 'PROCESSING' : undefined,
  shipmentActions: Object.fromEntries(
    order.details.shipments.map((shipment) => [shipment.id, getShipmentActions(shipment.status)])
  ),
  order: order.details
})

const toCustomer = (customer: DemoCustomer, orders: DemoOrder[]): AdminCustomerResponse => {
  const customerOrders = orders.filter((order) => order.customerId === customer.id)
  const lastOrder = customerOrders.reduce<DemoOrder | undefined>(
    (latest, order) => (!latest || order.createdAt > latest.createdAt ? order : latest),
    undefined
  )

  return {
    id: customer.id,
    email: customer.email,
    firstName: customer.firstName,
    lastName: customer.lastName,
    createdAt: toIso(customer.createdAt),
    orders: customerOrders.length,
    lastOrderAt: lastOrder?.details.createdAt
  }
}

export const salesHandlers: HttpHandler[] = [
  http.get('*/api/admin/orders', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    const status = url.searchParams.get('status') as OrderStatus | null
    const pending = readBoolean(url, 'pending')

    const orders = getFixtures()
      .orders.filter((order) => matchesQuery(q, order.id, order.customerEmail, order.details.recipientName))
      .filter((order) => !status || order.status === status)
      .filter((order) => !pending || PENDING_ORDER_STATUSES.includes(order.status))
      .sort((a, b) => b.createdAt - a.createdAt)
      .map(toOrderSummary)

    return HttpResponse.json(paginate(orders, url))
  }),

  http.get('*/api/admin/orders/:id', ({ params }) => {
    const order = getFixtures().orders.find((candidate) => candidate.id === readId(params.id))

    return order ? HttpResponse.json(toOrderDetails(order)) : notFound()
  }),

  http.get('*/api/admin/returns', ({ request }) => {
    const url = new URL(request.url)
    const status = url.searchParams.get('status') as ReturnStatus | null
    const pending = readBoolean(url, 'pending')

    const returns = getFixtures()
      .returns.filter((item) => !status || item.response.status === status)
      .filter((item) => !pending || PENDING_RETURN_STATUSES.includes(item.response.status))
      .sort((a, b) => b.createdAt - a.createdAt)
      .map((item) => item.response)

    return HttpResponse.json(paginate(returns, url))
  }),

  http.get('*/api/admin/customers', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    const { customers, orders } = getFixtures()

    const content = customers
      .filter((customer) => matchesQuery(q, customer.email, customer.firstName, customer.lastName, `${customer.firstName} ${customer.lastName}`))
      .sort((a, b) => b.createdAt - a.createdAt)
      .map((customer) => toCustomer(customer, orders))

    return HttpResponse.json(paginate(content, url))
  }),

  http.get('*/api/admin/customers/:id', ({ params }) => {
    const { customers, orders } = getFixtures()
    const customer = customers.find((candidate) => candidate.id === readId(params.id))

    return customer ? HttpResponse.json(toCustomer(customer, orders)) : notFound()
  }),

  http.get('*/api/admin/customers/:id/orders', ({ params, request }) => {
    const url = new URL(request.url)
    const id = readId(params.id)

    const orders: OrderSummaryResponse[] = getFixtures()
      .orders.filter((order) => order.customerId === id)
      .sort((a, b) => b.createdAt - a.createdAt)
      .map((order) => ({
        id: order.id,
        status: order.status,
        createdAt: order.details.createdAt,
        totalAmount: order.details.totalAmount,
        currency: order.details.currency
      }))

    return HttpResponse.json(paginate(orders, url))
  })
]
