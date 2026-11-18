import { Link } from 'react-router-dom'
import { APP_NAME } from '@/constants'
import { useAuthStore } from '@/store/useAuthStore'

interface AdminBrandProps {
  onNavigate?: () => void
}

export const AdminBrand = ({ onNavigate }: AdminBrandProps) => {
  const isDemo = useAuthStore((state) => state.user?.role === 'DEMO')

  return (
    <div>
      <Link to="/admin" onClick={onNavigate} className="font-display text-xl italic tracking-tighter">
        {APP_NAME}
      </Link>
      <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Admin Console</p>
      {isDemo && <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-neon-pink">Read-only demo</p>}
    </div>
  )
}
