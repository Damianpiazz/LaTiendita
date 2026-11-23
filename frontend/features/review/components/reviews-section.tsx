"use client";

import Link from "next/link";
import { useActionState, useState } from "react";

import type { ProductDetail } from "@/features/product/types";
import {
  createReview,
  type ReviewActionState,
} from "@/features/review/actions/review.actions";
import { ReviewCard } from "@/features/review/components/review-card";
import { ReviewSummaryCard } from "@/features/review/components/review-summary-card";
import type { Review, ReviewSummary } from "@/features/review/types";

type ReviewsSectionProps = {
  product: ProductDetail;
  summary: ReviewSummary;
  reviews: Review[];
};

const INITIAL_STATE: ReviewActionState = { ok: false };

/** Reviews section measured from the `Reviews` frame (0:352). */
export function ReviewsSection({ product, summary, reviews }: ReviewsSectionProps) {
  const [formOpen, setFormOpen] = useState(false);
  const [state, formAction, isPending] = useActionState(createReview, INITIAL_STATE);

  return (
    <section id="resenas" aria-labelledby="resenas-titulo" className="bg-wf-surface p-12">
      <div className="mx-auto flex w-full max-w-[1440px] flex-col gap-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <h2 id="resenas-titulo" className="text-2xl font-bold text-wf-ink">
            Reseñas ({summary.count})
          </h2>
          <button
            type="button"
            onClick={() => setFormOpen((open) => !open)}
            aria-expanded={formOpen}
            aria-controls="review-form"
            className="inline-flex h-10 items-center rounded-wf-sm border border-wf-line-strong bg-wf-surface px-[18px] text-sm font-medium text-wf-ink-secondary transition-colors hover:bg-wf-canvas"
          >
            {formOpen ? "Cancelar" : "Escribir una reseña"}
          </button>
        </div>

        {formOpen ? (
          <form
            id="review-form"
            action={formAction}
            noValidate
            className="flex flex-col gap-4 rounded-wf-md border border-wf-line bg-wf-canvas p-6"
          >
            <input type="hidden" name="productId" value={product.id} />
            <input type="hidden" name="slug" value={product.slug} />

            <fieldset className="flex flex-wrap items-center gap-2 border-0 p-0">
              <legend className="mb-2 text-sm font-medium text-wf-ink-secondary">Puntuación</legend>
              {[1, 2, 3, 4, 5].map((value) => (
                <label key={value} className="flex cursor-pointer items-center gap-1 text-sm text-wf-ink-secondary">
                  <input type="radio" name="rating" value={value} required className="accent-black" />
                  {value} ★
                </label>
              ))}
              {state.fieldErrors?.rating ? (
                <span className="w-full text-xs text-red-600">{state.fieldErrors.rating}</span>
              ) : null}
            </fieldset>

            <div className="flex flex-col gap-2">
              <label htmlFor="review-title" className="text-sm font-medium text-wf-ink-secondary">
                Título
              </label>
              <input
                id="review-title"
                name="title"
                maxLength={120}
                required
                aria-invalid={state.fieldErrors?.title ? true : undefined}
                className="h-10 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm outline-none"
              />
              {state.fieldErrors?.title ? (
                <span className="text-xs text-red-600">{state.fieldErrors.title}</span>
              ) : null}
            </div>

            <div className="flex flex-col gap-2">
              <label htmlFor="review-body" className="text-sm font-medium text-wf-ink-secondary">
                Tu experiencia
              </label>
              <textarea
                id="review-body"
                name="body"
                rows={4}
                maxLength={4000}
                required
                aria-invalid={state.fieldErrors?.body ? true : undefined}
                className="rounded-wf-sm border border-wf-line-strong bg-wf-surface p-3 text-sm outline-none"
              />
              {state.fieldErrors?.body ? (
                <span className="text-xs text-red-600">{state.fieldErrors.body}</span>
              ) : null}
            </div>

            <button
              type="submit"
              disabled={isPending}
              className="h-10 self-start rounded-wf-sm bg-wf-inverse px-5 text-sm font-medium text-wf-on-inverse disabled:opacity-60"
            >
              {isPending ? "Enviando..." : "Enviar reseña"}
            </button>

            {state.message ? (
              <p role={state.ok ? "status" : "alert"} className={state.ok ? "text-sm text-green-700" : "text-sm text-red-600"}>
                {state.message}
              </p>
            ) : null}
          </form>
        ) : null}

        <div className="flex flex-col gap-6 lg:flex-row">
          <ReviewSummaryCard summary={summary} />

          {reviews.length > 0 ? (
            <div className="grid flex-1 gap-6 md:grid-cols-2">
              {reviews.map((review) => (
                <ReviewCard key={review.id} review={review} />
              ))}
            </div>
          ) : (
            <p className="flex-1 text-sm text-wf-ink-muted">
              Este producto todavía no tiene reseñas.
            </p>
          )}
        </div>

        {summary.count > 0 ? (
          <Link
            href={`/productos/${product.slug}/resenas`}
            className="self-start text-sm font-medium text-wf-ink-secondary transition-colors hover:text-wf-ink"
          >
            Ver las {summary.count} reseñas →
          </Link>
        ) : null}
      </div>
    </section>
  );
}
