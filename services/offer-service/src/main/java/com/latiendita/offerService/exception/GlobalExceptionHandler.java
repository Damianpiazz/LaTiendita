package com.latiendita.offerService.exception;

import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail onUnreadableMessage(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed request body");
    }

    @ExceptionHandler(PromotionNotFoundException.class)
    public ProblemDetail onPromotionNotFound(PromotionNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Promotion not found", ex.getMessage());
    }

    @ExceptionHandler(CouponNotFoundException.class)
    public ProblemDetail onCouponNotFound(CouponNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Coupon not found", ex.getMessage());
    }

    @ExceptionHandler(DuplicatePromotionCodeException.class)
    public ProblemDetail onDuplicateCode(DuplicatePromotionCodeException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler({InvalidPromotionException.class, InvalidCouponException.class})
    public ProblemDetail onUnprocessable(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    public ProblemDetail onConflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict",
                "Concurrent update or unique constraint violation; retry the request");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail onUnexpected(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", "Unexpected error");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(title);
        pd.setDetail(detail);
        return pd;
    }
}
