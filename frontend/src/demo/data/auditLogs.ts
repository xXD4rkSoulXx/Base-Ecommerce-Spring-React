import type { AdminAuditLogResponse, AuditAction, AuditEntityType } from '@/types/admin/adminAuditLog'
import type { AdminTeamMemberResponse } from '@/types/admin/adminTeam'
import type { DemoDiscountCode, DemoOrder, DemoProduct, DemoReturn, DemoReview } from '@/demo/types'
import { sizeLabel } from '@/demo/data/catalog'
import { DAY, toIso, type Random } from '@/demo/data/random'

interface AuditSource {
  products: DemoProduct[]
  orders: DemoOrder[]
  returns: DemoReturn[]
  reviews: DemoReview[]
  discountCodes: DemoDiscountCode[]
  team: AdminTeamMemberResponse[]
}

interface AuditEntry {
  action: AuditAction
  entityType: AuditEntityType
  entityId: number
  entityLabel: string
  details: string | null
}

const REPETITIONS = 3
const WINDOW_IN_DAYS = 25

export const buildAuditLogs = (now: number, rng: Random, source: AuditSource): AdminAuditLogResponse[] => {
  const staff = source.team.filter((member) => member.role !== 'DEMO')
  const skus = source.products.flatMap((product) => product.skus.map((sku) => ({ product, sku })))
  const shipments = source.orders.flatMap((order) => order.details.shipments.map((shipment) => ({ order, shipment })))

  const product = () => rng.pick(source.products)
  const variant = () => rng.pick(skus)
  const order = () => rng.pick(source.orders)
  const orderReturn = () => rng.pick(source.returns).response
  const discountCode = () => rng.pick(source.discountCodes)
  const review = () => rng.pick(source.reviews)

  const productEntry = (action: AuditAction, details: string | null): AuditEntry => {
    const target = product()

    return { action, entityType: 'PRODUCT', entityId: target.id, entityLabel: target.ref, details }
  }

  const variantEntry = (action: AuditAction, details: string | null): AuditEntry => {
    const { product: target, sku } = variant()

    return {
      action,
      entityType: 'VARIANT',
      entityId: sku.id,
      entityLabel: `${target.ref} · ${sizeLabel(sku.size)} ${sku.color}`,
      details
    }
  }

  const returnEntry = (action: AuditAction, details: string | null): AuditEntry => {
    const target = orderReturn()

    return { action, entityType: 'RETURN', entityId: target.id, entityLabel: `#${target.id}`, details }
  }

  const codeEntry = (action: AuditAction, details: string | null): AuditEntry => {
    const target = discountCode()

    return { action, entityType: 'DISCOUNT_CODE', entityId: target.id, entityLabel: target.code, details }
  }

  const reviewEntry = (action: AuditAction): AuditEntry => {
    const target = review()

    return { action, entityType: 'REVIEW', entityId: target.id, entityLabel: `#${target.id}`, details: null }
  }

  const templates: Array<() => AuditEntry> = [
    () => productEntry('CREATED', null),
    () => productEntry('UPDATED', `Price ${rng.int(30, 90)}.00 → ${rng.int(91, 160)}.90`),
    () => productEntry('ARCHIVED', null),
    () => productEntry('RESTORED', null),
    () => variantEntry('CREATED', null),
    () => variantEntry('UPDATED', `Discount ${rng.int(0, 10)}% → ${rng.int(11, 30)}%`),
    () => variantEntry('ARCHIVED', null),
    () => variantEntry('RESTORED', null),
    () => {
      const delta = rng.int(1, 25)

      return variantEntry('STOCK_ADJUSTED', `+${delta} (stock now ${delta + rng.int(0, 30)})`)
    },
    () => variantEntry('STOCK_SET', `Set to ${rng.int(0, 60)}`),
    () => variantEntry('IMAGE_UPLOADED', null),
    () => variantEntry('IMAGE_DELETED', null),
    () => variantEntry('COVER_CHANGED', null),
    () => {
      const target = order()

      return {
        action: 'STATUS_CHANGED',
        entityType: 'ORDER',
        entityId: target.id,
        entityLabel: `#${target.id}`,
        details: rng.pick(['PAID → PROCESSING', 'PROCESSING → SHIPPED', 'SHIPPED → DELIVERED'])
      }
    },
    () => {
      const { order: target, shipment } = rng.pick(shipments)

      return {
        action: 'SHIPMENT_STATUS_CHANGED',
        entityType: 'ORDER',
        entityId: target.id,
        entityLabel: `#${target.id}`,
        details: `Parcel ${shipment.id}: ${rng.pick(['PRE_TRANSIT → IN_TRANSIT', 'IN_TRANSIT → OUT_FOR_DELIVERY', 'OUT_FOR_DELIVERY → DELIVERED'])}`
      }
    },
    () => returnEntry('APPROVED', null),
    () => returnEntry('REJECTED', rng.pick(['Item shows signs of wear and was washed.', 'Outside of the allowed conditions.'])),
    () => returnEntry('RECEIVED', null),
    () => returnEntry('REFUNDED', null),
    () => codeEntry('CREATED', `${rng.int(5, 30)}%`),
    () => codeEntry('UPDATED', `${rng.int(5, 15)}% → ${rng.int(16, 30)}%`),
    () => codeEntry('ACTIVATED', null),
    () => codeEntry('DEACTIVATED', null),
    () => reviewEntry('HIDDEN'),
    () => reviewEntry('SHOWN'),
    () => {
      const target = rng.pick(staff)

      return {
        action: 'ROLE_CHANGED',
        entityType: 'TEAM_MEMBER',
        entityId: target.id,
        entityLabel: target.email,
        details: rng.pick(['FULFILLMENT → SUPPORT', 'SUPPORT → MERCHANDISER', 'MERCHANDISER → MANAGER'])
      }
    }
  ]

  return Array.from({ length: templates.length * REPETITIONS }, (_, index) => ({
    entry: templates[index % templates.length](),
    createdAt: now - (0.02 + Math.pow(rng.next(), 1.2) * WINDOW_IN_DAYS) * DAY,
    actorEmail: rng.pick(staff).email
  }))
    .sort((a, b) => a.createdAt - b.createdAt)
    .map(({ entry, createdAt, actorEmail }, index) => ({
      id: index + 1,
      actorEmail,
      ...entry,
      createdAt: toIso(createdAt)
    }))
}
