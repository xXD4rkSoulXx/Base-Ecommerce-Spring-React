import { useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ArrowLeft, Radio } from 'lucide-react'

function NotFound() {
  const location = useLocation();

  useEffect(() => {
    console.error("404 Error: User attempted to access non-existent route: ", location.pathname);
  }, [location.pathname]);

  return (
    <div className="relative flex min-h-dvh items-center justify-center overflow-hidden bg-background px-6 text-foreground">
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.07]"
        style={{
          backgroundImage: "linear-gradient(hsl(var(--foreground)) 1px, transparent 1px), linear-gradient(90deg, hsl(var(--foreground)) 1px, transparent 1px)",
          backgroundSize: "48px 48px"
        }}
      />
      <div
        className="pointer-events-none absolute -top-32 left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full opacity-30 blur-3xl"
        style={{ background: "hsl(var(--neon-pink))" }}
      />
      <div className="relative z-10 mx-auto max-w-2xl text-center">
        <p className="font-mono text-[10px] uppercase tracking-[0.5em] text-neon-cyan">
          Signal Lost · Node Unreachable
        </p>
        <h1 className="mt-6 font-display text-[28vw] leading-none tracking-tighter md:text-[14rem]">
          <span className="text-neon-pink">4</span>
          <span className="italic">0</span>
          <span className="text-neon-cyan">4</span>
        </h1>
        <p className="mt-4 font-mono text-xs uppercase tracking-widest text-muted-foreground">
          path :: <span className="text-foreground">{location.pathname}</span>
        </p>
        <p className="mx-auto mt-6 max-w-md text-sm text-muted-foreground">
          The route you tried to access has been redacted from the grid, or it
          never existed in this timeline.
        </p>
        <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Link
            to="/"
            className="flex items-center gap-2 bg-neon-cyan px-6 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
          >
            <ArrowLeft className="h-3.5 w-3.5" /> Return to Grid
          </Link>
          <Link
            to="/shop"
            className="flex items-center gap-2 border border-border px-6 py-3 text-xs font-black uppercase tracking-widest hover:border-neon-cyan hover:text-neon-cyan"
          >
            <Radio className="h-3.5 w-3.5" /> Browse Drops
          </Link>
        </div>
      </div>
    </div>
  )
}

export default NotFound
