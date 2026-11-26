import { formatPrice } from '@/utils/format'

export const priceText = (value: number) => formatPrice(value).replace(/\s/g, ' ')
