import { useState } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { Shield, UserPlus, X } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { useAssignRole } from '@/hooks/admin/useAssignRole'
import type { TeamMemberResponse } from '@/types/admin/team'
import type { UserRole } from '@/types/user'

interface AssignRoleDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  member: TeamMemberResponse | null
}

interface RoleOption {
  value: UserRole
  description: string
}

const ROLE_OPTIONS: RoleOption[] = [
  { value: 'ADMIN', description: 'Full access, including the team' },
  { value: 'MANAGER', description: 'Everything except changing roles' },
  { value: 'MERCHANDISER', description: 'Catalog, discounts and reviews' },
  { value: 'FULFILLMENT', description: 'Orders and stock' },
  { value: 'SUPPORT', description: 'Customers, orders, returns and reviews' },
  { value: 'DEMO', description: 'Read-only demo access' },
  { value: 'CUSTOMER', description: 'Remove from the team' }
]

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AssignRoleDialog = ({ open, onOpenChange, member }: AssignRoleDialogProps) => {
  const initialRole = member?.role ?? 'SUPPORT'
  const [email, setEmail] = useState('')
  const [role, setRole] = useState<UserRole>(initialRole)
  const [error, setError] = useState<string | null>(null)
  const { mutate: assignRole, isPending } = useAssignRole()

  if (!open) {
    return null
  }

  const isChange = member !== null
  const options = isChange
    ? ROLE_OPTIONS
    : ROLE_OPTIONS.filter((option) => option.value !== 'CUSTOMER')

  const close = () => {
    setEmail('')
    setRole(initialRole)
    setError(null)
    onOpenChange(false)
  }

  const submit = () => {
    const targetEmail = member?.email ?? email.trim()

    if (!targetEmail) {
      setError('Email is required')
      return
    }

    if (role === member?.role) {
      setError('Select a different role')
      return
    }

    setError(null)

    assignRole(
      { email: targetEmail, role },
      {
        onSuccess: (updated) => {
          toast.success(
            updated.role === 'CUSTOMER'
              ? `${updated.email} removed from the team`
              : `${updated.email} is now ${updated.role}`
          )
          close()
        },
        onError: (mutationError) =>
          setError(getErrorMessage(mutationError, 'Could not update the role'))
      }
    )
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <div className="max-h-[90dvh] w-full max-w-md overflow-y-auto border border-neon-cyan/40 bg-card p-6 shadow-2xl shadow-neon-cyan/10">
        <div className="flex items-start justify-between">
          <div>
            <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-cyan">
              {isChange ? 'Reassign' : 'Recruit'}
            </p>
            <h2 className="mt-2 flex items-center gap-2 font-display text-2xl uppercase tracking-tighter">
              {isChange ? (
                <Shield className="h-5 w-5 text-neon-cyan" />
              ) : (
                <UserPlus className="h-5 w-5 text-neon-cyan" />
              )}
              {isChange ? 'Change Role' : 'Add Team Member'}
            </h2>
            <p className="mt-2 text-xs text-muted-foreground">
              {isChange
                ? `${member.firstName} ${member.lastName} · ${member.email}`
                : 'Promote a registered customer by email.'}
            </p>
          </div>
          <button
            onClick={close}
            aria-label="Close"
            className="text-muted-foreground hover:text-neon-cyan"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        {error && (
          <div className="mt-6">
            <FormError message={error} />
          </div>
        )}
        <div className="mt-6 space-y-4">
          {!isChange && (
            <FormField
              label="Email"
              value={email}
              onChange={setEmail}
              accent="cyan"
              autoComplete="off"
              disabled={isPending}
            />
          )}
          <div className="space-y-2">
            {options.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => setRole(option.value)}
                disabled={isPending}
                className={`flex w-full items-center justify-between border p-4 text-left transition-colors ${
                  role === option.value
                    ? 'border-neon-cyan bg-neon-cyan/10'
                    : 'border-border hover:border-foreground/40'
                }`}
              >
                <div>
                  <p className="font-display text-lg uppercase tracking-tighter">{option.value}</p>
                  <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                    {option.description}
                  </p>
                </div>
                {member?.role === option.value && (
                  <span className="font-mono text-[9px] uppercase tracking-widest text-neon-pink">Current</span>
                )}
              </button>
            ))}
          </div>
        </div>
        <div className="mt-8 flex gap-3">
          <button
            onClick={close}
            disabled={isPending}
            className="flex-1 border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={isPending}
            className="flex-1 bg-neon-cyan py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            {isChange ? 'Apply' : 'Promote'}
          </button>
        </div>
      </div>
    </div>
  )
}
