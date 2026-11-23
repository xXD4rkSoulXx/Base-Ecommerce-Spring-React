import { z } from 'zod'
import { selectSchema, wholeNumberSchema } from '@/schemas/helpers'
import type { ProductCategory } from '@/types/product'

export const productSchema = z.object({
  ref: z
    .string()
    .trim()
    .toUpperCase()
    .min(1, 'Ref is required')
    .max(50, 'Ref must be at most 50 characters')
    .regex(/^[A-Z0-9-]+$/, 'Ref can only contain letters, numbers and dashes'),
  name: z.string().trim().min(1, 'Name is required').max(120, 'Name must be at most 120 characters'),
  description: z
    .string()
    .trim()
    .min(1, 'Description is required')
    .max(5000, 'Description must be at most 5000 characters'),
  price: z
    .string()
    .trim()
    .regex(/^\d{1,5}([.,]\d{1,2})?$/, 'Price must be between 0.01 and 99999.99, with at most 2 decimal places')
    .refine(
      (price) => Number(price.replace(',', '.')) >= 0.01,
      'Price must be between 0.01 and 99999.99, with at most 2 decimal places'
    ),
  discountPercentage: wholeNumberSchema('Discount must be a whole number between 0 and 100', 0, 100),
  category: selectSchema<ProductCategory>('Select a category'),
  featuredSlot: z.enum(['', 'LEFT', 'RIGHT'])
})

export type ProductFormValues = z.infer<typeof productSchema>
