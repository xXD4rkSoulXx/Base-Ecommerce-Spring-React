interface PaginationProps {
  currentPage: number
  totalPages: number
  onChange: (page: number) => void
}

export const Pagination = ({ currentPage, totalPages, onChange }: PaginationProps) => {
  if (totalPages <= 1) {
    return null
  }

  return (
    <nav
      aria-label="Pagination"
      className="mt-16 flex items-center justify-center gap-4 border-t border-border pt-8 font-mono text-xs"
    >
      <button
        type="button"
        aria-label="Previous page"
        disabled={currentPage === 1}
        onClick={() => onChange(currentPage - 1)}
        className="border border-border px-4 py-2 uppercase tracking-widest transition-colors enabled:hover:border-neon-cyan enabled:hover:text-neon-cyan disabled:opacity-30"
      >
        PREV
      </button>
      <span className="uppercase tracking-wider text-muted-foreground">
        Page <span className="font-bold text-foreground">{currentPage}</span> of{' '}
        <span className="font-bold text-foreground">{totalPages}</span>
      </span>
      <button
        type="button"
        aria-label="Next page"
        disabled={currentPage === totalPages}
        onClick={() => onChange(currentPage + 1)}
        className="border border-border px-4 py-2 uppercase tracking-widest transition-colors enabled:hover:border-neon-cyan enabled:hover:text-neon-cyan disabled:opacity-30"
      >
        NEXT
      </button>
    </nav>
  )
}
