import { useState } from 'react'
import { isAxiosError } from 'axios'
import { KeyRound, X } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { useChangePassword } from '@/hooks/useChangePassword'

interface ChangePasswordDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
}

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

const getValidationError = (
  currentPassword: string,
  newPassword: string,
  confirmPassword: string
) => {
  if (!currentPassword || !newPassword || !confirmPassword) {
    return 'All fields are required'
  }

  if (newPassword.length < 8) {
    return `Password must be at least 8 characters long`
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

export const ChangePasswordDialog = ({ open, onOpenChange }: ChangePasswordDialogProps) => {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const { mutate: changePassword, isPending } = useChangePassword()

  if (!open) {
    return null
  }

  const close = () => {
    setCurrentPassword('')
    setNewPassword('')
    setConfirmPassword('')
    setError(null)
    onOpenChange(false)
  }

  const submit = () => {
    const validationError = getValidationError(currentPassword, newPassword, confirmPassword)

    if (validationError) {
      setError(validationError)
      return
    }

    setError(null)

    changePassword(
      { currentPassword, newPassword },
      {
        onError: (mutationError) =>
          setError(getErrorMessage(mutationError, 'Could not change password'))
      }
    )
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <div className="w-full max-w-md border border-neon-pink/40 bg-card p-6 shadow-2xl shadow-neon-pink/10">
        <div className="flex items-start justify-between">
          <div>
            <h2 className="mt-2 flex items-center gap-2 font-display text-2xl uppercase tracking-tighter">
              <KeyRound className="h-5 w-5 text-neon-pink" /> Change Password
            </h2>
          </div>
          <button
            onClick={close}
            aria-label="Close"
            className="text-muted-foreground hover:text-neon-pink"
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
            onClick={close}
            disabled={isPending}
            className="flex-1 border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={submit}
            disabled={isPending}
            className="flex-1 bg-neon-pink py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            Update
          </button>
        </div>
      </div>
    </div>
  )
}
