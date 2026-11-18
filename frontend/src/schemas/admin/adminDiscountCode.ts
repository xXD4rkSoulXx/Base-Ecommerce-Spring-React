import { z } from 'zod'
import { wholeNumberSchema } from '@/schemas/helpers'

export const discountCodeSchema = z.object({
  code: z
    .string()
    .trim()
    .toUpperCase()
    .regex(/^[A-Z0-9_-]{3,20}$/, 'Code must have 3 to 20 characters: letters, numbers, - or _'),
  discountPercentage: wholeNumberSchema('Discount must be a whole number between 1 and 100', 1, 100)
})

export type DiscountCodeFormValues = z.infer<typeof discountCodeSchema>
