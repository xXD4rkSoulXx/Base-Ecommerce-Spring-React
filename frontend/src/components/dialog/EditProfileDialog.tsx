import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { toast } from 'sonner'
import { User } from 'lucide-react'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useUpdateProfile } from '@/hooks/useUpdateProfile'
import { profileSchema, type ProfileForm } from '@/schemas/profile'
import { getErrorMessage } from '@/utils/error'

interface EditProfileDialogProps {
  initialFirstName: string
  initialLastName: string
  onClose: () => void
}

export const EditProfileDialog = ({ initialFirstName, initialLastName, onClose }: EditProfileDialogProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<ProfileForm>({
    resolver: zodResolver(profileSchema),
    defaultValues: { firstName: initialFirstName, lastName: initialLastName }
  })

  const { mutate: updateProfile, isPending, error } = useUpdateProfile()

  const onSubmit = (data: ProfileForm) =>
    updateProfile(data, {
      onSuccess: () => {
        toast.success('Profile updated')
        onClose()
      }
    })

  return (
    <Modal eyebrow="Account" title="Profile" icon={<User className="h-5 w-5 text-neon-cyan" />} onClose={onClose}>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        {error && (
          <div className="mb-6">
            <FormError message={getErrorMessage(error, 'Could not update profile')} />
          </div>
        )}
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Field
              label="First Name"
              accent="cyan"
              autoComplete="given-name"
              disabled={isPending}
              error={errors.firstName?.message}
              {...register('firstName')}
            />
            <Field
              label="Last Name"
              accent="cyan"
              autoComplete="family-name"
              disabled={isPending}
              error={errors.lastName?.message}
              {...register('lastName')}
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
