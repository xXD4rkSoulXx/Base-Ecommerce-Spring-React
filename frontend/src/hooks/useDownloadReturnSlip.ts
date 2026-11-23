import { useMutation } from '@tanstack/react-query'
import { orderService } from '@/services/order.service'
import { downloadFile } from '@/utils/download'

export const useDownloadReturnSlip = () =>
  useMutation({
    mutationFn: async ({ orderId, returnId }: { orderId: number; returnId: number }) => {
      const slip = await orderService.getReturnSlip(orderId, returnId)

      downloadFile(slip, `return-${returnId}-slip.pdf`)
    }
  })
