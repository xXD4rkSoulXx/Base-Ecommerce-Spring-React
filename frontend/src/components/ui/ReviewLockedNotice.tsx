import { Lock } from 'lucide-react'

interface ReviewLockedNoticeProps {
  message: string
}

export const ReviewLockedNotice = ({ message }: ReviewLockedNoticeProps) => (
  <div className="flex items-center gap-3 border border-dashed border-border bg-card/40 px-5 py-4 text-xs">
    <Lock className="h-4 w-4 shrink-0 text-neon-pink" />
    <span className="font-mono uppercase tracking-widest text-muted-foreground">{message}</span>
  </div>
)
