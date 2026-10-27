import { useState } from 'react'
import { ChevronDown } from 'lucide-react'

interface FaqItemProps {
  question: string
  answer: string
}

export const FaqItem = ({ question, answer }: FaqItemProps) => {
  const [open, setOpen] = useState(false)

  return (
    <div className="border-b border-border last:border-b-0">
      <button
        type="button"
        onClick={() => setOpen((value) => !value)}
        aria-expanded={open}
        className="flex w-full items-center justify-between gap-4 px-4 py-4 text-left hover:text-neon-cyan"
      >
        <span className="text-sm font-bold uppercase tracking-wide">{question}</span>
        <ChevronDown className={`h-4 w-4 shrink-0 transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>
      {open && (
        <p className="px-4 pb-4 text-sm leading-relaxed text-muted-foreground">{answer}</p>
      )}
    </div>
  )
}
