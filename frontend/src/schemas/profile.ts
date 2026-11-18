import { z } from 'zod'
import { nameSchema } from '@/schemas/common'

export const profileSchema = z.object({
  firstName: nameSchema('First name'),
  lastName: nameSchema('Last name')
})

export type ProfileForm = z.infer<typeof profileSchema>
