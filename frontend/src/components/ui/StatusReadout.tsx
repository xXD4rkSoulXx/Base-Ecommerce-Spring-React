type StatusReadoutTone = 'pink' | 'cyan'

interface StatusReadoutLine {
  label: string
  value: string
  tone: StatusReadoutTone
}

interface StatusReadoutProps {
  lines: StatusReadoutLine[]
}

const TONE_CLASS_NAMES: Record<StatusReadoutTone, string> = {
  pink: 'text-neon-pink',
  cyan: 'text-neon-cyan'
}

export const StatusReadout = ({ lines }: StatusReadoutProps) => (
  <div className="mx-auto mt-6 max-w-md space-y-1 border border-border bg-card p-4 text-left font-mono text-xs">
    {lines.map((line) => (
      <p key={line.label} className="text-muted-foreground">
        <span className={TONE_CLASS_NAMES[line.tone]}>●</span> {line.label} ::{' '}
        <span className="text-foreground">{line.value}</span>
      </p>
    ))}
  </div>
)
