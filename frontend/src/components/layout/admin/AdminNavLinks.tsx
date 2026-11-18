import { NavLink } from 'react-router-dom'
import { useAdminNavItems } from '@/hooks/admin/useAdminNavItems'

interface AdminNavLinksProps {
  onNavigate?: () => void
  className?: string
}

export const AdminNavLinks = ({ onNavigate, className }: AdminNavLinksProps) => {
  const adminNavItems = useAdminNavItems()

  return (
    <nav className={className}>
      {adminNavItems.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.end}
          onClick={onNavigate}
          className={({ isActive }) =>
            `mb-1 flex items-center gap-3 px-4 py-3 text-xs font-bold uppercase tracking-widest transition-colors ${
              isActive
                ? 'bg-neon-cyan text-primary-foreground'
                : 'text-muted-foreground hover:bg-secondary hover:text-foreground'
            }`
          }
        >
          <item.icon className="h-4 w-4" />
          {item.label}
        </NavLink>
      ))}
    </nav>
  )
}
