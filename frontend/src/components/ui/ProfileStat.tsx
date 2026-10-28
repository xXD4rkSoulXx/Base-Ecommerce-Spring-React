interface ProfileStatProps {
  label: string
  value: string | number
}

export const ProfileStat = ({ label, value }: ProfileStatProps) => (
  <div>
    <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">{label}</p>
    <p className="mt-1 font-display text-2xl uppercase tracking-tighter">{value}</p>
  </div>
)
