import { cn } from "@/lib/utils";

type ImagePlaceholderProps = {
  /** Caption rendered inside the box, e.g. `[ Imagen ]` or `Imagen 560 x 340`. */
  label?: string;
  /** Wireframe convention for pending assets: dashed outline. */
  dashed?: boolean;
  className?: string;
};

/**
 * Box that stands in for an image while there is no real asset.
 *
 * It keeps the wireframe honest: the layout reserves the exact space the real
 * image will take, and the caption says what is missing.
 */
export function ImagePlaceholder({
  label = "[ Imagen ]",
  dashed = false,
  className,
}: ImagePlaceholderProps) {
  return (
    <div
      className={cn(
        "flex items-center justify-center rounded-wf-md text-sm text-wf-ink-placeholder",
        dashed
          ? "border-2 border-dashed border-wf-line-strong bg-wf-surface"
          : "bg-wf-subtle",
        className,
      )}
    >
      {label}
    </div>
  );
}
