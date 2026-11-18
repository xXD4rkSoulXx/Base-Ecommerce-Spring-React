import { http, HttpResponse, type HttpHandler } from 'msw'
import type { AdminAuditLogResponse } from '@/types/admin/adminAuditLog'
import type { AdminDiscountCodeResponse } from '@/types/admin/adminDiscountCode'
import type { AdminReviewResponse } from '@/types/admin/adminReview'
import { getFixtures } from '@/demo/data/fixtures'
import { toIso } from '@/demo/data/random'
import { matchesQuery, paginate, readBoolean } from '@/demo/handlers/support'

export const communityHandlers: HttpHandler[] = [
  http.get('*/api/admin/reviews', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    const hidden = readBoolean(url, 'hidden')

    const reviews: AdminReviewResponse[] = getFixtures()
      .reviews.filter((review) => matchesQuery(q, review.productName, review.productRef, review.customerName, review.customerEmail, review.title))
      .filter((review) => hidden === undefined || review.hidden === hidden)
      .sort((a, b) => b.createdAt - a.createdAt)
      .map((review) => ({ ...review, createdAt: toIso(review.createdAt) }))

    return HttpResponse.json(paginate(reviews, url))
  }),

  http.get('*/api/admin/discount-codes', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    const active = readBoolean(url, 'active')
    const { discountCodes, orders } = getFixtures()

    const codes: AdminDiscountCodeResponse[] = discountCodes
      .filter((code) => matchesQuery(q, code.code))
      .filter((code) => active === undefined || code.active === active)
      .sort((a, b) => b.createdAt - a.createdAt)
      .map((code) => ({
        id: code.id,
        code: code.code,
        discountPercentage: code.discountPercentage,
        active: code.active,
        orders: orders.filter((order) => order.discountCode === code.code).length,
        createdAt: toIso(code.createdAt)
      }))

    return HttpResponse.json(paginate(codes, url))
  }),

  http.get('*/api/admin/team', () => HttpResponse.json(getFixtures().team)),

  http.get('*/api/admin/audit-logs', ({ request }) => {
    const url = new URL(request.url)
    const q = url.searchParams.get('q')
    const entityType = url.searchParams.get('entityType')
    const action = url.searchParams.get('action')

    const logs: AdminAuditLogResponse[] = getFixtures()
      .auditLogs.filter((log) => matchesQuery(q, log.actorEmail, log.entityLabel, log.details))
      .filter((log) => !entityType || log.entityType === entityType)
      .filter((log) => !action || log.action === action)
      .sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt))

    return HttpResponse.json(paginate(logs, url))
  })
]
