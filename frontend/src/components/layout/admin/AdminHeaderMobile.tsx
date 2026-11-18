import { Link } from 'react-router-dom'
import { Menu } from 'lucide-react'
import { APP_NAME } from '@/constants'

interface AdminHeaderMobileProps {
  onOpenMenu: () => void
}

export const AdminHeaderMobile = ({ onOpenMenu }: AdminHeaderMobileProps) => (
  <header className="flex items-center justify-between border-b border-border bg-card px-5 py-4 lg:hidden">
    <button
      type="button"
      onClick={onOpenMenu}
      className="text-foreground/80 transition-colors hover:text-neon-cyan"
      aria-label="Open admin menu"
    >
      <Menu className="h-5 w-5" />
    </button>
    <Link to="/admin" className="font-display text-lg italic tracking-tighter">
      {APP_NAME}
    </Link>
    <span className="font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Admin</span>
  </header>
)
