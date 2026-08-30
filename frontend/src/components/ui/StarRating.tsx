import { Star } from 'lucide-react'

interface StarRatingProps {
  value: number
  onChange?: (n: number) => void
  size?: number
  readOnly?: boolean
}

export const StarRating = ({ value, onChange, size = 18, readOnly = false }: StarRatingProps) => {
  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((n) => {
        const active = n <= Math.round(value)
        const Component = readOnly ? 'span' : 'button'

        return (
          <Component
            key={n}
            {...(!readOnly && {
              type: "button",
              onClick: () => onChange?.(n),
              'aria-label': `${n} star${n > 1 ? 's' : ''}`,
            })}
            className={`transition-colors ${!readOnly ? 'hover:scale-110' : ''} ${
              active ? 'text-neon-cyan' : 'text-muted-foreground/40'
            }`}
          >
            <Star
              className=""
              style={{ width: size, height: size }}
              fill={active ? 'currentColor' : 'none'}
              strokeWidth={1.5}
            />
          </Component>
        )
      })}
    </div>
  )
}
