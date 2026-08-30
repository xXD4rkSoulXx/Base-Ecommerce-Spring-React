export const ReviewLockedNotice = ({ message }: { message: string }) => (
  <div className="flex items-center gap-3 border border-dashed border-border bg-card/40 px-5 py-4 text-xs">
    <svg
      className="h-4 w-4 text-neon-pink flex-shrink-0"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
      strokeWidth={2}
    >
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
    </svg>
    <span className="font-mono uppercase tracking-widest text-muted-foreground">
      {message}
    </span>
  </div>
)
