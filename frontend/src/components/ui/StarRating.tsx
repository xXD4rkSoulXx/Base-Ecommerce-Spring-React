import { Star } from 'lucide-react'

interface StarRatingProps {
  value: number
  onChange?: (n: number) => void
  size?: number
  readOnly?: boolean
}

const STARS = [1, 2, 3, 4, 5]

export const StarRating = ({ value, onChange, size = 18, readOnly = false }: StarRatingProps) => {
  const rounded = Math.round(value)

  return (
    <div
      role={readOnly ? 'img' : 'group'}
      aria-label={readOnly ? `Rated ${rounded} out of 5` : 'Rating'}
      className="flex items-center gap-1"
    >
      {STARS.map((n) => {
        const active = n <= rounded
        const color = active ? 'text-neon-cyan' : 'text-muted-foreground/40'
        const star = <Star size={size} fill={active ? 'currentColor' : 'none'} strokeWidth={1.5} />

        if (readOnly) {
          return (
            <span key={n} className={`transition-colors ${color}`}>
              {star}
            </span>
          )
        }

        return (
          <button
            key={n}
            type="button"
            aria-label={`${n} star${n > 1 ? 's' : ''}`}
            aria-pressed={n === rounded}
            onClick={() => onChange?.(n)}
            className={`transition-colors hover:scale-110 ${color}`}
          >
            {star}
          </button>
        )
      })}
    </div>
  )
}
