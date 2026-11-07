import { Boxes, LayoutDashboard, Package, RotateCcw, ShoppingCart, Tag, Users } from 'lucide-react'
import { useAuthStore } from '@/store/useAuthStore'
import type { AdminNavItem } from '@/types/admin'

const ADMIN_NAV_ITEMS: AdminNavItem[] = [
  { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, permission: 'DASHBOARD_READ', end: true },
  { to: '/admin/products', label: 'Products', icon: Package, permission: 'PRODUCTS_READ' },
  { to: '/admin/stock', label: 'Stock', icon: Boxes, permission: 'STOCK_READ' },
  { to: '/admin/orders', label: 'Orders', icon: ShoppingCart, permission: 'ORDERS_READ' },
  { to: '/admin/returns', label: 'Returns', icon: RotateCcw, permission: 'RETURNS_READ' },
  { to: '/admin/discounts', label: 'Discounts', icon: Tag, permission: 'DISCOUNTS_READ' },
  { to: '/admin/team', label: 'Team', icon: Users, permission: 'TEAM_READ' }
]

export const useAdminNavItems = () => {
  const permissions = useAuthStore((state) => state.user?.permissions)

  return ADMIN_NAV_ITEMS.filter((item) => permissions?.includes(item.permission))
}
