import type { ReactNode } from 'react'
import { useUrlFilters } from '@/hooks/useUrlFilters'

interface AdminFilterCheckboxProps {
  param: string
  children: ReactNode
}

export const AdminFilterCheckbox = ({ param, children }: AdminFilterCheckboxProps) => {
  const { searchParams, changeFilters } = useUrlFilters()

  return (
    <label className="flex cursor-pointer items-center gap-2 border border-border bg-background px-3 py-2.5 font-mono text-xs uppercase tracking-widest">
      <input
        type="checkbox"
        checked={searchParams.get(param) === 'true'}
        onChange={(event) => changeFilters({ [param]: event.target.checked ? 'true' : '' })}
        className="accent-neon-cyan"
      />
      {children}
    </label>
  )
}
