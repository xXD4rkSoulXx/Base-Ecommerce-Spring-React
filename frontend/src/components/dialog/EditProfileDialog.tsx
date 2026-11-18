import { useState, type SubmitEvent } from 'react'
import { toast } from 'sonner'
import { User } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { FormField } from '@/components/ui/FormField'
import { Modal } from '@/components/ui/Modal'
import { useUpdateProfile } from '@/hooks/useUpdateProfile'
import { getErrorMessage } from '@/utils/error'

interface EditProfileDialogProps {
  initialFirstName: string
  initialLastName: string
  onClose: () => void
}

export const EditProfileDialog = ({ initialFirstName, initialLastName, onClose }: EditProfileDialogProps) => {
  const [firstName, setFirstName] = useState(initialFirstName)
  const [lastName, setLastName] = useState(initialLastName)
  const [error, setError] = useState<string | null>(null)
  const { mutate: updateProfile, isPending } = useUpdateProfile()

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    const trimmedFirstName = firstName.trim()
    const trimmedLastName = lastName.trim()

    if (!trimmedFirstName || !trimmedLastName) {
      setError('First name and last name are required')
      return
    }

    if (trimmedFirstName.length > 100 || trimmedLastName.length > 100) {
      setError('First name and last name must be at most 100 characters')
      return
    }

    setError(null)

    updateProfile(
      { firstName: trimmedFirstName, lastName: trimmedLastName },
      {
        onSuccess: () => {
          toast.success('Profile updated')
          onClose()
        },
        onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not update profile'))
      }
    )
  }

  return (
    <Modal eyebrow="Account" title="Profile" icon={<User className="h-5 w-5 text-neon-cyan" />} onClose={onClose}>
      <form onSubmit={submit}>
        {error && (
          <div className="mb-6">
            <FormError message={error} />
          </div>
        )}
        <div className="space-y-4">
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
            Save
          </button>
        </div>
      </form>
    </Modal>
  )
}
