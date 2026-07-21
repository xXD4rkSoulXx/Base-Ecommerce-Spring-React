import { Link } from 'react-router-dom'
import { Menu } from 'lucide-react'

function AdminHeaderMobile({ onOpenMenu }: { onOpenMenu: () => void }) {
  return (
    <header className="flex items-center justify-between border-b border-border bg-card px-5 py-4 lg:hidden">
      <button onClick={onOpenMenu} className="text-foreground/80 transition-colors hover:text-neon-cyan" aria-label="Open admin menu">
        <Menu className="h-5 w-5" />
      </button>
      <Link to="/admin" className="font-display text-lg italic tracking-tighter">VELO_CITI</Link>
      <span className="font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Admin</span>
    </header>
  )
}

export default AdminHeaderMobile
