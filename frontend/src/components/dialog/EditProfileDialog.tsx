import { useState } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { User, X } from 'lucide-react'
import { FormField } from '@/components/ui/FormField'
import { FormError } from '@/components/ui/FormError'
import { useUpdateProfile } from '@/hooks/useUpdateProfile'

interface EditProfileDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  initialFirstName: string
  initialLastName: string
}

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const EditProfileDialog = ({
  open,
  onOpenChange,
  initialFirstName,
  initialLastName
}: EditProfileDialogProps) => {
  const [firstName, setFirstName] = useState(initialFirstName)
  const [lastName, setLastName] = useState(initialLastName)
  const [error, setError] = useState<string | null>(null)
  const { mutate: updateProfile, isPending } = useUpdateProfile()

  if (!open) {
    return null
  }

  const close = () => {
    setFirstName(initialFirstName)
    setLastName(initialLastName)
    setError(null)
    onOpenChange(false)
  }

  const submit = () => {
    setError(null)

    if (!firstName.trim() || !lastName.trim()) {
      setError('First name and last name are required')
      return
    }

    updateProfile(
      { firstName: firstName.trim(), lastName: lastName.trim() },
      {
        onSuccess: () => {
          toast.success('Profile updated')
          onOpenChange(false)
        },
        onError: (mutationError) =>
          setError(getErrorMessage(mutationError, 'Could not update profile'))
      }
    )
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <div className="w-full max-w-md border border-neon-cyan/40 bg-card p-6 shadow-2xl shadow-neon-cyan/10">
        <div className="flex items-start justify-between">
          <div>
            <h2 className="mt-2 flex items-center gap-2 font-display text-2xl uppercase tracking-tighter">
              <User className="h-5 w-5 text-neon-cyan" /> Profile
            </h2>
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
          <div className="grid grid-cols-2 gap-3">
            <FormField
              label="First Name"
              value={firstName}
              onChange={setFirstName}
              accent="cyan"
              autoComplete="given-name"
              disabled={isPending}
            />
            <FormField
              label="Last Name"
              value={lastName}
              onChange={setLastName}
              accent="cyan"
              autoComplete="family-name"
              disabled={isPending}
            />
          </div>
          <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            Email cannot be changed
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
            className="flex-1 bg-neon-cyan py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            Save
          </button>
        </div>
      </div>
    </div>
  )
}
