import { useState } from 'react'
import { ChangePasswordDialog } from '@/components/dialog/ChangePasswordDialog'
import { EditProfileDialog } from '@/components/dialog/EditProfileDialog'
import { ProfileStat } from '@/components/ui/ProfileStat'
import { useLogout } from '@/hooks/useLogout'
import { useLogoutAll } from '@/hooks/useLogoutAll'
import { useProfile } from '@/hooks/useProfile'

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium' })

const getInitials = (firstName: string, lastName: string) =>
  `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()

export const ProfileSection = () => {
  const { data: profile, isLoading, isError } = useProfile()
  const { mutate: logout, isPending: isLoggingOut } = useLogout()
  const { mutate: logoutAll, isPending: isLoggingOutAll } = useLogoutAll()
  const [isEditOpen, setIsEditOpen] = useState(false)
  const [isPasswordOpen, setIsPasswordOpen] = useState(false)

  if (isLoading) {
    return (
      <aside className="self-start border border-border bg-card p-8">
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      </aside>
    )
  }

  if (isError || !profile) {
    return (
      <aside className="self-start border border-border bg-card p-8">
        <p className="font-mono text-xs uppercase tracking-widest text-destructive">
          Could not load your profile
        </p>
      </aside>
    )
  }

  const { user, createdAt, totalOrders } = profile

  return (
    <aside className="self-start border border-border bg-card p-8">
      <div className="flex items-center gap-4">
        <div className="flex h-16 w-16 shrink-0 items-center justify-center border border-neon-cyan/40 font-display text-2xl text-neon-cyan">
          {getInitials(user.firstName, user.lastName)}
        </div>
        <div className="min-w-0">
          <p className="truncate font-display text-xl uppercase tracking-tighter">
            {user.firstName} {user.lastName}
          </p>
          <p className="truncate font-mono text-xs text-muted-foreground">{user.email}</p>
        </div>
      </div>
      <div className="mt-8 grid grid-cols-2 gap-6 border-y border-border py-6">
        <ProfileStat label="Member Since" value={dateFormatter.format(new Date(createdAt))} />
        <ProfileStat label="Total Orders" value={totalOrders} />
      </div>
      <div className="mt-6 space-y-3">
        <button
          onClick={() => setIsEditOpen(true)}
          className="w-full border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-neon-cyan hover:text-neon-cyan"
        >
          Edit Profile
        </button>
        <button
          onClick={() => setIsPasswordOpen(true)}
          className="w-full border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-neon-pink hover:text-neon-pink"
        >
          Change Password
        </button>
        <button
          onClick={() => logout()}
          disabled={isLoggingOut}
          className="w-full border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-destructive hover:text-destructive disabled:opacity-50"
        >
          Sign Out
        </button>
      </div>
      <div className="mt-8 border-t border-border pt-6">
        <button
          onClick={() => logoutAll()}
          disabled={isLoggingOutAll}
          className="w-full border border-destructive/40 py-3 text-xs font-bold uppercase tracking-widest text-destructive hover:bg-destructive/10 disabled:opacity-50"
        >
          Terminate All Sessions
        </button>
        <p className="mt-3 text-xs text-muted-foreground">
          Signs you out on every device, including this one.
        </p>
      </div>
      <EditProfileDialog
        open={isEditOpen}
        onOpenChange={setIsEditOpen}
        initialFirstName={user.firstName}
        initialLastName={user.lastName}
      />
      <ChangePasswordDialog open={isPasswordOpen} onOpenChange={setIsPasswordOpen} />
    </aside>
  )
}
