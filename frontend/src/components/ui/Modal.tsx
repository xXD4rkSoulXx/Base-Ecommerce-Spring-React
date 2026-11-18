import { useEffect, useId, useRef, type ReactNode } from 'react'
import { X } from 'lucide-react'

type ModalTone = 'cyan' | 'pink'
type ModalSize = 'md' | 'lg' | '2xl'

interface ModalProps {
  eyebrow: string
  title: string
  description?: string
  icon?: ReactNode
  tone?: ModalTone
  size?: ModalSize
  onClose: () => void
  children: ReactNode
}

const TONE_CLASS_NAMES: Record<ModalTone, { panel: string; eyebrow: string; close: string }> = {
  cyan: {
    panel: 'border-neon-cyan/40 shadow-neon-cyan/10',
    eyebrow: 'text-neon-cyan',
    close: 'hover:text-neon-cyan'
  },
  pink: {
    panel: 'border-neon-pink/40 shadow-neon-pink/10',
    eyebrow: 'text-neon-pink',
    close: 'hover:text-neon-pink'
  }
}

const SIZE_CLASS_NAMES: Record<ModalSize, string> = {
  md: 'max-w-md',
  lg: 'max-w-lg',
  '2xl': 'max-w-2xl'
}

export const Modal = ({
  eyebrow,
  title,
  description,
  icon,
  tone = 'cyan',
  size = 'md',
  onClose,
  children
}: ModalProps) => {
  const titleId = useId()
  const dialogRef = useRef<HTMLDialogElement>(null)
  const toneClassNames = TONE_CLASS_NAMES[tone]

  useEffect(() => {
    const dialog = dialogRef.current

    if (dialog && !dialog.open) {
      dialog.showModal()
    }
  }, [])

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={titleId}
      onClose={onClose}
      onClick={(event) => {
        if (event.target === event.currentTarget) {
          onClose()
        }
      }}
      className={`m-auto w-[calc(100%-2rem)] bg-transparent p-0 text-foreground backdrop:bg-background/80 backdrop:backdrop-blur-sm ${SIZE_CLASS_NAMES[size]}`}
    >
      <div className={`max-h-[90dvh] overflow-y-auto border bg-card p-6 shadow-2xl ${toneClassNames.panel}`}>
        <div className="flex items-start justify-between">
          <div>
            <p className={`font-mono text-[10px] uppercase tracking-[0.4em] ${toneClassNames.eyebrow}`}>{eyebrow}</p>
            <h2 id={titleId} className="mt-2 flex items-center gap-2 font-display text-2xl uppercase tracking-tighter">
              {icon}
              {title}
            </h2>
            {description && <p className="mt-2 text-xs text-muted-foreground">{description}</p>}
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className={`text-muted-foreground ${toneClassNames.close}`}
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="mt-6">{children}</div>
      </div>
    </dialog>
  )
}
