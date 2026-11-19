import { Star } from "lucide-react";

import { cn } from "@/lib/utils";

type RatingStarsProps = {
  /** Number of filled stars; the rest render in `background/muted`. */
  filled: number;
  total?: number;
  size?: "sm" | "md";
  className?: string;
};

/**
 * Star row of the wireframe.
 *
 * The design uses a discrete count per context (four on the product info, five
 * on the summary, the review rating on each card), so the caller owns `filled`
 * instead of deriving it from an average that would round differently.
 */
export function RatingStars({
  filled,
  total = 5,
  size = "md",
  className,
}: RatingStarsProps) {
  return (
    <span
      className={cn("inline-flex items-center gap-0.5", className)}
      role="img"
      aria-label={`${filled} de ${total} estrellas`}
    >
      {Array.from({ length: total }, (_, index) =>
        index < filled ? (
          <Star
            key={index}
            aria-hidden
            className={cn(
              "fill-wf-ink text-wf-ink",
              size === "sm" ? "size-3.5" : "size-4",
            )}
          />
        ) : (
          <Star
            key={index}
            aria-hidden
            className={cn(
              "fill-wf-muted text-wf-muted",
              size === "sm" ? "size-3.5" : "size-4",
            )}
          />
        ),
      )}
    </span>
  );
}
