"use client";

import { Button } from "@/components/ui/button";
import { useCartStore } from "@/features/cart/store/cart-store";
import { cn } from "@/lib/utils";

type AddToCartButtonProps = {
  productId: string;
  quantity?: number;
  className?: string;
};

export function AddToCartButton({
  productId,
  quantity = 1,
  className,
}: AddToCartButtonProps) {
  const addItem = useCartStore((state) => state.addItem);

  return (
    <Button
      type="button"
      variant="outline"
      onClick={() => addItem(productId, quantity)}
      className={cn(
        "h-10 w-full text-sm font-medium text-wf-ink-secondary",
        className,
      )}
    >
      Agregar al carrito
    </Button>
  );
}
