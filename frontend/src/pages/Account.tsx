import { useLogout } from '@/hooks/mutations/useLogout'

function Account() {
  const { mutate: handleLogout, isPending } = useLogout();
  
  return (
    <button
      onClick={() => handleLogout()}
      disabled={isPending}
      className="mt-6 w-full border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-destructive hover:text-destructive"
    >
      Sign Out
    </button>
  )
}

export default Account
