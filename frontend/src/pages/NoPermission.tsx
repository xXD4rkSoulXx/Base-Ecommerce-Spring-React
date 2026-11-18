import { Link } from 'react-router-dom'
import { ArrowLeft, Lock } from 'lucide-react'
import { StatusScreenLayout } from '@/components/layout/StatusScreenLayout'
import { StatusReadout } from '@/components/ui/StatusReadout'

export const NoPermission = () => (
  <StatusScreenLayout glow="top">
    <p className="font-mono text-[10px] uppercase tracking-[0.5em] text-neon-pink">
      Access Denied · Clearance Insufficient
    </p>
    <div className="mx-auto mt-8 flex h-28 w-28 items-center justify-center border-2 border-neon-pink">
      <Lock className="h-12 w-12 text-neon-pink" />
    </div>
    <h1 className="mt-8 font-display text-7xl uppercase leading-none tracking-tighter md:text-9xl">
      <span className="text-neon-pink">4</span>
      <span className="italic">0</span>
      <span className="text-neon-pink">3</span>
    </h1>
    <h2 className="mt-4 font-display text-2xl uppercase tracking-tighter md:text-3xl">Rejected</h2>
    <StatusReadout
      lines={[
        { label: 'clearance', value: 'REJECTED', tone: 'pink' },
        { label: 'required', value: 'STAFF ACCESS', tone: 'cyan' },
        { label: 'incident', value: 'logged', tone: 'cyan' }
      ]}
    />
    <p className="mx-auto mt-6 max-w-md text-sm text-muted-foreground">
      Your role doesn't grant entry to this area. Ask an administrator if you think this is a mistake.
    </p>
    <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
      <Link
        to="/"
        className="flex items-center gap-2 border border-border px-6 py-3 text-xs font-black uppercase tracking-widest hover:border-neon-cyan hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Return to Grid
      </Link>
    </div>
  </StatusScreenLayout>
)
