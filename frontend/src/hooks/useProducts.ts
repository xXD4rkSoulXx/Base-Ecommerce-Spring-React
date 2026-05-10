import { useQuery } from "@tanstack/react-query";
// import { getProducts, type Product } from "@/services/productService";
import { getProductsMock } from "@/mocks/Services/productMockService";
import { type Product } from "@/types/Product";

export const useProducts = () => {
  return useQuery<Product[]>({
    queryKey: ["products"],
    // queryFn: getProducts,
    queryFn: getProductsMock,
  });
};
