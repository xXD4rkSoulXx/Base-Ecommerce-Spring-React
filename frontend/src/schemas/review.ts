import { z } from 'zod'

export const reviewSchema = z.object({
  rating: z.number().int().min(1, 'Select a rating').max(5),
  title: z.string().trim().min(1, 'Title is required').max(100, 'Title must be at most 100 characters'),
  description: z
    .string()
    .trim()
    .min(1, 'Description is required')
    .max(2000, 'Description must be at most 2000 characters')
})

export type ReviewFormValues = z.infer<typeof reviewSchema>
