import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { KeyRound } from 'lucide-react'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useChangePassword } from '@/hooks/useChangePassword'
import { changePasswordSchema, type ChangePasswordForm } from '@/schemas/auth'
import { getErrorMessage } from '@/utils/error'

interface ChangePasswordDialogProps {
  onClose: () => void
}

export const ChangePasswordDialog = ({ onClose }: ChangePasswordDialogProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<ChangePasswordForm>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' }
  })

  const { mutate: changePassword, isPending, error } = useChangePassword()

  const onSubmit = ({ currentPassword, newPassword }: ChangePasswordForm) =>
    changePassword({ currentPassword, newPassword })

  return (
    <Modal
      eyebrow="Security"
      title="Change Password"
      icon={<KeyRound className="h-5 w-5 text-neon-pink" />}
      tone="pink"
      onClose={onClose}
    >
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        {error && (
          <div className="mb-6">
            <FormError message={getErrorMessage(error, 'Could not change password')} />
          </div>
        )}
        <div className="space-y-4">
          <Field
            label="Current Password"
            type="password"
            autoComplete="current-password"
            disabled={isPending}
            error={errors.currentPassword?.message}
            {...register('currentPassword')}
          />
          <Field
            label="New Password"
            type="password"
            autoComplete="new-password"
            disabled={isPending}
            error={errors.newPassword?.message}
            {...register('newPassword')}
          />
          <Field
            label="Confirm New Password"
            type="password"
            autoComplete="new-password"
            disabled={isPending}
            error={errors.confirmPassword?.message}
            {...register('confirmPassword')}
          />
          <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            You will be signed out of all devices
          </p>
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
            className="flex-1 bg-neon-pink py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            Update
          </button>
        </div>
      </form>
    </Modal>
  )
}
