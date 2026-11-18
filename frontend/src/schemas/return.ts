import { z } from 'zod'
import { RETURN_REASON_LABELS } from '@/constants'
import type { ReturnReason } from '@/types/order'

const RETURN_REASON_VALUES = Object.keys(RETURN_REASON_LABELS) as [ReturnReason, ...ReturnReason[]]

export const returnSchema = z
  .object({
    quantities: z
      .record(z.string(), z.number().int().min(0))
      .refine((quantities) => Object.values(quantities).some((quantity) => quantity > 0), 'Select at least one item'),
    reason: z.enum(RETURN_REASON_VALUES, 'Select a reason'),
    comment: z.string().trim().max(500, 'Comment cannot exceed 500 characters')
  })
  .refine((data) => data.reason !== 'OTHER' || data.comment !== '', {
    path: ['comment'],
    message: 'Please describe the reason'
  })

export type ReturnFormValues = z.infer<typeof returnSchema>
