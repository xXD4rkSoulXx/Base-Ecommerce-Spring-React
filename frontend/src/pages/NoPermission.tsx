import { Link } from "react-router-dom";
import { Lock, ArrowLeft, ShieldAlert } from "lucide-react";

const NoPermission = () => {
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
        className="pointer-events-none absolute -top-32 left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full opacity-30 blur-3xl"
        style={{ background: "hsl(var(--neon-pink))" }}
      />

      <div className="relative z-10 mx-auto max-w-2xl text-center">
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
        <h2 className="mt-4 font-display text-2xl uppercase tracking-tighter md:text-3xl">
          Rejected
        </h2>

        <div className="mx-auto mt-6 max-w-md border border-border bg-card p-4 text-left font-mono text-xs">
          <p className="text-muted-foreground">
            <span className="text-neon-pink">●</span> clearance :: <span className="text-foreground">REJECTED</span>
          </p>
          <p className="mt-1 text-muted-foreground">
            <span className="text-neon-cyan">●</span> required :: <span className="text-foreground">OPERATOR</span>
          </p>
          <p className="mt-1 text-muted-foreground">
            <span className="text-neon-cyan">●</span> incident :: <span className="text-foreground">logged</span>
          </p>
        </div>

        <p className="mx-auto mt-6 max-w-md text-sm text-muted-foreground">
          Your credentials don't grant entry to this node. Authenticate as an
          operator or return to public terrain.
        </p>

        <div className="mt-10 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Link
            to="/login"
            className="flex items-center gap-2 bg-neon-cyan px-6 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
          >
            <ShieldAlert className="h-3.5 w-3.5" /> Sign In as Operator
          </Link>
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

export default NoPermission;
