import { z } from 'zod'
import { selectSchema, wholeNumberSchema } from '@/schemas/helpers'
import type { ProductColor, ProductSize } from '@/types/product'

export const variantSchema = z.object({
  size: selectSchema<ProductSize>('Select a size'),
  color: selectSchema<ProductColor>('Select a color'),
  weightGrams: wholeNumberSchema('Weight must be a whole number between 1 and 100000 g', 1, 100000),
  lengthCm: wholeNumberSchema('Length must be a whole number between 1 and 1000 cm', 1, 1000),
  widthCm: wholeNumberSchema('Width must be a whole number between 1 and 1000 cm', 1, 1000),
  heightCm: wholeNumberSchema('Height must be a whole number between 1 and 1000 cm', 1, 1000),
  discountPercentage: wholeNumberSchema('Discount must be a whole number between 0 and 100', 0, 100)
})

export type VariantFormValues = z.infer<typeof variantSchema>
