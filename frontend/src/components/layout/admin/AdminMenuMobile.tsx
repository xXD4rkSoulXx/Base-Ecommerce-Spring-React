import { useEffect } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { X, ArrowLeft, LayoutDashboard, Package, Tag, ShoppingCart, Users, type LucideIcon } from 'lucide-react'
import { useAuthStore } from '@/store/useAuthStore'

interface AdminMenuMobileProps {
  open: boolean
  onClose: () => void
}

interface AdminNavItem {
  to: string
  label: string
  icon: LucideIcon
  end?: boolean
}

export const AdminMenuMobile = ({ open, onClose }: AdminMenuMobileProps) => {
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

  useEffect(() => {
    if (open) {
      document.body.style.overflow = 'hidden'
    }

    return () => { document.body.style.overflow = '' }
  }, [open])

  const handleBackToStore = () => {
    onClose()
    navigate('/')
  }

  return (
    <div className={`fixed inset-0 z-50 lg:hidden ${open ? '' : 'pointer-events-none'}`} aria-hidden={!open}>
      <div onClick={onClose} className={`absolute inset-0 bg-background/80 backdrop-blur-sm transition-opacity ${open ? 'opacity-100' : 'opacity-0'}`} />
      <aside className={`absolute left-0 top-0 h-full w-[85%] max-w-sm border-r border-neon-cyan/30 bg-card p-6 shadow-2xl shadow-neon-cyan/10 transition-transform duration-300 ${open ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="flex items-center justify-between">
          <div>
            <NavLink
              to="/admin"
              end
              onClick={onClose}
              className="font-display text-xl italic tracking-tighter"
            >
              VELO_CITI
            </NavLink>
            <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Admin Console</p>
          </div>
          <button
            onClick={onClose}
            className="text-foreground/70 hover:text-neon-cyan"
            aria-label="Close menu"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <nav className="mt-8">
          {adminNavItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              onClick={onClose}
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
          onClick={handleBackToStore}
          className="absolute bottom-6 left-6 flex items-center gap-2 text-[10px] font-bold uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
        >
          <ArrowLeft className="h-3 w-3" /> Back to Store
        </button>
      </aside>
    </div>
  )
}
