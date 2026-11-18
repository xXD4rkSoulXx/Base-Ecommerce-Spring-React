import { zodResolver } from '@hookform/resolvers/zod'
import { Controller, useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { Shield, UserPlus } from 'lucide-react'
import { Field } from '@/components/ui/Field'
import { FieldError } from '@/components/ui/FieldError'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useAdminAssignRole } from '@/hooks/admin/useAdminAssignRole'
import { assignRoleSchema, type AssignRoleFormValues } from '@/schemas/admin/adminTeam'
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
  const {
    register,
    control,
    handleSubmit,
    formState: { errors }
  } = useForm<AssignRoleFormValues>({
    resolver: zodResolver(assignRoleSchema(member?.role)),
    defaultValues: { email: member?.email ?? '', role: member?.role ?? 'SUPPORT' }
  })

  const { mutate: assignRole, isPending, error } = useAdminAssignRole()

  const isChange = member !== null
  const options = isChange ? ROLE_OPTIONS : ROLE_OPTIONS.filter((option) => option.value !== 'CUSTOMER')

  const onSubmit = (data: AssignRoleFormValues) =>
    assignRole(data, {
      onSuccess: (updated) => {
        toast.success(
          updated.role === 'CUSTOMER'
            ? `${updated.email} removed from the team`
            : `${updated.email} is now ${updated.role}`
        )
        onClose()
      }
    })

  return (
    <Modal
      eyebrow={isChange ? 'Reassign' : 'Recruit'}
      title={isChange ? 'Change Role' : 'Add Team Member'}
      description={
        isChange
          ? `${member.firstName} ${member.lastName} · ${member.email}`
          : 'Promote a registered customer by email.'
      }
      icon={isChange ? <Shield className="h-5 w-5 text-neon-cyan" /> : <UserPlus className="h-5 w-5 text-neon-cyan" />}
      onClose={onClose}
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        {error && (
          <div className="mb-6">
            <FormError message={getErrorMessage(error, 'Could not update the role')} />
          </div>
        )}
        <div className="space-y-4">
          {!isChange && (
            <Field
              label="Email"
              type="email"
              accent="cyan"
              autoComplete="off"
              disabled={isPending}
              error={errors.email?.message}
              {...register('email')}
            />
          )}
          <Controller
            control={control}
            name="role"
            render={({ field }) => (
              <div className="space-y-2">
                {options.map((option) => (
                  <button
                    key={option.value}
                    type="button"
                    onClick={() => field.onChange(option.value)}
                    disabled={isPending}
                    className={`flex w-full items-center justify-between border p-4 text-left transition-colors ${
                      field.value === option.value
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
                <FieldError message={errors.role?.message} />
              </div>
            )}
          />
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
