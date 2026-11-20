import { useEffect, type ReactNode } from 'react'
import { X } from 'lucide-react'

type DrawerBreakpoint = 'lg' | 'xl'

interface DrawerProps {
  open: boolean
  onClose: () => void
  label: string
  hideFrom: DrawerBreakpoint
  children: ReactNode
}

const HIDE_FROM_CLASS_NAMES: Record<DrawerBreakpoint, string> = {
  lg: 'lg:hidden',
  xl: 'xl:hidden'
}

export const Drawer = ({ open, onClose, label, hideFrom, children }: DrawerProps) => {
  useEffect(() => {
    const closeOnEscape = (event: KeyboardEvent) => {
      if (open && event.key === 'Escape') {
        onClose()
      }
    }

    document.addEventListener('keydown', closeOnEscape)

    return () => document.removeEventListener('keydown', closeOnEscape)
  }, [open, onClose])

  useEffect(() => {
    if (open) {
      document.body.style.overflow = 'hidden'
    }

    return () => {
      document.body.style.overflow = ''
    }
  }, [open])

  return (
    <div
      className={`fixed inset-0 z-50 ${HIDE_FROM_CLASS_NAMES[hideFrom]} ${open ? '' : 'pointer-events-none'}`}
      inert={!open}
    >
      <div
        onClick={onClose}
        aria-hidden="true"
        className={`absolute inset-0 bg-background/80 backdrop-blur-sm transition-opacity ${
          open ? 'opacity-100' : 'opacity-0'
        }`}
      />
      <div
        role="dialog"
        aria-label={label}
        className={`absolute left-0 top-0 h-full w-[85%] max-w-sm border-r border-neon-cyan/30 bg-card p-6 shadow-2xl shadow-neon-cyan/10 transition-transform duration-300 ${
          open ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <button
          type="button"
          onClick={onClose}
          aria-label="Close menu"
          className="absolute right-6 top-6 text-foreground/70 hover:text-neon-cyan"
        >
          <X className="h-5 w-5" />
        </button>
        {children}
      </div>
    </div>
  )
}
