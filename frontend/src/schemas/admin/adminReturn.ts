import { z } from 'zod'

export const rejectReturnSchema = z.object({
  reason: z.string().trim().min(1, 'Reason is required').max(255, 'Reason must be at most 255 characters')
})

export type RejectReturnFormValues = z.infer<typeof rejectReturnSchema>
