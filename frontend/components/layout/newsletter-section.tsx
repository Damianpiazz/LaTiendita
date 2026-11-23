"use client";

import { useState, type FormEvent } from "react";

import { Button } from "@/components/ui/button";
import { env } from "@/config/env";
import { siteConfig } from "@/config/site";

type SubmitState = "idle" | "pending" | "success" | "error";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * Newsletter box.
 *
 * Subscriptions are public: the form posts the typed address to the
 * customer-service newsletter endpoint through the gateway. Mock mode skips
 * the network call so isolated component tests stay deterministic.
 */
export function NewsletterSection() {
  const { newsletter } = siteConfig;
  const [email, setEmail] = useState("");
  const [state, setState] = useState<SubmitState>("idle");
  const [message, setMessage] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = email.trim();

    if (!EMAIL_PATTERN.test(value)) {
      setState("error");
      setMessage("Ingresá un email válido.");
      return;
    }

    setState("pending");
    setMessage(null);

    try {
      if (!env.useMocks) {
        const response = await fetch(
          `${env.apiBaseUrl}/api/customers/newsletter/subscriptions`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email: value }),
          },
        );

        if (!response.ok) {
          throw new Error(`Newsletter subscription failed: ${response.status}`);
        }
      }

      setState("success");
      setMessage("¡Listo! Te vamos a avisar de las novedades.");
      setEmail("");
    } catch {
      setState("error");
      setMessage("No pudimos completar la suscripción. Intentá de nuevo.");
    }
  }

  return (
    <section aria-labelledby="newsletter-title" className="px-12 py-16">
      <div className="mx-auto flex max-w-[1440px] flex-col gap-6 rounded-wf-md bg-wf-inverse p-10 lg:flex-row lg:items-center lg:justify-between">
        <div className="max-w-lg">
          <h2 id="newsletter-title" className="text-2xl font-bold text-wf-on-inverse">
            {newsletter.title}
          </h2>
          <p className="mt-2 text-sm text-wf-muted">{newsletter.copy}</p>
        </div>

        <form onSubmit={handleSubmit} className="flex w-full max-w-md flex-col gap-3 sm:flex-row">
          <label htmlFor="newsletter-email" className="sr-only">
            Email
          </label>
          <input
            id="newsletter-email"
            name="email"
            type="email"
            autoComplete="email"
            required
            value={email}
            onChange={(event) => {
              setEmail(event.target.value);
              if (state !== "idle") {
                setState("idle");
                setMessage(null);
              }
            }}
            placeholder={newsletter.placeholder}
            className="h-12 w-full rounded-wf-sm border border-wf-line-strong bg-wf-surface px-3 text-sm text-wf-ink outline-none placeholder:text-wf-ink-placeholder"
          />
          <Button
            type="submit"
            disabled={state === "pending"}
            className="h-12 shrink-0 bg-wf-surface px-6 text-sm font-semibold text-wf-ink hover:bg-wf-subtle disabled:opacity-60"
          >
            {newsletter.action}
          </Button>
          <p
            aria-live="polite"
            role={state === "error" ? "alert" : undefined}
            className="w-full text-sm text-wf-muted"
          >
            {message}
          </p>
        </form>
      </div>
    </section>
  );
}
