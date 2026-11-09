import type { AdminAuditLogResponse } from '@/types/admin/adminAuditLog'
import type { AdminTeamMemberResponse } from '@/types/admin/adminTeam'
import type { DemoCustomer, DemoDiscountCode, DemoOrder, DemoProduct, DemoReturn, DemoReview } from '@/demo/types'
import { buildAuditLogs } from '@/demo/data/auditLogs'
import { buildCatalog } from '@/demo/data/catalog'
import { buildDiscountCodes } from '@/demo/data/discountCodes'
import { buildOrders } from '@/demo/data/orders'
import { buildCustomers, buildTeam } from '@/demo/data/people'
import { createRandom } from '@/demo/data/random'
import { buildReviews } from '@/demo/data/reviews'

const SEED = 20260101

export interface DemoFixtures {
  products: DemoProduct[]
  customers: DemoCustomer[]
  orders: DemoOrder[]
  returns: DemoReturn[]
  reviews: DemoReview[]
  discountCodes: DemoDiscountCode[]
  team: AdminTeamMemberResponse[]
  auditLogs: AdminAuditLogResponse[]
}

const build = (): DemoFixtures => {
  const now = Date.now()
  const rng = createRandom(SEED)

  const products = buildCatalog()
  const customers = buildCustomers(now, rng)
  const discountCodes = buildDiscountCodes(now)
  const { orders, returns } = buildOrders(now, rng, products, customers, discountCodes)
  const reviews = buildReviews(now, rng, products, customers)
  const team = buildTeam(now)
  const auditLogs = buildAuditLogs(now, rng, { products, orders, returns, reviews, discountCodes, team })

  return { products, customers, orders, returns, reviews, discountCodes, team, auditLogs }
}

let fixtures: DemoFixtures | undefined

export const getFixtures = () => {
  fixtures ??= build()

  return fixtures
}
