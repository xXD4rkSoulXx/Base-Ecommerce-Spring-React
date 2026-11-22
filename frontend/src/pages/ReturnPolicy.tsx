import { LegalPageLayout } from '@/components/layout/LegalPageLayout'
import { LegalSection, type LegalSectionProps } from '@/components/section/LegalSection'
import { LEGAL_LAST_UPDATED, RETURN_REASON_LABELS } from '@/constants'

const SECTIONS: LegalSectionProps[] = [
  {
    title: 'Cancel an order',
    paragraphs: [
      'You can cancel an order after it has been paid, for as long as its status is Paid. Once we start preparing it (Processing) it can no longer be canceled, but you can return it after delivery.'
    ],
    items: [
      'Open your account, select the order and click Cancel order.',
      'The full amount you paid, including shipping and tax, is refunded to the original payment method.',
      'The items go back into stock.',
      'If you used a discount code, it is released and you can use it again.'
    ]
  },
  {
    title: 'Return window',
    paragraphs: [
      'You can return items within 14 days of delivery. The 14 days start on the date your order was first marked as Delivered.',
      'Orders that have not been delivered yet cannot be returned, but you can cancel them while they are still Paid.'
    ]
  },
  {
    title: 'How to return',
    paragraphs: ['Returns are requested from your order page:'],
    items: [
      'Open your account and select the order from your order history.',
      'Click Return items. The button is only shown while the order is eligible.',
      'Choose the items and quantities you want to return, then pick a reason.',
      'Submit the request. The order shows as Return Requested until the refund is issued.'
    ]
  },
  {
    title: 'Partial returns',
    paragraphs: [
      'You can return only some of the items, or only some units of an item. You can request more returns later on the same order, one at a time, as long as there are units left to return and the 14 days have not passed.'
    ]
  },
  {
    title: 'Reasons',
    paragraphs: ['Select the reason that fits best. If you choose Other, a short description is required.'],
    items: Object.values(RETURN_REASON_LABELS)
  },
  {
    title: 'Return shipping',
    paragraphs: ['Return shipping is free. In this demo store no return parcel is generated or sent.']
  },
  {
    title: 'Refunds',
    paragraphs: ['Refunds go back to the payment method used for the order.'],
    items: [
      'Items: you are refunded the price you paid for each returned item, including any discount that applied.',
      'Tax: the tax on the returned items is refunded proportionally.',
      'Shipping: the shipping cost is refunded in proportion to the weight of the returned items compared with the whole order. If you return everything, shipping is refunded in full.',
      'The total refunded across all your returns never exceeds what you paid.'
    ]
  },
  {
    title: 'Refund timing',
    paragraphs: [
      'In this demo store, returns are approved automatically a few minutes after they are requested. With a real payment, a refund can take several business days to show on your statement, depending on your bank.'
    ]
  },
  {
    title: 'Discount codes',
    paragraphs: [
      'A discount code can be used once per account. If you return only part of an order, the code stays used. If you cancel an order, or return every item and the order becomes Refunded, the code is released and you can use it again.'
    ]
  },
  {
    title: 'If a refund fails',
    paragraphs: [
      'If the payment provider cannot process a return refund, the return is marked as Failed and its items become available to return again. You can submit a new request. If a cancellation refund fails, the order stays Paid and you can try again.'
    ]
  }
]

export const ReturnPolicy = () => (
  <LegalPageLayout title="Return policy" updated={LEGAL_LAST_UPDATED}>
    {SECTIONS.map((section) => (
      <LegalSection key={section.title} {...section} />
    ))}
  </LegalPageLayout>
)
