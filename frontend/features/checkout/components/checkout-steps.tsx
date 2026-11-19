import { cn } from "@/lib/utils";

const steps = [
  { number: 1, label: "Carrito" },
  { number: 2, label: "Envío" },
  { number: 3, label: "Pago" },
  { number: 4, label: "Confirmación" },
];

type CheckoutStepsProps = {
  /** 1-based number of the step the screen represents. */
  currentStep: number;
};

/**
 * Checkout progress strip shared by every step of the flow.
 *
 * Wireframe: 24px circles with 8px gaps, 24px connectors and 14px labels.
 * Steps before `currentStep` are done and show a tick, the current one shows
 * its number, and the rest stay muted.
 */
export function CheckoutSteps({ currentStep }: CheckoutStepsProps) {
  return (
    <ol
      aria-label="Pasos de la compra"
      className="flex flex-wrap items-center gap-2"
    >
      {steps.map((step, index) => {
        const isCurrent = step.number === currentStep;
        const isDone = step.number < currentStep;

        return (
          <li key={step.label} className="flex items-center gap-2">
            {index > 0 ? (
              <span aria-hidden className="h-px w-6 bg-wf-line" />
            ) : null}
            <span className="flex items-center gap-2">
              <span
                aria-current={isCurrent ? "step" : undefined}
                className={cn(
                  "flex size-6 items-center justify-center rounded-full text-xs",
                  isCurrent && "bg-wf-inverse font-bold text-wf-on-inverse",
                  isDone && "bg-wf-inverse text-wf-on-inverse",
                  !isCurrent && !isDone && "bg-wf-subtle text-wf-ink-muted",
                )}
              >
                {isDone ? "✓" : step.number}
              </span>
              <span
                className={cn(
                  "text-sm",
                  isCurrent
                    ? "font-medium text-wf-ink"
                    : isDone
                      ? "text-wf-ink-secondary"
                      : "text-wf-ink-muted",
                )}
              >
                {step.label}
              </span>
            </span>
          </li>
        );
      })}
    </ol>
  );
}
