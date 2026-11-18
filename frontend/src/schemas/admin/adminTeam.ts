import { z } from 'zod'
import { emailSchema } from '@/schemas/common'
import type { UserRole } from '@/types/user'

export const assignRoleSchema = (currentRole?: UserRole) =>
  z
    .object({
      email: emailSchema,
      role: z.custom<UserRole>()
    })
    .refine((data) => data.role !== currentRole, { path: ['role'], message: 'Select a different role' })

export type AssignRoleFormValues = z.infer<ReturnType<typeof assignRoleSchema>>
