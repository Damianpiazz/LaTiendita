import Link from "next/link";

import { buttonVariants } from "@/components/ui/button";
import { ImagePlaceholder } from "@/components/ui/image-placeholder";
import { cn } from "@/lib/utils";

/** Hero: editorial copy on the left, pending 560x340 image on the right. */
export function HeroSection() {
  return (
    <section className="bg-wf-surface px-12 py-16">
      <div className="mx-auto grid max-w-[1440px] items-center gap-12 lg:grid-cols-[minmax(0,1fr)_560px]">
        <div>
          <h1 className="max-w-2xl text-5xl font-bold leading-[1.1] tracking-tight text-wf-ink">
            Lorem ipsum dolor sit amet, consectetur elit.
          </h1>
          <p className="mt-6 max-w-xl text-lg leading-relaxed text-wf-ink-tertiary">
            Lorem ipsum dolor sit amet, consectetur adipiscing elit Integer nec odio
            praesent libero sed cursus.
          </p>
          <div className="mt-8 flex flex-wrap gap-4">
            <Link
              href="/productos"
              className={cn(
                buttonVariants({ variant: "default" }),
                "h-12 bg-wf-inverse px-6 text-base font-semibold text-wf-on-inverse hover:bg-wf-ink",
              )}
            >
              Comprar ahora
            </Link>
            <Link
              href="/categorias"
              className={cn(
                buttonVariants({ variant: "outline" }),
                "h-12 border-wf-line-strong bg-wf-surface px-6 text-base font-semibold text-wf-ink hover:bg-wf-canvas",
              )}
            >
              Ver catálogo
            </Link>
          </div>
        </div>

        <ImagePlaceholder
          dashed
          label="Imagen 560 x 340"
          className="h-[340px] w-full lg:w-[560px]"
        />
      </div>
    </section>
  );
}
