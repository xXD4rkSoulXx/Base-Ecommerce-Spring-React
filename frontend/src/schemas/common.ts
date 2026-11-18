import { z } from 'zod'

export const nameSchema = (label: string) =>
  z.string().trim().min(1, `${label} is required`).max(100, `${label} must be at most 100 characters`)

export const emailSchema = z
  .string()
  .trim()
  .min(1, 'Email is required')
  .max(254, 'Email must be at most 254 characters')
  .pipe(z.email('Invalid email format'))

export const passwordSchema = z
  .string()
  .min(1, 'Password is required')
  .min(8, 'Password must be between 8 and 72 characters long')
  .max(72, 'Password must be between 8 and 72 characters long')
  .regex(/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).+$/, 'Password must contain uppercase, lowercase and number')
