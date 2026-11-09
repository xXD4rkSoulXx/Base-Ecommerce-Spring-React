import { unmarkDemoSession } from '@/demo/demoSession'
import { stopDemo } from '@/demo/startDemo'
import { useEndSession } from '@/hooks/useEndSession'

export const useExitDemo = () => {
  const endSession = useEndSession()

  return () => {
    stopDemo()
    unmarkDemoSession()
    endSession()
  }
}
