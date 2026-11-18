import type { AdminReturnResponse } from '@/types/admin/adminReturn'
import type { OrderItemResponse, OrderReturnResponse, OrderShipmentResponse, OrderStatus, OrderStatusEventResponse, ReturnReason, ReturnStatus, ShipmentStatus, ShipmentTrackingEventResponse } from '@/types/order'
import type { DemoCustomer, DemoDiscountCode, DemoOrder, DemoOrderLine, DemoProduct, DemoReturn, DemoSku } from '@/demo/types'
import { getSkuCover } from '@/demo/data/catalog'
import { DAY, HOUR, roundMoney, toIso, type Random } from '@/demo/data/random'

const ORDER_COUNT = 130
const FIRST_ORDER_ID = 1001
const FIRST_RETURN_ID = 501
const MAX_ORDER_AGE_IN_DAYS = 119
const RETURN_WINDOW_IN_DAYS = 30
const FREE_SHIPPING_THRESHOLD = 100
const SHIPPING_COST = 4.9
const TAX_RATE = 0.23

const CARRIERS = ['DHL Express', 'UPS', 'CTT Expresso']
const SERVICES = ['Standard', 'Express']

const PENDING_RETURN_CYCLE: ReturnStatus[] = ['REQUESTED', 'APPROVED', 'RECEIVED', 'REQUESTED', 'APPROVED']

const RETURN_REASONS: ReturnReason[] = ['DOES_NOT_FIT', 'DEFECTIVE', 'NOT_AS_DESCRIBED', 'CHANGED_MIND', 'OTHER']

const RETURN_COMMENTS = [
  'Runs a size too small for me.',
  'The zipper broke after the first wear.',
  'Colour is different from the pictures.',
  'Bought two by mistake.',
  'Stitching came loose on the sleeve.'
]

const REJECTION_REASONS = [
  'Item shows signs of wear and was washed.',
  'Return requested outside of the allowed conditions.'
]

