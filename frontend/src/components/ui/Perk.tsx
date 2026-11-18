interface PerkProps {
  label: string
  text: string
}

export const Perk = ({ label, text }: PerkProps) => (
  <div className="flex items-center gap-4">
    <span className="font-mono text-[10px] text-neon-cyan">{label}</span>
    <span className="text-sm text-foreground/80">{text}</span>
  </div>
)
