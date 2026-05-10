import { Link, useNavigate } from "react-router-dom";
import { AlertTriangle, RefreshCw, ArrowLeft } from "lucide-react";

const Error = () => {
  const navigate = useNavigate();

  return (
    <div className="relative flex min-h-dvh items-center justify-center overflow-hidden bg-background px-6 text-foreground">
      <div
        className="pointer-events-none absolute inset-0 opacity-[0.07]"
        style={{
          backgroundImage:
            "linear-gradient(hsl(var(--foreground)) 1px, transparent 1px), linear-gradient(90deg, hsl(var(--foreground)) 1px, transparent 1px)",
          backgroundSize: "48px 48px",
        }}
      />
      <div
        className="pointer-events-none absolute -bottom-32 left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full opacity-30 blur-3xl"
        style={{ background: "hsl(var(--neon-pink))" }}
      />

      <div className="relative z-10 mx-auto max-w-2xl text-center">
        <p className="font-mono text-[10px] uppercase tracking-[0.5em] text-neon-pink">
          System Failure · Node Crashed
        </p>

        <div className="mx-auto mt-8 flex h-24 w-24 items-center justify-center border-2 border-neon-pink animate-pulse">
          <AlertTriangle className="h-10 w-10 text-neon-pink" />
        </div>

        <h1 className="mt-8 font-display text-7xl uppercase leading-none tracking-tighter md:text-9xl">
          <span className="text-neon-pink">5</span>
          <span className="italic">0</span>
          <span className="text-neon-pink">0</span>
        </h1>
        <h2 className="mt-4 font-display text-2xl uppercase tracking-tighter md:text-3xl">
          Critical Error
        </h2>

        <div className="mx-auto mt-6 max-w-md border border-border bg-card p-4 text-left font-mono text-xs">
          <p className="text-muted-foreground">
            <span className="text-neon-pink">●</span> status :: <span className="text-foreground">FAILED</span>
          </p>
          <p className="mt-1 text-muted-foreground">
            <span className="text-neon-pink">●</span> reason :: <span className="text-foreground">unhandled exception</span>
          </p>
          <p className="mt-1 text-muted-foreground">
            <span className="text-neon-cyan">●</span> action :: <span className="text-foreground">retry available</span>
          </p>
        </div>

        <p className="mx-auto mt-6 max-w-md text-sm text-muted-foreground">
          A signal collapse interrupted your request. Retry the transmission or
          return to base.
        </p>

        <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <button
            onClick={() => navigate(0)}
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
      </div>
    </div>
  );
};

export default Error;
