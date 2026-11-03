import { useSearchParams } from 'react-router-dom'

const TABS = [
  { value: 'ALL', label: 'All' },
  { value: 'TODO', label: 'To do' },
  { value: 'REQUESTED', label: 'Requested' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'RECEIVED', label: 'Received' },
  { value: 'REFUNDED', label: 'Refunded' },
  { value: 'REJECTED', label: 'Rejected' }
]

export const AdminReturnTabs = () => {
  const [searchParams, setSearchParams] = useSearchParams()
  const current = searchParams.get('status') || 'TODO'

  const select = (value: string) => {
    const next = new URLSearchParams(searchParams)

    next.set('status', value)
    next.delete('page')
    setSearchParams(next)
  }

  return (
    <div className="mb-6 flex flex-wrap gap-2">
      {TABS.map((tab) => (
        <button
          key={tab.value}
          onClick={() => select(tab.value)}
          className={`border px-4 py-2 font-mono text-[10px] uppercase tracking-widest transition-colors ${
            current === tab.value
              ? 'border-neon-cyan bg-neon-cyan/10 text-neon-cyan'
              : 'border-border text-muted-foreground hover:border-foreground/40'
          }`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  )
}
