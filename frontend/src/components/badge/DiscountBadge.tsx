interface DiscountBadgeProps {
  percentage: number
}

export const DiscountBadge = ({ percentage }: DiscountBadgeProps) => (
  <div className="absolute right-3 top-3 flex h-12 w-12 items-center justify-center bg-neon-pink font-mono text-xs font-black tracking-tighter text-primary-foreground shadow-lg shadow-neon-pink/30">
    -{percentage}%
  </div>
)
