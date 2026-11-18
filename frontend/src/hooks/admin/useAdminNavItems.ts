import { Boxes, Contact, LayoutDashboard, MessageSquare, Package, RotateCcw, ShoppingCart, Tag, Users, History, type LucideIcon } from 'lucide-react'
import { useAuthStore } from '@/store/useAuthStore'
import type { Permission } from '@/types/user'

interface AdminNavItem {
  to: string
  label: string
  icon: LucideIcon
  permission: Permission
  end?: boolean
}

const ADMIN_NAV_ITEMS: AdminNavItem[] = [
  { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, permission: 'DASHBOARD_READ', end: true },
  { to: '/admin/products', label: 'Products', icon: Package, permission: 'PRODUCTS_READ' },
  { to: '/admin/stock', label: 'Stock', icon: Boxes, permission: 'STOCK_READ' },
  { to: '/admin/orders', label: 'Orders', icon: ShoppingCart, permission: 'ORDERS_READ' },
  { to: '/admin/returns', label: 'Returns', icon: RotateCcw, permission: 'RETURNS_READ' },
  { to: '/admin/customers', label: 'Customers', icon: Contact, permission: 'CUSTOMERS_READ' },
  { to: '/admin/reviews', label: 'Reviews', icon: MessageSquare, permission: 'REVIEWS_READ' },
  { to: '/admin/discounts', label: 'Discounts', icon: Tag, permission: 'DISCOUNTS_READ' },
  { to: '/admin/team', label: 'Team', icon: Users, permission: 'TEAM_READ' },
  { to: '/admin/audit', label: 'Audit log', icon: History, permission: 'AUDIT_READ' }
]

export const useAdminNavItems = () => {
  const permissions = useAuthStore((state) => state.user?.permissions)

  return ADMIN_NAV_ITEMS.filter((item) => permissions?.includes(item.permission))
}
