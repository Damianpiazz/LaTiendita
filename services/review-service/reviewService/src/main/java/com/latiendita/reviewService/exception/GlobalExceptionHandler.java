package com.latiendita.reviewService.exception;

import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Bad Request", "Validation failed");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList()));
        return pd;
    }

    @ExceptionHandler({InvalidRatingException.class, ModerationReasonRequiredException.class})
    public ProblemDetail onBadRequest(RuntimeException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler({NotPurchasedException.class, AdminRoleRequiredException.class,
            MissingGatewayHeaderException.class})
    public ProblemDetail onForbidden(RuntimeException ex) {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    @ExceptionHandler({ProductNotFoundException.class, ReviewNotFoundException.class})
    public ProblemDetail onNotFound(RuntimeException ex) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler({DuplicateReviewException.class, DuplicateVoteException.class,
            SelfVoteException.class, ReviewNotVotableException.class,
            OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ProblemDetail onConflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler({OrderNotEligibleException.class, InvalidReviewStatusException.class})
    public ProblemDetail onUnprocessable(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage());
    }

    @ExceptionHandler({OrderServiceUnavailableException.class, ProductServiceUnavailableException.class})
    public ProblemDetail onUnavailable(RuntimeException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", ex.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        return pd;
    }
}
