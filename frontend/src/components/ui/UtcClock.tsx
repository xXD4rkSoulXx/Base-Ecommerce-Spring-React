import { useEffect, useState } from 'react'

interface UtcClockProps {
  className?: string
}

const timeFormatter = new Intl.DateTimeFormat('pt-PT', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  timeZone: 'UTC'
})

export const UtcClock = ({ className = '' }: UtcClockProps) => {
  const [now, setNow] = useState(() => new Date())

  useEffect(() => {
    const interval = setInterval(() => setNow(new Date()), 1000)

    return () => clearInterval(interval)
  }, [])

  return (
    <span className={`font-mono text-[10px] uppercase tracking-widest text-muted-foreground ${className}`}>
      UTC {timeFormatter.format(now)}
    </span>
  )
}
