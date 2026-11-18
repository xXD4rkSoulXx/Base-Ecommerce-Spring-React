import { useState, type SubmitEvent } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Lock, Mail, User } from 'lucide-react'
import { AuthPageLayout } from '@/components/layout/AuthPageLayout'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { Perk } from '@/components/ui/Perk'
import { useRegister } from '@/hooks/useRegister'
import { getErrorMessage } from '@/utils/error'

const PERKS = [
  { label: '01', text: 'Cart synced across all your devices' },
  { label: '02', text: 'Discount codes unlocked at checkout' },
  { label: '03', text: "Reviews on products you've received" }
]

export const Register = () => {
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [accept, setAccept] = useState(false)
  const [validationError, setValidationError] = useState<string | null>(null)

  const { mutate, isPending, error } = useRegister()

  const errorMessage = validationError ?? (error ? getErrorMessage(error, 'Could not create the account') : null)

  const handleRegister = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()
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
    <AuthPageLayout
      side="right"
      tone="pink"
      asideTitle={
        <>
          Drop_in. <br />
          Suit <span className="text-neon-pink">up</span>.
        </>
      }
      asideDescription="Track every parcel, manage returns and save your favorites."
      asideFooter={
        <div className="space-y-3">
          {PERKS.map((perk) => (
            <Perk key={perk.label} {...perk} />
          ))}
        </div>
      }
    >
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-6xl">
        Join the <span className="text-neon-pink">Signal</span>
      </h1>
      <p className="mt-3 text-sm text-muted-foreground">Provision your operator profile and unlock the full archive.</p>
      <form className="mt-10 space-y-6" onSubmit={handleRegister}>
        {errorMessage && <FormError message={errorMessage} />}
        <Field
          label="First name"
          icon={<User className="h-4 w-4" />}
          type="text"
          name="firstName"
          autoComplete="given-name"
          value={firstName}
          onChange={(event) => setFirstName(event.target.value)}
          disabled={isPending}
          maxLength={100}
          required
          placeholder="Alex"
        />
        <Field
          label="Last name"
          icon={<User className="h-4 w-4" />}
          type="text"
          name="lastName"
          autoComplete="family-name"
          value={lastName}
          onChange={(event) => setLastName(event.target.value)}
          disabled={isPending}
          maxLength={100}
          required
          placeholder="Kovacs"
        />
        <Field
          label="Email"
          icon={<Mail className="h-4 w-4" />}
          type="email"
          name="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          disabled={isPending}
          maxLength={254}
          required
          placeholder="operator@signal.com"
        />
        <Field
          label="Password"
          icon={<Lock className="h-4 w-4" />}
          type="password"
          name="password"
          autoComplete="new-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          disabled={isPending}
          minLength={8}
          maxLength={72}
          required
          placeholder="8+ chars: upper, lower, number"
        />
        <Field
          label="Confirm Password"
          icon={<Lock className="h-4 w-4" />}
          type="password"
          name="confirmPassword"
          autoComplete="new-password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          disabled={isPending}
          maxLength={72}
          required
          placeholder="Repeat password"
        />
        <label className="flex items-start gap-3 text-xs">
          <input
            type="checkbox"
            checked={accept}
            onChange={(event) => setAccept(event.target.checked)}
            disabled={isPending}
            className="mt-0.5 h-3 w-3 accent-neon-pink"
          />
          <span className="font-mono uppercase leading-relaxed tracking-widest text-muted-foreground">
            I accept the{' '}
            <Link to="/terms" className="text-neon-cyan hover:text-neon-pink">
              Terms
            </Link>{' '}
            and the{' '}
            <Link to="/privacy" className="text-neon-cyan hover:text-neon-pink">
              Privacy policy
            </Link>
            .
          </span>
        </label>
        <button
          type="submit"
          disabled={isPending}
          className="group flex w-full items-center justify-center gap-3 border border-neon-pink bg-neon-pink py-4 text-xs font-bold uppercase tracking-[0.3em] text-primary-foreground transition-all hover:bg-transparent hover:text-neon-pink disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isPending ? 'Creating...' : 'Create Operator'}
          {!isPending && <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />}
        </button>
      </form>
      <p className="mt-10 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
        Already an operator?{' '}
        <Link to="/login" className="text-neon-cyan hover:text-neon-pink">
          Sign in →
        </Link>
      </p>
    </AuthPageLayout>
  )
}
