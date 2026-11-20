import { useMatches } from 'react-router-dom'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'

interface RouteHandle {
  title?: string
}

export const useRouteTitle = () => {
  const titles = useMatches().map((match) => (match.handle as RouteHandle | undefined)?.title)

  useDocumentTitle(titles.findLast(Boolean))
}
