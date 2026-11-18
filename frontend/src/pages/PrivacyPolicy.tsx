import { LegalPageLayout } from '@/components/layout/LegalPageLayout'
import { LegalSection, type LegalSectionProps } from '@/components/section/LegalSection'
import { APP_NAME, LEGAL_LAST_UPDATED } from '@/constants'

const SECTIONS: LegalSectionProps[] = [
  {
    title: 'Scope',
    paragraphs: [
      `${APP_NAME} is a demo store built for a portfolio. This page explains which data the demo handles. It is a sample text, not legal advice.`
    ]
  },
  {
    title: 'Data we collect',
    items: [
      'Account: your email and the other details you give when you register.',
      'Orders: delivery name, address and phone number, the items you buy and the amounts paid.',
      'Cart and wishlist: the products you save.',
      'Technical data: the cookies described below.'
    ]
  },
  {
    title: 'How we use it',
    paragraphs: [
      'To create your account, process and deliver (simulated) orders, request shipping rates, issue refunds and keep you signed in. We do not sell your data, and the demo sends no marketing emails.'
    ]
  },
  {
    title: 'Service providers',
    paragraphs: ['We share data only with the providers needed to run the demo.'],
    items: [
      "Stripe processes payments. Card details are entered on Stripe's page and never reach our servers. We keep the payment reference and the amounts.",
      'Shippo receives your delivery address and the size and weight of your parcels to quote shipping rates and create test labels.'
    ]
  },
  {
    title: 'Cookies',
    paragraphs: [
      'We use essential cookies only: a refresh-token cookie that keeps you signed in for up to 30 days, and a cookie that protects against cross-site request forgery. Stripe may set its own cookies on its checkout page.'
    ]
  },
  {
    title: 'Retention and security',
    paragraphs: [
      'Account and order data are kept while the demo is running and may be deleted at any time. Passwords are never stored in plain text.'
    ]
  },
  {
    title: 'Your data',
    paragraphs: [
      'You can manage part of your data from your account page. The demo has no account deletion, data export or support mailbox, so other requests cannot be handled. Demo data may be deleted at any time.'
    ],
    items: [
      'View your name, email, the date you joined and your order history.',
      'Edit your first and last name. Your email cannot be changed.',
      'Change your password, which signs you out of every device.',
      'End all your sessions on every device at any time.'
    ]
  }
]

export const PrivacyPolicy = () => (
  <LegalPageLayout title="Privacy policy" updated={LEGAL_LAST_UPDATED}>
    {SECTIONS.map((section) => (
      <LegalSection key={section.title} {...section} />
    ))}
  </LegalPageLayout>
)
