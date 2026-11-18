import { useNavigate } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { useExitDemo } from '@/hooks/useExitDemo'
import { useAuthStore } from '@/store/useAuthStore'

interface AdminLeaveButtonProps {
  onNavigate?: () => void
}

export const AdminLeaveButton = ({ onNavigate }: AdminLeaveButtonProps) => {
  const navigate = useNavigate()
  const exitDemo = useExitDemo()
  const isDemo = useAuthStore((state) => state.user?.role === 'DEMO')

  const leave = () => {
    onNavigate?.()

    if (isDemo) {
      exitDemo()
    } else {
      navigate('/')
    }
  }

  return (
    <button
      type="button"
      onClick={leave}
      className="absolute bottom-6 left-6 flex items-center gap-2 text-[10px] font-bold uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
    >
      <ArrowLeft className="h-3 w-3" /> {isDemo ? 'Exit demo' : 'Back to Store'}
    </button>
  )
}
