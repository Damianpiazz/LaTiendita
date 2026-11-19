"use client";

import { useState } from "react";

import { ImagePlaceholder } from "@/components/ui/image-placeholder";
import { cn } from "@/lib/utils";

type ProductGalleryProps = {
  /** Number of images the product has; the wireframe seeds four. */
  count: number;
};

/**
 * Main image plus thumbnails of the detail screen.
 *
 * The wireframe has no real assets, so selecting a thumbnail only changes the
 * selected index and the caption; the space reserved matches the final image.
 */
export function ProductGallery({ count }: ProductGalleryProps) {
  const [selected, setSelected] = useState(0);

  return (
    <div className="flex w-full min-w-0 flex-col gap-3.5">
      <ImagePlaceholder
        label="[ Imagen principal — 640 x 480 ]"
        className="aspect-[4/3] w-full rounded-wf-md border border-wf-line-strong text-[13px]"
      />

      <ul className="flex flex-wrap gap-3">
        {Array.from({ length: count }, (_, index) => (
          <li key={index}>
            <button
              type="button"
              aria-label={`Imagen ${index + 1} de ${count}`}
              aria-pressed={selected === index}
              onClick={() => setSelected(index)}
              className={cn(
                "flex size-[88px] items-center justify-center rounded-wf-sm bg-wf-subtle text-[11px] text-wf-ink-subtle",
                selected === index
                  ? "border-2 border-wf-inverse"
                  : "border border-wf-muted",
              )}
            >
              [ img ]
            </button>
          </li>
        ))}
      </ul>

      <span className="text-xs text-wf-ink-subtle">
        {selected + 1} de {count} — hacé clic para cambiar la imagen
      </span>
    </div>
  );
}
