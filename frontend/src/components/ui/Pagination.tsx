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
    <div className="mt-16 flex items-center justify-center gap-4 border-t border-border pt-8 font-mono text-xs">
      <button
        disabled={currentPage === 1}
        onClick={() => onChange(currentPage - 1)}
        className="border border-border px-4 py-2 uppercase tracking-widest transition-colors hover:border-neon-cyan hover:text-neon-cyan disabled:opacity-30 disabled:hover:border-border disabled:hover:text-muted-foreground"
      >
        PREV
      </button>
      <span className="text-muted-foreground uppercase tracking-wider">
        Page <span className="font-bold text-foreground">{currentPage}</span> of <span className="font-bold text-foreground">{totalPages}</span>
      </span>
      <button
        disabled={currentPage === totalPages}
        onClick={() => onChange(currentPage + 1)}
        className="border border-border px-4 py-2 uppercase tracking-widest transition-colors hover:border-neon-cyan hover:text-neon-cyan disabled:opacity-30 disabled:hover:border-border disabled:hover:text-muted-foreground"
      >
        NEXT
      </button>
    </div>
  )
}
