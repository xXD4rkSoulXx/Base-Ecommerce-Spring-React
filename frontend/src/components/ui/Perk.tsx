interface PerkProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  text: string
}

function Perk({ label, text }: PerkProps) {
  return (
    <div className="flex items-center gap-4">
      <span className="font-mono text-[10px] text-neon-cyan">{label}</span>
      <span className="text-sm text-foreground/80">{text}</span>
    </div>
  )
}

export default Perk
