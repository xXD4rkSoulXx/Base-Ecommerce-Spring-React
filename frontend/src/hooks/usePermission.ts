import { useAuthStore } from '@/store/useAuthStore'
import type { Permission } from '@/types/user'

export const usePermission = (permission: Permission) =>
  useAuthStore((state) => state.user?.permissions.includes(permission) ?? false)
