import { Link } from 'react-router-dom'
import { AlertTriangle, ArrowLeft, RefreshCw } from 'lucide-react'
import { StatusScreenLayout } from '@/components/layout/StatusScreenLayout'
import { StatusReadout } from '@/components/ui/StatusReadout'

export const ErrorPage = () => (
  <StatusScreenLayout title="Error" glow="bottom">
    <p className="font-mono text-[10px] uppercase tracking-[0.5em] text-neon-pink">System Failure · Node Crashed</p>
    <div className="mx-auto mt-8 flex h-24 w-24 animate-pulse items-center justify-center border-2 border-neon-pink">
      <AlertTriangle className="h-10 w-10 text-neon-pink" />
    </div>
    <h1 className="mt-8 font-display text-7xl uppercase leading-none tracking-tighter md:text-9xl">
      <span className="text-neon-pink">5</span>
      <span className="italic">0</span>
      <span className="text-neon-pink">0</span>
    </h1>
    <h2 className="mt-4 font-display text-2xl uppercase tracking-tighter md:text-3xl">Critical Error</h2>
    <StatusReadout
      lines={[
        { label: 'status', value: 'FAILED', tone: 'pink' },
        { label: 'reason', value: 'unhandled exception', tone: 'pink' },
        { label: 'action', value: 'retry available', tone: 'cyan' }
      ]}
    />
    <p className="mx-auto mt-6 max-w-md text-sm text-muted-foreground">
      A signal collapse interrupted your request. Retry the transmission or return to base.
    </p>
    <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
      <button
        type="button"
        onClick={() => window.location.reload()}
        className="flex items-center gap-2 bg-neon-cyan px-6 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
      >
        <RefreshCw className="h-3.5 w-3.5" /> Retry Transmission
      </button>
      <Link
        to="/"
        className="flex items-center gap-2 border border-border px-6 py-3 text-xs font-black uppercase tracking-widest hover:border-neon-cyan hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Return to Grid
      </Link>
    </div>
  </StatusScreenLayout>
)
