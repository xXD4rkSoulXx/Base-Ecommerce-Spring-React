import { z } from 'zod'

export const wholeNumberSchema = (message: string, min: number, max: number) =>
  z.number(message).int(message).min(min, message).max(max, message)

export const selectSchema = <T extends string>(message: string) =>
  z.custom<T>((value) => typeof value === 'string' && value !== '', message)
