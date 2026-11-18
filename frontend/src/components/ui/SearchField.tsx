import type { SubmitEvent } from 'react'
import { SearchIcon } from 'lucide-react'

interface SearchFieldProps {
  value: string
  placeholder: string
  onChange: (value: string) => void
  onSubmit: (event: SubmitEvent<HTMLFormElement>) => void
}

export const SearchField = ({ value, placeholder, onChange, onSubmit }: SearchFieldProps) => (
  <form onSubmit={onSubmit} className="flex flex-1 items-center gap-2 border border-border bg-background px-3">
    <SearchIcon className="h-3.5 w-3.5 text-neon-cyan" />
    <input
      value={value}
      onChange={(event) => onChange(event.target.value)}
      aria-label="Search"
      placeholder={placeholder}
      className="w-full bg-transparent py-2.5 text-sm outline-none placeholder:text-muted-foreground"
    />
  </form>
)
