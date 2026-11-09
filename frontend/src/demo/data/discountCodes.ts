import type { DemoDiscountCode } from '@/demo/types'
import { DAY } from '@/demo/data/random'

export const buildDiscountCodes = (now: number): DemoDiscountCode[] => [
  { id: 1, code: 'WELCOME10', discountPercentage: 10, active: true, createdAt: now - 300 * DAY },
  { id: 2, code: 'SUMMER20', discountPercentage: 20, active: true, createdAt: now - 150 * DAY },
  { id: 3, code: 'VIP15', discountPercentage: 15, active: true, createdAt: now - 120 * DAY },
  { id: 4, code: 'FRIENDS5', discountPercentage: 5, active: true, createdAt: now - 90 * DAY },
  { id: 5, code: 'BLACKFRIDAY25', discountPercentage: 25, active: false, createdAt: now - 330 * DAY },
  { id: 6, code: 'STAFF30', discountPercentage: 30, active: false, createdAt: now - 200 * DAY },
  { id: 7, code: 'NEWYEAR12', discountPercentage: 12, active: true, createdAt: now - 10 * DAY }
]
