import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'

interface AdminBackLinkProps {
  to: string
  children: string
}

export const AdminBackLink = ({ to, children }: AdminBackLinkProps) => (
  <Link
    to={to}
    className="mb-6 inline-flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
  >
    <ArrowLeft className="h-3.5 w-3.5" /> {children}
  </Link>
)
