import { NavLink, useNavigate } from 'react-router-dom'
import { ArrowLeft, LayoutDashboard, Package, Tag, ShoppingCart, Users, type LucideIcon } from 'lucide-react'
import { useAuthStore } from '@/store/useAuthStore'

interface AdminNavItem {
  to: string
  label: string
  icon: LucideIcon
  end?: boolean
}

export const AdminSidebar = () => {
  const navigate = useNavigate()

  const hasAnyRole = useAuthStore((state) => state.hasAnyRole)

  const isManagerOrAdmin = hasAnyRole(['MANAGER', 'ADMIN'])
  const isAdminOnly = hasAnyRole(['ADMIN'])
  
  const adminNavItems: AdminNavItem[] = [
    { to: '/admin', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/admin/products', label: 'Products', icon: Package },
    ...(isManagerOrAdmin ? [{ to: '/admin/discounts', label: 'Discounts', icon: Tag }] : []),
    { to: '/admin/orders', label: 'Orders', icon: ShoppingCart },
    ...(isAdminOnly ? [{ to: '/admin/users', label: 'Users', icon: Users }] : [])
  ]

  return (
    <aside className="sticky top-0 hidden h-dvh w-64 shrink-0 border-r border-border bg-card lg:block">
      <div className="border-b border-border p-6">
        <NavLink
          to="/"
          end
          className="font-display text-xl italic tracking-tighter"
        >
          VELO_CITI
        </NavLink>
        <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Admin Console</p>
      </div>
      <nav className="p-3">
        {adminNavItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            className={({ isActive }) =>
              `mb-1 flex items-center gap-3 px-4 py-3 text-xs font-bold uppercase tracking-widest transition-colors ${
                isActive ? 'bg-neon-cyan text-primary-foreground' : 'text-muted-foreground hover:bg-secondary hover:text-foreground'
              }`
            }
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>
      <button
        onClick={() => navigate('/')}
        className="absolute bottom-6 left-6 flex items-center gap-2 text-[10px] font-bold uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3 w-3" /> Back to Store
      </button>
    </aside>
  )
}
