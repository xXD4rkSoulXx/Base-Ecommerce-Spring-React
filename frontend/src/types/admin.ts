import type { LucideIcon } from 'lucide-react'
import type { Permission } from '@/types/user'

export interface AdminNavItem {
  to: string
  label: string
  icon: LucideIcon
  permission: Permission
  end?: boolean
}
