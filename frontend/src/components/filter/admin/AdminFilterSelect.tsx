import { useUrlFilters } from '@/hooks/useUrlFilters'

export interface FilterOption {
  value: string
  label: string
}

interface AdminFilterSelectProps {
  param: string
  label: string
  options: FilterOption[]
  defaultValue?: string
}

export const AdminFilterSelect = ({ param, label, options, defaultValue = '' }: AdminFilterSelectProps) => {
  const { searchParams, changeFilters } = useUrlFilters()

  return (
    <select
      value={searchParams.get(param) || defaultValue}
      onChange={(event) => changeFilters({ [param]: event.target.value })}
      aria-label={label}
      className="border border-border bg-background px-3 py-2.5 font-mono text-xs uppercase tracking-widest outline-none focus:border-neon-cyan sm:w-56"
    >
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  )
}
