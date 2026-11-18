import type { FilterOption } from '@/components/filter/admin/AdminFilterSelect'
import { ALL_FILTER, RETURN_STATUS_LABELS, TODO_FILTER } from '@/constants'
import { useUrlFilters } from '@/hooks/useUrlFilters'

const OPTIONS: FilterOption[] = [
  { value: ALL_FILTER, label: 'All' },
  { value: TODO_FILTER, label: 'To do' },
  ...Object.entries(RETURN_STATUS_LABELS).map(([value, label]) => ({ value, label }))
]

export const AdminReturnStatusFilter = () => {
  const { searchParams, changeFilters } = useUrlFilters()
  const current = searchParams.get('status') || TODO_FILTER

  return (
    <div className="mb-6 flex flex-wrap gap-2">
      {OPTIONS.map((option) => (
        <button
          key={option.value}
          type="button"
          aria-pressed={current === option.value}
          onClick={() => changeFilters({ status: option.value })}
          className={`border px-4 py-2 font-mono text-[10px] uppercase tracking-widest transition-colors ${
            current === option.value
              ? 'border-neon-cyan bg-neon-cyan/10 text-neon-cyan'
              : 'border-border text-muted-foreground hover:border-foreground/40'
          }`}
        >
          {option.label}
        </button>
      ))}
    </div>
  )
}
