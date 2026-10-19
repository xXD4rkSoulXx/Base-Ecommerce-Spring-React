import { useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { Check } from 'lucide-react'

const NEXT_STEPS = [
  {
    label: 'Payment confirmation',
    text: 'Stripe confirms your payment to our system. This usually takes a few seconds.'
  },
  {
    label: 'Order preparation',
    text: 'We prepare your objects for the shipping method you selected.'
  },
  {
    label: 'Shipping',
    text: 'Your order is handed to the carrier and starts its journey to you.'
  }
]

export const CheckoutSuccess = () => {
  const queryClient = useQueryClient()

  useEffect(() => {
    let attempts = 0
    const refreshCart = () => queryClient.invalidateQueries({ queryKey: ['cart'] })

    refreshCart()

    const interval = setInterval(() => {
      attempts += 1
      refreshCart()

      if (attempts === 5) {
        clearInterval(interval)
      }
    }, 2000)

    return () => clearInterval(interval)
  }, [queryClient])

  return (
    <div className="mx-auto flex max-w-2xl flex-col items-center px-6 py-24 text-center md:py-32">
      <div className="flex h-20 w-20 items-center justify-center border-2 border-neon-cyan shadow-lg shadow-neon-cyan/30">
        <Check className="h-10 w-10 text-neon-cyan" />
      </div>
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-6xl">
        Order <span className="italic text-neon-cyan">Received</span>
      </h1>
      <p className="mt-6 max-w-md text-muted-foreground">
        Thank you for your purchase. We are confirming your payment and your cart will be cleared
        as soon as it is done.
      </p>
      <div className="mt-12 w-full border border-border bg-card p-6 text-left">
        <h2 className="mb-6 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
          What happens next
        </h2>
        <div className="space-y-5">
          {NEXT_STEPS.map((step, i) => (
            <div key={step.label} className="flex gap-4">
              <span className="font-mono text-xs text-neon-cyan">0{i + 1}</span>
              <div>
                <p className="text-sm font-bold uppercase">{step.label}</p>
                <p className="mt-1 text-sm text-muted-foreground">{step.text}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div className="mt-10 flex flex-col gap-3 sm:flex-row">
        <Link
          to="/account"
          className="border border-border px-8 py-4 text-xs font-bold uppercase tracking-widest hover:border-foreground"
        >
          View Orders
        </Link>
        <Link
          to="/shop"
          className="bg-neon-cyan px-8 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80"
        >
          Continue Shopping
        </Link>
      </div>
    </div>
  )
}
