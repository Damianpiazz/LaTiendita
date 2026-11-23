"use client";

import { useState, useTransition } from "react";

import { RatingStars } from "@/features/product/components/rating-stars";
import { markReviewHelpful } from "@/features/review/actions/review.actions";
import { formatReviewDate } from "@/features/review/helpers/format-review-date";
import type { Review } from "@/features/review/types";

/** One approved review card, with a real "helpful" vote. */
export function ReviewCard({ review }: { review: Review }) {
  const [helpfulCount, setHelpfulCount] = useState(review.helpfulCount);
  const [voted, setVoted] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [pending, startTransition] = useTransition();

  function voteHelpful() {
    startTransition(async () => {
      const result = await markReviewHelpful(review.id);

      if (result.ok) {
        setHelpfulCount((count) => count + 1);
        setVoted(true);
      } else {
        setMessage(result.message ?? "No pudimos registrar tu voto.");
      }
    });
  }

  return (
    <article className="flex flex-col gap-2.5 rounded-wf-card border border-wf-line bg-wf-surface p-5">
      <div className="flex items-center gap-2.5">
        <span aria-hidden className="size-9 shrink-0 rounded-full bg-wf-muted" />
        <span className="flex flex-col gap-0.5">
          <span className="text-sm font-bold text-wf-ink">{review.author}</span>
          <span className="text-xs text-wf-ink-muted">
            {review.verified ? "Compra verificada · " : ""}
            {formatReviewDate(review.createdAt)}
          </span>
        </span>
      </div>

      <RatingStars filled={review.rating} size="sm" />

      <h3 className="text-sm font-bold text-wf-ink">{review.title}</h3>

      <p className="text-[13px] leading-5 text-wf-ink-secondary">{review.body}</p>

      <div className="flex flex-wrap items-center gap-4 text-xs">
        <span className="text-wf-ink-muted">¿Te sirvió?</span>
        <button
          type="button"
          onClick={voteHelpful}
          disabled={pending || voted}
          title={voted ? "Ya registraste tu voto" : undefined}
          className="font-medium text-wf-ink-secondary underline-offset-2 hover:underline disabled:cursor-not-allowed disabled:text-wf-ink-muted disabled:no-underline"
        >
          Sí ({helpfulCount})
        </button>
        <button
          type="button"
          disabled
          title="El voto negativo todavía no está disponible"
          className="font-medium text-wf-ink-placeholder"
        >
          No ({review.notHelpfulCount})
        </button>
      </div>

      {message ? (
        <p role="alert" className="text-xs text-red-600">
          {message}
        </p>
      ) : null}
    </article>
  );
}
