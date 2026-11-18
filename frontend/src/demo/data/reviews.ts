import type { DemoCustomer, DemoProduct, DemoReview } from '@/demo/types'
import { DAY, type Random } from '@/demo/data/random'

const REVIEW_COUNT = 32

const POSITIVE: Array<[string, string]> = [
  ['Love it', 'Great quality for the price and it fits exactly as expected.'],
  ['Exactly what I wanted', 'The material feels premium and the colour matches the photos.'],
  ['Great fit', 'True to size, comfortable from the first wear.'],
  ['Would buy again', 'Fast delivery and the product is even better in person.']
]

const NEUTRAL: Array<[string, string]> = [
  ['It is fine', 'Good overall but the sizing runs a little large.'],
  ['Decent', 'Nice design, although I expected a thicker fabric.']
]

const NEGATIVE: Array<[string, string]> = [
  ['Disappointed', 'The stitching came apart after two washes.'],
  ['Not as described', 'The colour looks much duller than in the pictures.'],
  ['Does not fit', 'Way too small, even after sizing up.']
]

const chooseRating = (rng: Random) => {
  const roll = rng.next()

  return roll < 0.45 ? 5 : roll < 0.75 ? 4 : roll < 0.88 ? 3 : roll < 0.96 ? 2 : 1
}

export const buildReviews = (
  now: number,
  rng: Random,
  products: DemoProduct[],
  customers: DemoCustomer[]
): DemoReview[] => {
  const reviewable = products.filter((product) => product.active)

  return Array.from({ length: REVIEW_COUNT }, (_, index) => {
    const product = rng.pick(reviewable)
    const customer = rng.pick(customers)
    const rating = chooseRating(rng)
    const [title, description] = rng.pick(rating >= 4 ? POSITIVE : rating === 3 ? NEUTRAL : NEGATIVE)
    const hidden = rating <= 2 ? rng.chance(0.5) : rng.chance(0.04)

    return {
      id: index + 1,
      productId: product.id,
      productName: product.name,
      productRef: product.ref,
      customerName: `${customer.firstName} ${customer.lastName}`,
      customerEmail: customer.email,
      rating,
      title,
      description,
      hidden,
      createdAt: now - Math.pow(rng.next(), 1.3) * 90 * DAY
    }
  })
    .sort((a, b) => a.createdAt - b.createdAt)
    .map((review, index) => ({ ...review, id: index + 1 }))
}
