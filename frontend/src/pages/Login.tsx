import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { ArrowRight, Lock, Mail, Play } from 'lucide-react'
import { AuthPageLayout } from '@/components/layout/AuthPageLayout'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { APP_NAME } from '@/constants'
import { useLogin } from '@/hooks/useLogin'
import { useStartDemo } from '@/hooks/useStartDemo'
import { loginSchema, type LoginForm } from '@/schemas/auth'
import { getErrorMessage } from '@/utils/error'

export const Login = () => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' }
  })

  const { mutate: login, isPending, isError, error } = useLogin()
  const { mutate: startDemo, isPending: isDemoPending, isError: isDemoError } = useStartDemo()
  const isBusy = isPending || isDemoPending

  return (
    <AuthPageLayout
      side="left"
      tone="cyan"
      asideTitle={
        <>
          Re_enter <br />
          the <span className="text-neon-cyan">Grid</span>.
        </>
      }
      asideDescription={`Operator credentials required. Access your archive, transit logs and wishlist across the ${APP_NAME} network.`}
    >
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-6xl">
        Sign <span className="text-neon-cyan">In</span>
      </h1>
      <p className="mt-3 text-sm text-muted-foreground">Welcome back. Authenticate to resume your session.</p>
      <form className="mt-10 space-y-6" onSubmit={handleSubmit((data) => login(data))} noValidate>
        {isError && <FormError message={getErrorMessage(error, 'Invalid credentials')} />}
        <Field
          label="Email"
          icon={<Mail className="h-4 w-4" />}
          type="email"
          autoComplete="email"
          disabled={isBusy}
          placeholder="operator@signal.com"
          error={errors.email?.message}
          {...register('email')}
        />
        <Field
          label="Password"
          icon={<Lock className="h-4 w-4" />}
          type="password"
          autoComplete="current-password"
          disabled={isBusy}
          placeholder="••••••••••"
          error={errors.password?.message}
          {...register('password')}
        />
        <button
          type="submit"
          disabled={isBusy}
          className="group flex w-full items-center justify-center gap-3 border border-neon-cyan bg-neon-cyan py-4 text-xs font-bold uppercase tracking-[0.3em] text-primary-foreground transition-all hover:bg-transparent hover:text-neon-cyan disabled:cursor-not-allowed disabled:opacity-50"
        >
          {isPending ? 'Authenticating...' : 'Authenticate'}
          {!isPending && <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-1" />}
        </button>
      </form>
      <div className="mt-8 space-y-3 border-t border-border pt-8">
        {isDemoError && <FormError message="Could not start the demo" />}
        <button
          type="button"
          onClick={() => startDemo()}
          disabled={isBusy}
          className="group flex w-full items-center justify-center gap-3 border border-neon-pink py-4 text-xs font-bold uppercase tracking-[0.3em] text-neon-pink transition-all hover:bg-neon-pink hover:text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50"
        >
          <Play className="h-4 w-4" />
          {isDemoPending ? 'Loading demo...' : 'Try the admin demo'}
        </button>
        <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          Read-only tour of the admin console with fictional data. No account needed.
        </p>
      </div>
      <p className="mt-10 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
        New operator?{' '}
        <Link to="/register" className="text-neon-cyan hover:text-neon-pink">
          Request access →
        </Link>
      </p>
    </AuthPageLayout>
  )
}
