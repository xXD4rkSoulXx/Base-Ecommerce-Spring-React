import type { UserRole } from '@/types/user'

interface RoleBadgeProps {
  role: UserRole
}

const ROLE_CLASS_NAMES: Record<UserRole, string> = {
  ADMIN: 'text-neon-pink',
  MANAGER: 'text-neon-cyan',
  MERCHANDISER: 'text-foreground',
  FULFILLMENT: 'text-foreground',
  SUPPORT: 'text-foreground',
  DEMO: 'text-muted-foreground',
  CUSTOMER: 'text-muted-foreground'
}

export const RoleBadge = ({ role }: RoleBadgeProps) => (
  <span className={`font-mono text-[10px] uppercase tracking-widest ${ROLE_CLASS_NAMES[role]}`}>{role}</span>
)
