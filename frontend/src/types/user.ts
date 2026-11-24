import type { Schema } from '@/types/schema'

export type Permission = Schema<'UserResponse'>['permissions'][number]

export type UserRole = Schema<'UserResponse'>['role'] | 'DEMO'

export interface UpdateProfileRequest {
  firstName: string
  lastName: string
}

export interface UserResponse extends Omit<Schema<'UserResponse'>, 'role'> {
  role: UserRole
}

export interface ProfileResponse extends Omit<Schema<'ProfileResponse'>, 'user'> {
  user: UserResponse
}
