import { useState } from 'react'
import type { SubmitEvent } from 'react'
import { Link } from 'react-router-dom'
import { AlertTriangle, ArrowRight, Lock, Mail, User } from 'lucide-react'
import type { AxiosError } from 'axios'
import { useRegister } from '@/hooks/useRegister'
import { Field } from '@/components/ui/Field'
import { Perk } from '@/components/ui/Perk'

export const Register = () => {
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [accept, setAccept] = useState(false)
  const [validationError, setValidationError] = useState<string | null>(null)
  
  const { mutate, isPending, isError, error } = useRegister()

  const errorMessage = validationError
    || (error as AxiosError<{ message: string }>)?.response?.data?.message
    || error?.message

  const handleRegister = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()
    setValidationError(null)
    
    if (password !== confirmPassword) {
      setValidationError('Passwords do not match.')

      return
    }
    
    if (!accept) {
      setValidationError('You must accept the Network Protocol to continue.')

      return
    }

    mutate({ email, password, firstName, lastName })
  }

  return (
    <div className="relative mx-auto grid min-h-[calc(100vh-80px)] max-w-[1440px] gap-0 px-6 py-16 md:px-10 lg:grid-cols-2 lg:gap-16">
      <section className="flex flex-col justify-center order-2 lg:order-1">
        <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-6xl">
          Join the <span className="text-neon-pink">Signal</span>
        </h1>
        <p className="mt-3 text-sm text-muted-foreground">
          Provision your operator profile and unlock the full archive.
        </p>
        {(validationError || isError) && (
          <div
            role="alert"
            className="mt-8 flex items-start gap-3 border border-destructive/60 bg-destructive/10 px-4 py-3 text-xs text-destructive"
          >
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
            <div>
              <p className="mt-1 font-mono">
                {errorMessage}
              </p>
            </div>
          </div>
        )}
        <form className="mt-10 space-y-6" onSubmit={handleRegister}>
          <Field
            label="First name"
            icon={<User className="h-4 w-4" />}
            type="text"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            disabled={isPending}
            placeholder="Alex"
          />
          <Field
            label="Last name"
            icon={<User className="h-4 w-4" />}
            type="text"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            disabled={isPending}
            placeholder="Kovacs"
          />
          <Field
            label="Email"
            icon={<Mail className="h-4 w-4" />}
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isPending}
            placeholder="operator@signal.com"
          />
          <Field
            label="Password"
            icon={<Lock className="h-4 w-4" />}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isPending}
            placeholder="Min. 8 characters"
          />
          <Field
            label="Confirm Password"
            icon={<Lock className="h-4 w-4" />}
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            disabled={isPending}
            placeholder="Repeat password"
          />
          <label className="flex items-start gap-3 text-xs">
            <input
              type="checkbox"
              checked={accept}
              onChange={(e) => setAccept(e.target.checked)}
              className="mt-0.5 h-3 w-3 accent-neon-pink"
            />
            <span className="font-mono uppercase tracking-widest leading-relaxed text-muted-foreground">
              I accept the{" "}
              <Link to="/about" className="text-neon-cyan hover:text-neon-pink">
                Network Protocol
              </Link>{" "}
              and Transit Policy.
            </span>
          </label>
          <button
            type="submit"
            disabled={isPending}
            className="group flex w-full items-center justify-center gap-3 border border-neon-pink bg-neon-pink py-4 text-xs font-bold uppercase tracking-[0.3em] text-primary-foreground transition-all hover:bg-transparent hover:text-neon-pink"
          >
            Create Operator
            <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />
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
          Already an operator?{" "}
          <Link to="/login" className="text-neon-cyan hover:text-neon-pink">
            Sign in →
          </Link>
        </p>
      </section>
      <aside className="relative hidden overflow-hidden border border-border bg-card lg:block lg:order-2">
        <div className="grid-noise absolute inset-0 opacity-40" />
        <div className="absolute inset-0 bg-gradient-to-tl from-transparent via-transparent to-neon-pink/15" />
        <div className="relative flex h-full flex-col justify-between p-10">
          <div>
            <h2 className="mt-6 font-display text-6xl uppercase leading-[0.9] tracking-tighter">
              Drop_in. <br />
              Suit <span className="text-neon-pink">up</span>.
            </h2>
            <p className="mt-6 max-w-sm text-sm text-muted-foreground">
              Get early-access drops, transit tracking and Lab releases delivered
              straight to your console.
            </p>
          </div>
          <div className="space-y-3 border-t border-border pt-6">
            <Perk label="01" text="Cart synced across all your devices" />
            <Perk label="02" text="Discount codes unlocked at checkout" />
            <Perk label="03" text="Reviews on products you've received" />
          </div>
        </div>
      </aside>
    </div>
  )
}
