import { Loader2 } from "lucide-react"

function Loading() {
  return (
    <div className="relative flex min-h-dvh items-center justify-center overflow-hidden bg-background text-foreground">
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.06]"
        style={{
          backgroundImage: "linear-gradient(hsl(var(--foreground)) 1px, transparent 1px), linear-gradient(90deg, hsl(var(--foreground)) 1px, transparent 1px)",
          backgroundSize: "48px 48px"
        }}
      />
      <div className="relative z-10 flex flex-col items-center gap-6 text-center">
        <div className="relative flex h-20 w-20 items-center justify-center">
          <span className="absolute inset-0 animate-ping rounded-full border border-neon-cyan/40" />
          <span className="absolute inset-2 rounded-full border border-neon-cyan/30" />
          <Loader2 className="h-8 w-8 animate-spin text-neon-cyan" />
        </div>
        <div>
          <p className="font-mono text-[10px] uppercase tracking-[0.5em] text-neon-cyan">
            Buffering Signal
          </p>
          <h1 className="mt-3 font-display text-3xl uppercase tracking-tighter md:text-4xl">
            Syncing Grid…
          </h1>
          <p className="mt-3 font-mono text-xs uppercase tracking-widest text-muted-foreground">
            connecting to node · stand by
          </p>
        </div>
        <div className="h-1 w-56 overflow-hidden bg-secondary">
          <div className="h-full w-1/2 animate-[loadbar_1.4s_ease-in-out_infinite] bg-neon-cyan" />
        </div>
      </div>
      <style>{`
        @keyframes loadbar {
          0% { transform: translateX(-100%); }
          100% { transform: translateX(220%); }
        }
      `}</style>
    </div>
  )
}

export default Loading
