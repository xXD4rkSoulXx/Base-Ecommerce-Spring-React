import type { Schema } from '@/types/schema'
import type { UserResponse } from '@/types/user'

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface LoginResponse extends Omit<Schema<'LoginResponse'>, 'user'> {
  user: UserResponse
}
