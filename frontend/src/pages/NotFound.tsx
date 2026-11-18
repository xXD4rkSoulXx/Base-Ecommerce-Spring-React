import { Link, useLocation } from 'react-router-dom'
import { ArrowLeft, Radio } from 'lucide-react'
import { StatusScreenLayout } from '@/components/layout/StatusScreenLayout'

export const NotFound = () => {
  const location = useLocation()

  return (
    <StatusScreenLayout glow="top">
      <p className="font-mono text-[10px] uppercase tracking-[0.5em] text-neon-cyan">Signal Lost · Node Unreachable</p>
      <h1 className="mt-6 font-display text-[28vw] leading-none tracking-tighter md:text-[14rem]">
        <span className="text-neon-pink">4</span>
        <span className="italic">0</span>
        <span className="text-neon-cyan">4</span>
      </h1>
      <p className="mt-4 font-mono text-xs uppercase tracking-widest text-muted-foreground">
        path :: <span className="text-foreground">{location.pathname}</span>
      </p>
      <p className="mx-auto mt-6 max-w-md text-sm text-muted-foreground">
        The route you tried to access has been redacted from the grid, or it never existed in this timeline.
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
    </StatusScreenLayout>
  )
}
