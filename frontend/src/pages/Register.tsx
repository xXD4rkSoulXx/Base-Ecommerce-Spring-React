import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { ArrowRight, Lock, Mail, User } from 'lucide-react'
import { AuthPageLayout } from '@/components/layout/AuthPageLayout'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { Perk } from '@/components/ui/Perk'
import { useRegister } from '@/hooks/useRegister'
import { registerSchema, type RegisterForm } from '@/schemas/auth'
import { getErrorMessage } from '@/utils/error'

const PERKS = [
  { label: '01', text: 'Cart synced across all your devices' },
  { label: '02', text: 'Discount codes unlocked at checkout' },
  { label: '03', text: "Reviews on products you've received" }
]

export const Register = () => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: { firstName: '', lastName: '', email: '', password: '', confirmPassword: '', accept: false }
  })

  const { mutate, isPending, error } = useRegister()

  const onSubmit = ({ email, password, firstName, lastName }: RegisterForm) =>
    mutate({ email, password, firstName, lastName })

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
      <form className="mt-10 space-y-6" onSubmit={handleSubmit(onSubmit)} noValidate>
        {error && <FormError message={getErrorMessage(error, 'Could not create the account')} />}
        <Field
          label="First name"
          icon={<User className="h-4 w-4" />}
          autoComplete="given-name"
          disabled={isPending}
          placeholder="Alex"
          error={errors.firstName?.message}
          {...register('firstName')}
        />
        <Field
          label="Last name"
          icon={<User className="h-4 w-4" />}
          autoComplete="family-name"
          disabled={isPending}
          placeholder="Kovacs"
          error={errors.lastName?.message}
          {...register('lastName')}
        />
        <Field
          label="Email"
          icon={<Mail className="h-4 w-4" />}
          type="email"
          autoComplete="email"
          disabled={isPending}
          placeholder="operator@signal.com"
          error={errors.email?.message}
          {...register('email')}
        />
        <Field
          label="Password"
          icon={<Lock className="h-4 w-4" />}
          type="password"
          autoComplete="new-password"
          disabled={isPending}
          placeholder="8+ chars: upper, lower, number"
          error={errors.password?.message}
          {...register('password')}
        />
        <Field
          label="Confirm Password"
          icon={<Lock className="h-4 w-4" />}
          type="password"
          autoComplete="new-password"
          disabled={isPending}
          placeholder="Repeat password"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />
        <div>
          <label className="flex items-start gap-3 text-xs">
            <input
              type="checkbox"
              disabled={isPending}
              aria-invalid={!!errors.accept}
              className="mt-0.5 h-3 w-3 accent-neon-pink"
              {...register('accept')}
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
          {errors.accept && <p className="mt-2 font-mono text-[10px] text-destructive">{errors.accept.message}</p>}
        </div>
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
