import { LegalPageLayout } from '@/components/layout/LegalPageLayout'
import { FaqItem } from '@/components/ui/FaqItem'
import { APP_NAME, LEGAL_LAST_UPDATED } from '@/constants'

const FAQS = [
  {
    question: 'Is this a real store?',
    answer: `No. ${APP_NAME} is a portfolio project. Nothing you order here will be shipped or charged.`
  },
  {
    question: 'Can I pay with a real card?',
    answer:
      'No, please do not. Payments run in Stripe test mode. Use the test card 4242 4242 4242 4242 with any future expiry date and any CVC.'
  },
  {
    question: 'Do I need an account to buy?',
    answer: 'Yes. You need to sign in to check out.'
  },
  {
    question: 'How do shipping and tracking work?',
    answer:
      'Shipping rates are quoted for your address before you pay. After payment the order moves through Processing, Shipped and Delivered automatically, usually in under an hour, and each parcel has simulated tracking events.'
  },
  {
    question: 'Why is my order split into several parcels?',
    answer:
      'Larger orders are packed into as few boxes as possible within the weight and size limits of each box. Every box has its own tracking number and history, and the order is Delivered when all boxes are delivered.'
  },
  {
    question: 'Why do some orders show no parcels?',
    answer:
      'Some carriers cannot issue test labels. In that case the order still moves forward by itself, but no parcels are shown.'
  },
  {
    question: 'Can I cancel an order?',
    answer: 'Not after payment in this demo. Once it is delivered you can return it within 14 days.'
  },
  {
    question: 'How do I return an item?',
    answer: 'Open the order in your account and click Return items. The Return policy page has the details.'
  },
  {
    question: 'When will I get my refund?',
    answer:
      'Returns are approved automatically about ten minutes after you request them, and the refund goes to your original payment method.'
  },
  {
    question: 'Do I get the shipping cost back?',
    answer: 'Yes, in proportion to the weight of the items you return, and in full if you return the whole order.'
  },
  {
    question: 'Can I reuse a discount code?',
    answer:
      'Codes can be used once per account. If you return the entire order and it becomes Refunded, the code is released.'
  },
  {
    question: 'What happens to my data?',
    answer:
      'We only keep what the demo needs to work. The Privacy policy page explains what is stored and who receives it.'
  }
]

export const Faq = () => (
  <LegalPageLayout title="FAQ" updated={LEGAL_LAST_UPDATED}>
    <div className="border border-border">
      {FAQS.map((faq) => (
        <FaqItem key={faq.question} question={faq.question} answer={faq.answer} />
      ))}
    </div>
  </LegalPageLayout>
)