const SHIPMENT_STEPS: Record<ShipmentStatus, ShipmentStatus[]> = {
  PRE_TRANSIT: ['PRE_TRANSIT'],
  IN_TRANSIT: ['PRE_TRANSIT', 'IN_TRANSIT'],
  OUT_FOR_DELIVERY: ['PRE_TRANSIT', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'],
  DELIVERED: ['PRE_TRANSIT', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'DELIVERED'],
  FAILURE: ['PRE_TRANSIT', 'IN_TRANSIT', 'OUT_FOR_DELIVERY', 'FAILURE']
}

const STEP_HOURS = [26, 46, 70, 84]

const STEP_DESCRIPTIONS: Record<ShipmentStatus, string> = {
  PRE_TRANSIT: 'Shipping label created',
  IN_TRANSIT: 'Departed sorting facility',
  OUT_FOR_DELIVERY: 'Out for delivery',
  DELIVERED: 'Delivered to recipient',
  FAILURE: 'Delivery attempt failed'
}

const SHIPMENT_ACTIONS: Record<ShipmentStatus, ShipmentStatus[]> = {
  PRE_TRANSIT: ['IN_TRANSIT'],
  IN_TRANSIT: ['OUT_FOR_DELIVERY'],
  OUT_FOR_DELIVERY: ['FAILURE', 'DELIVERED'],
  DELIVERED: [],
  FAILURE: ['OUT_FOR_DELIVERY', 'DELIVERED']
}

export const getShipmentActions = (status: ShipmentStatus) => SHIPMENT_ACTIONS[status]

const chooseStatus = (rng: Random, ageInDays: number): OrderStatus => {
  if (ageInDays < 1) {
    return rng.chance(0.7) ? 'PAID' : 'PROCESSING'
  }

  if (ageInDays < 3) {
    const roll = rng.next()

    return roll < 0.15 ? 'PAID' : roll < 0.85 ? 'PROCESSING' : 'SHIPPED'
  }

  if (ageInDays < 5) {
    return rng.chance(0.1) ? 'PROCESSING' : 'SHIPPED'
  }

  if (ageInDays < 8) {
    return rng.chance(0.2) ? 'SHIPPED' : 'DELIVERED'
  }

  const roll = rng.next()

  if (ageInDays < 40 && roll < 0.17) {
    return 'RETURN_REQUESTED'
  }

  if (ageInDays >= 14 && ageInDays < 75 && roll >= 0.17 && roll < 0.25) {
    return 'REFUNDED'
  }

  return 'DELIVERED'
}

const chooseShipmentStatus = (rng: Random, orderStatus: OrderStatus): ShipmentStatus => {
  if (orderStatus === 'PROCESSING') {
    return 'PRE_TRANSIT'
  }

  if (orderStatus === 'SHIPPED') {
    const roll = rng.next()

    return roll < 0.5 ? 'IN_TRANSIT' : roll < 0.85 ? 'OUT_FOR_DELIVERY' : 'FAILURE'
  }

  return 'DELIVERED'
}

const pickSkus = (rng: Random, skus: DemoSku[]) => {
  const count = 1 + (rng.chance(0.45) ? 1 : 0) + (rng.chance(0.15) ? 1 : 0)
  const picked: DemoSku[] = []

  while (picked.length < count) {
    const candidate = rng.pick(skus)

    if (!picked.some((sku) => sku.productId === candidate.productId)) {
      picked.push(candidate)
    }
  }

  return picked
}

const trackingNumber = (rng: Random) => `VC${rng.int(100000000, 999999999)}PT`

export const buildOrders = (
  now: number,
  rng: Random,
  products: DemoProduct[],
  customers: DemoCustomer[],
  discountCodes: DemoDiscountCode[]
) => {
  const productsById = new Map(products.map((product) => [product.id, product]))
  const sellableSkus = products.filter((product) => product.active).flatMap((product) => product.skus.filter((sku) => sku.active))
  const usableCodes = discountCodes.filter((code) => code.code !== 'NEWYEAR12')

  const ages = [
    ...Array.from({ length: 6 }, () => rng.next() * 0.9 + 0.05),
    ...Array.from({ length: ORDER_COUNT - 6 }, () => 0.1 + Math.pow(rng.next(), 1.35) * MAX_ORDER_AGE_IN_DAYS)
  ].sort((a, b) => b - a)

  let nextItemId = 1
  let nextShipmentId = 1
  let nextReturnId = FIRST_RETURN_ID
  let pendingReturnIndex = 0

  const orders: DemoOrder[] = []
  const returns: DemoReturn[] = []

  ages.forEach((ageInDays, index) => {
    const id = FIRST_ORDER_ID + index
    const createdAt = now - ageInDays * DAY
    const status = chooseStatus(rng, ageInDays)
    const customer = rng.pick(customers)
    const at = (hours: number) => Math.min(createdAt + hours * HOUR, now - 5 * 60_000)

    const lines: DemoOrderLine[] = []
    const items: OrderItemResponse[] = pickSkus(rng, sellableSkus).map((sku) => {
      const product = productsById.get(sku.productId)!
      const quantity = rng.chance(0.2) ? 2 : rng.chance(0.05) ? 3 : 1
      const discount = Math.max(product.discountPercentage, sku.discountPercentage)
      const unitPrice = roundMoney(product.price * (1 - discount / 100))

      lines.push({ productId: product.id, skuId: sku.id, quantity })

      return {
        id: nextItemId++,
        ref: product.ref,
        productName: product.name,
        color: sku.color,
        size: sku.size,
        imagePath: getSkuCover(sku),
        quantity,
        unitPrice,
        lineTotal: roundMoney(unitPrice * quantity),
        returnableQuantity: 0
      }
    })

    const subtotal = roundMoney(items.reduce((sum, item) => sum + item.lineTotal, 0))
    const code = rng.chance(0.3) ? rng.pick(usableCodes) : undefined
    const discountAmount = code ? roundMoney((subtotal * code.discountPercentage) / 100) : 0
    const shippingCost = subtotal - discountAmount >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST
    const totalAmount = roundMoney(subtotal - discountAmount + shippingCost)
    const taxAmount = roundMoney((totalAmount * TAX_RATE) / (1 + TAX_RATE))

    const hasShipment = status !== 'PAID'
    const shipments: OrderShipmentResponse[] = []

    if (hasShipment) {
      const split = items.length >= 2 && rng.chance(0.3)
      const groups = split ? [[items[0]], items.slice(1)] : [items]

      groups.forEach((group, groupIndex) => {
        const shipmentStatus =
          status === 'SHIPPED' && split && groupIndex === 0 && ageInDays >= 3.5
            ? 'DELIVERED'
            : chooseShipmentStatus(rng, status)

        const steps = SHIPMENT_STEPS[shipmentStatus]
        const events: ShipmentTrackingEventResponse[] = steps.map((step, stepIndex) => ({
          status: step,
          description: STEP_DESCRIPTIONS[step],
          location:
            stepIndex === 0
              ? 'Warehouse Lisboa'
              : step === 'IN_TRANSIT'
                ? 'Sorting hub Madrid'
                : customer.address.city,
          occurredAt: toIso(shipmentStatus === 'PRE_TRANSIT' ? at(2) : at(STEP_HOURS[stepIndex]))
        }))

        shipments.push({
          id: nextShipmentId++,
          trackingNumber: trackingNumber(rng),
          status: shipmentStatus,
          items: group.map((item) => ({ orderItemId: item.id, quantity: item.quantity })),
          events
        })
      })
    }

    const events: OrderStatusEventResponse[] = [{ status: 'PAID', occurredAt: toIso(createdAt) }]

    if (status !== 'PAID') {
      events.push({ status: 'PROCESSING', occurredAt: toIso(at(2)) })
    }

    if (['SHIPPED', 'DELIVERED', 'RETURN_REQUESTED', 'REFUNDED'].includes(status)) {
      events.push({ status: 'SHIPPED', occurredAt: toIso(at(26)) })
    }

    const delivered = ['DELIVERED', 'RETURN_REQUESTED', 'REFUNDED'].includes(status)
    const deliveredAt = createdAt + 84 * HOUR

    if (delivered) {
      events.push({ status: 'DELIVERED', occurredAt: toIso(deliveredAt) })
    }

    const orderReturns: OrderReturnResponse[] = []
    const rejectedReturn = status === 'DELIVERED' && ageInDays >= 10 && ageInDays < 60 && rng.chance(0.06)

    if (status === 'RETURN_REQUESTED' || status === 'REFUNDED' || rejectedReturn) {
      const returnStatus: ReturnStatus = rejectedReturn
        ? 'REJECTED'
        : status === 'REFUNDED'
          ? 'REFUNDED'
          : PENDING_RETURN_CYCLE[pendingReturnIndex++ % PENDING_RETURN_CYCLE.length]

      const returnedItem = rng.pick(items)
      const returnedQuantity = rng.int(1, returnedItem.quantity)
      const returnCreatedAt = Math.min(deliveredAt + rng.int(1, 3) * DAY, now - 2 * HOUR)
      const refundedAt = returnStatus === 'REFUNDED' ? Math.min(returnCreatedAt + rng.int(3, 5) * DAY, now - HOUR) : undefined
      const refundAmount = returnStatus === 'REFUNDED' ? roundMoney(returnedItem.unitPrice * returnedQuantity) : undefined
      const reason = rng.pick(RETURN_REASONS)
      const comment = rng.chance(0.6) ? rng.pick(RETURN_COMMENTS) : undefined
      const rejectionReason = returnStatus === 'REJECTED' ? rng.pick(REJECTION_REASONS) : undefined
      const returnId = nextReturnId++

      orderReturns.push({
        id: returnId,
        status: returnStatus,
        reason,
        comment,
        refundAmount,
        createdAt: toIso(returnCreatedAt),
        refundedAt: refundedAt ? toIso(refundedAt) : undefined,
        rejectionReason,
        items: [{ orderItemId: returnedItem.id, quantity: returnedQuantity }]
      })

      const response: AdminReturnResponse = {
        id: returnId,
        orderId: id,
        customerEmail: customer.email,
        address: customer.address,
        status: returnStatus,
        reason,
        comment,
        refundAmount,
        rejectionReason,
        createdAt: toIso(returnCreatedAt),
        refundedAt: refundedAt ? toIso(refundedAt) : undefined,
        items: [
          {
            ref: returnedItem.ref,
            productName: returnedItem.productName,
            color: returnedItem.color,
            size: returnedItem.size,
            imagePath: returnedItem.imagePath,
            quantity: returnedQuantity
          }
        ]
      }

      returns.push({ createdAt: returnCreatedAt, refundedAt, response })

      if (!rejectedReturn) {
        events.push({ status: 'RETURN_REQUESTED', occurredAt: toIso(returnCreatedAt) })
      }

      if (refundedAt) {
        events.push({ status: 'REFUNDED', occurredAt: toIso(refundedAt) })
      }
    }

    if (status === 'DELIVERED') {
      items.forEach((item) => {
        const returned = orderReturns.some((orderReturn) => orderReturn.items.some((returnItem) => returnItem.orderItemId === item.id))

        item.returnableQuantity = returned && !rejectedReturn ? 0 : item.quantity
      })
    }

    orders.push({
      id,
      customerId: customer.id,
      customerEmail: customer.email,
      customerName: `${customer.firstName} ${customer.lastName}`,
      createdAt,
      status,
      discountCode: code?.code,
      lines,
      details: {
        ...customer.address,
        id,
        status,
        createdAt: toIso(createdAt),
        currency: 'EUR',
        discountCode: code?.code,
        subtotal,
        discountAmount,
        shippingCost,
        taxAmount,
        totalAmount,
        shippingCarrier: CARRIERS[id % CARRIERS.length],
        shippingService: SERVICES[id % SERVICES.length],
        events,
        shipments,
        returnableUntil: delivered ? toIso(deliveredAt + RETURN_WINDOW_IN_DAYS * DAY) : undefined,
        returns: orderReturns,
        items
      }
    })
  })

  return { orders, returns }
}
