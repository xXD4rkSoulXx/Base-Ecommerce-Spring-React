import { useState } from 'react'
import type { SubmitEvent } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Lock, Mail } from 'lucide-react'
import type { AxiosError } from 'axios'
import { useLogin } from '@/hooks/useLogin'
import { Field } from '@/components/ui/Field'

export const Login = () => {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const { mutate, isPending, isError, error } = useLogin()

  const handleLogin = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    
    mutate({ email, password })
  }

  return (
    <div className="relative mx-auto grid min-h-[calc(100vh-80px)] max-w-[1440px] gap-0 px-6 py-16 md:px-10 lg:grid-cols-2 lg:gap-16">
      <aside className="relative hidden overflow-hidden border border-border bg-card lg:block">
        <div className="grid-noise absolute inset-0 opacity-40" />
        <div className="absolute inset-0 bg-gradient-to-br from-transparent via-transparent to-neon-cyan/10" />
        <div className="relative flex h-full flex-col justify-between p-10">
          <div>
            <h2 className="mt-6 font-display text-6xl uppercase leading-[0.9] tracking-tighter">
              Re_enter <br />
              the <span className="text-neon-cyan">Grid</span>.
            </h2>
            <p className="mt-6 max-w-sm text-sm text-muted-foreground">
              Operator credentials required. Access your archive, transit logs and
              saved drops across the Velo_Citi network.
            </p>
          </div>
        </div>
      </aside>
      <section className="flex flex-col justify-center">
        <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-6xl">
          Sign <span className="text-neon-cyan">In</span>
        </h1>
        <p className="mt-3 text-sm text-muted-foreground">
          Welcome back. Authenticate to resume your session.
        </p>
        <form className="mt-10 space-y-6" onSubmit={handleLogin}>
          {isError && (
            <div className="border border-red-500/30 bg-red-500/10 px-4 py-3 font-mono text-xs text-red-400 uppercase tracking-wider">
              {(error as AxiosError<{ message: string }>)?.response?.data?.message || "⚠️ Invalid Credentials"}
            </div>
          )}
          <Field
            label="Email"
            icon={<Mail className="h-4 w-4" />}
            type="email"
            name="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isPending}
            placeholder="operator@signal.com"
          />
          <Field
            label="Password"
            icon={<Lock className="h-4 w-4" />}
            type="password"
            name="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isPending}
            placeholder="••••••••••"
          />
          <div className="flex items-center justify-between text-xs">
            <label className="flex items-center gap-2 font-mono uppercase tracking-widest text-muted-foreground">
              <input type="checkbox" className="h-3 w-3 accent-neon-cyan" />
                Remember device
            </label>
            <Link to="/login" className="font-mono uppercase tracking-widest text-neon-cyan hover:text-neon-pink">
              Forgot key?
            </Link>
          </div>
          <button
            type="submit"
            disabled={isPending}
            className="group flex w-full items-center justify-center gap-3 border border-neon-cyan bg-neon-cyan py-4 text-xs font-bold uppercase tracking-[0.3em] text-primary-foreground transition-all hover:bg-transparent hover:text-neon-cyan disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isPending ? "Authenticating..." : "Authenticate"}
            {!isPending && <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />}
          </button>
          <div className="relative my-2 flex items-center">
            <div className="flex-1 border-t border-border" />
            <span className="px-4 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
              or
            </span>
            <div className="flex-1 border-t border-border" />
          </div>
          <button
            type="button"
            className="w-full border border-border py-4 text-xs font-bold uppercase tracking-[0.3em] text-foreground transition-colors hover:border-neon-cyan hover:text-neon-cyan"
          >
            Continue with Google
          </button>
        </form>
        <p className="mt-10 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
           New operator?{" "}
          <Link to="/register" className="text-neon-cyan hover:text-neon-pink">
            Request access →
          </Link>
        </p>
      </section>
    </div>
  )
}
