import { useMutation } from '@tanstack/react-query'
import { adminOrderService } from '@/services/admin/adminOrder.service'
import { downloadFile } from '@/utils/download'

export const useAdminDownloadShippingSlip = () =>
  useMutation({
    mutationFn: async (orderId: number) => {
      const slip = await adminOrderService.getShippingSlip(orderId)

      downloadFile(slip, `order-${orderId}-shipping-slip.pdf`)
    }
  })
