import { useState, type SubmitEvent } from 'react'
import { toast } from 'sonner'
import { Shield, UserPlus } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { Modal } from '@/components/ui/Modal'
import { useAdminAssignRole } from '@/hooks/admin/useAdminAssignRole'
import type { AdminTeamMemberResponse } from '@/types/admin/adminTeam'
import type { UserRole } from '@/types/user'
import { getErrorMessage } from '@/utils/error'

interface AdminAssignRoleDialogProps {
  member: AdminTeamMemberResponse | null
  onClose: () => void
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
  { value: 'CUSTOMER', description: 'Remove from the team' }
]

export const AdminAssignRoleDialog = ({ member, onClose }: AdminAssignRoleDialogProps) => {
  const [email, setEmail] = useState('')
  const [role, setRole] = useState<UserRole>(member?.role ?? 'SUPPORT')
  const [error, setError] = useState<string | null>(null)
  const { mutate: assignRole, isPending } = useAdminAssignRole()

  const isChange = member !== null
  const options = isChange ? ROLE_OPTIONS : ROLE_OPTIONS.filter((option) => option.value !== 'CUSTOMER')

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

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
          onClose()
        },
        onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not update the role'))
      }
    )
  }

  return (
    <Modal
      eyebrow={isChange ? 'Reassign' : 'Recruit'}
      title={isChange ? 'Change Role' : 'Add Team Member'}
      description={
        isChange
          ? `${member.firstName} ${member.lastName} · ${member.email}`
          : 'Promote a registered customer by email.'
      }
      icon={
        isChange ? (
          <Shield className="h-5 w-5 text-neon-cyan" />
        ) : (
          <UserPlus className="h-5 w-5 text-neon-cyan" />
        )
      }
      onClose={onClose}
    >
      <form onSubmit={submit}>
        {error && (
          <div className="mb-6">
            <FormError message={error} />
          </div>
        )}
        <div className="space-y-4">
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
                  role === option.value ? 'border-neon-cyan bg-neon-cyan/10' : 'border-border hover:border-foreground/40'
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
            type="button"
            onClick={onClose}
            disabled={isPending}
            className="flex-1 border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isPending}
            className="flex-1 bg-neon-cyan py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            {isChange ? 'Apply' : 'Promote'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
