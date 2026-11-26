import type { ZodType } from 'zod'

export const issuesOf = (schema: ZodType, input: unknown) => {
  const result = schema.safeParse(input)

  if (result.success) {
    return []
  }

  return result.error.issues.map((issue) => ({ path: issue.path.join('.'), message: issue.message }))
}
