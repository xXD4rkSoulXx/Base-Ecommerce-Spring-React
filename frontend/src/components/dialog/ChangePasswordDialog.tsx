import { useState, type SubmitEvent } from 'react'
import { KeyRound } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { Modal } from '@/components/ui/Modal'
import { useChangePassword } from '@/hooks/useChangePassword'
import { getErrorMessage } from '@/utils/error'

interface ChangePasswordDialogProps {
  onClose: () => void
}

const getValidationError = (currentPassword: string, newPassword: string, confirmPassword: string) => {
  if (!currentPassword || !newPassword || !confirmPassword) {
    return 'All fields are required'
  }

  if (newPassword.length < 8 || newPassword.length > 72) {
    return 'Password must be between 8 and 72 characters long'
  }

  if (!/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d).+$/.test(newPassword)) {
    return 'Password must contain uppercase, lowercase and number'
  }

  if (newPassword !== confirmPassword) {
    return 'Passwords do not match'
  }

  if (newPassword === currentPassword) {
    return 'New password must be different from the current password'
  }

  return null
}

export const ChangePasswordDialog = ({ onClose }: ChangePasswordDialogProps) => {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { mutate: changePassword, isPending } = useChangePassword()

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    const validationError = getValidationError(currentPassword, newPassword, confirmPassword)

    if (validationError) {
      setError(validationError)
      return
    }

    setError(null)

    changePassword(
      { currentPassword, newPassword },
      { onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not change password')) }
    )
  }

  return (
    <Modal
      eyebrow="Security"
      title="Change Password"
      icon={<KeyRound className="h-5 w-5 text-neon-pink" />}
      tone="pink"
      onClose={onClose}
    >
      <form onSubmit={submit}>
        {error && (
          <div className="mb-6">
            <FormError message={error} />
          </div>
        )}
        <div className="space-y-4">
          <FormField
            label="Current Password"
            value={currentPassword}
            onChange={setCurrentPassword}
            type="password"
            accent="pink"
            autoComplete="current-password"
            disabled={isPending}
          />
          <FormField
            label="New Password"
            value={newPassword}
            onChange={setNewPassword}
            type="password"
            accent="pink"
            autoComplete="new-password"
            disabled={isPending}
          />
          <FormField
            label="Confirm New Password"
            value={confirmPassword}
            onChange={setConfirmPassword}
            type="password"
            accent="pink"
            autoComplete="new-password"
            disabled={isPending}
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
