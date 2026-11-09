let starting: Promise<void> | undefined
let stop: (() => void) | undefined

export const startDemo = () => {
  starting ??= import('@/demo/worker')
    .then(async ({ worker }) => {
      await worker.start({ quiet: true })

      stop = () => worker.stop()
    })
    .catch((error) => {
      starting = undefined

      throw error
    })

  return starting
}

export const stopDemo = () => {
  stop?.()
  stop = undefined
  starting = undefined
}
