import { z } from 'zod'

const AMOUNT_MESSAGE = 'Enter a whole number between -100000 and 100000'

export const stockSchema = (currentStock: number) =>
  z
    .object({
      mode: z.enum(['ADJUST', 'SET']),
      amount: z.number(AMOUNT_MESSAGE).int(AMOUNT_MESSAGE).min(-100000, AMOUNT_MESSAGE).max(100000, AMOUNT_MESSAGE)
    })
    .refine((data) => (data.mode === 'ADJUST' ? currentStock + data.amount : data.amount) >= 0, {
      path: ['amount'],
      message: 'Stock cannot go below zero'
    })

export type StockFormValues = z.infer<ReturnType<typeof stockSchema>>
