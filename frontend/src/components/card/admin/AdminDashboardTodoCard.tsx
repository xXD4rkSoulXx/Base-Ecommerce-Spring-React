import { Link } from 'react-router-dom'

interface AdminDashboardTodoCardProps {
  label: string
  count: number | undefined
  to: string
  isError: boolean
}

export const AdminDashboardTodoCard = ({ label, count, to, isError }: AdminDashboardTodoCardProps) => (
  <Link to={to} className="block border border-border bg-card p-5 hover:border-neon-cyan">
    <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">{label}</p>
    <p
      className={`mt-2 font-display text-3xl tracking-tighter ${
        count ? 'text-neon-pink' : ''
      }`}
    >
      {isError ? '—' : (count ?? '…')}
    </p>
  </Link>
)
