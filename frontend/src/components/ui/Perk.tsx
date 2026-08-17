import type { HTMLAttributes } from 'react'

interface PerkProps extends HTMLAttributes<HTMLDivElement>  {
  label: string
  text: string
}

export const Perk = ({ label, text, ...rest }: PerkProps) => {
  return (
    <div className="flex items-center gap-4" {...rest}>
      <span className="font-mono text-[10px] text-neon-cyan">{label}</span>
      <span className="text-sm text-foreground/80">{text}</span>
    </div>
  )
}
