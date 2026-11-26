import { render } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useInfiniteScroll } from '@/hooks/useInfiniteScroll'

type Callback = (entries: Array<{ isIntersecting: boolean }>) => void

let callback: Callback
const observe = vi.fn()
const disconnect = vi.fn()
const options = vi.fn()

class FakeIntersectionObserver {
  constructor(cb: Callback, opts: IntersectionObserverInit) {
    callback = cb
    options(opts)
  }

  observe = observe
  disconnect = disconnect
}

interface ProbeProps {
  hasNextPage: boolean
  isFetchingNextPage: boolean
  fetchNextPage: () => void
}

const Probe = (props: ProbeProps) => {
  const ref = useInfiniteScroll(props)

  return <div ref={ref} data-testid="target" />
}

const intersect = (isIntersecting: boolean) => callback([{ isIntersecting }])

describe('useInfiniteScroll', () => {
  beforeEach(() => {
    vi.stubGlobal('IntersectionObserver', FakeIntersectionObserver)
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('observes the target with a 0.1 threshold', () => {
    const { getByTestId } = render(<Probe hasNextPage isFetchingNextPage={false} fetchNextPage={vi.fn()} />)

    expect(observe).toHaveBeenCalledWith(getByTestId('target'))
    expect(options).toHaveBeenCalledWith({ threshold: 0.1 })
  })

  it('fetches the next page when the target becomes visible', () => {
    const fetchNextPage = vi.fn()
    render(<Probe hasNextPage isFetchingNextPage={false} fetchNextPage={fetchNextPage} />)

    intersect(true)

    expect(fetchNextPage).toHaveBeenCalledTimes(1)
  })

  it('does nothing while the target is not visible', () => {
    const fetchNextPage = vi.fn()
    render(<Probe hasNextPage isFetchingNextPage={false} fetchNextPage={fetchNextPage} />)

    intersect(false)

    expect(fetchNextPage).not.toHaveBeenCalled()
  })

  it('does nothing when there is no next page', () => {
    const fetchNextPage = vi.fn()
    render(<Probe hasNextPage={false} isFetchingNextPage={false} fetchNextPage={fetchNextPage} />)

    intersect(true)

    expect(fetchNextPage).not.toHaveBeenCalled()
  })

  it('does nothing while a page is already being fetched', () => {
    const fetchNextPage = vi.fn()
    render(<Probe hasNextPage isFetchingNextPage fetchNextPage={fetchNextPage} />)

    intersect(true)

    expect(fetchNextPage).not.toHaveBeenCalled()
  })

  it('disconnects the observer on unmount', () => {
    const { unmount } = render(<Probe hasNextPage isFetchingNextPage={false} fetchNextPage={vi.fn()} />)

    unmount()

    expect(disconnect).toHaveBeenCalled()
  })
})
