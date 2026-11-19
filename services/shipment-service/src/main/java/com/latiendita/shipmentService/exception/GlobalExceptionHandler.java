package com.latiendita.shipmentService.exception;

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

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ProblemDetail onShipmentNotFound(ShipmentNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Envío no encontrado", ex.getMessage());
    }

    @ExceptionHandler(AddressNotFoundException.class)
    public ProblemDetail onAddressNotFound(AddressNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Dirección no encontrada", ex.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ProblemDetail onOrderNotFound(OrderNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Pedido no encontrado", ex.getMessage());
    }

    @ExceptionHandler({AddressAccessDeniedException.class, ShipmentAccessDeniedException.class})
    public ProblemDetail onAccessDenied(RuntimeException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage());
    }

    @ExceptionHandler(MissingGatewayHeaderException.class)
    public ProblemDetail onMissingGatewayHeader(MissingGatewayHeaderException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autorizado", ex.getMessage());
    }

    @ExceptionHandler(DuplicateShipmentException.class)
    public ProblemDetail onDuplicateShipment(DuplicateShipmentException ex) {
        return problem(HttpStatus.CONFLICT, "Envío duplicado", ex.getMessage());
    }

    @ExceptionHandler(InvalidShipmentStateException.class)
    public ProblemDetail onInvalidState(InvalidShipmentStateException ex) {
        return problem(HttpStatus.CONFLICT, "Transición de estado inválida", ex.getMessage());
    }

    @ExceptionHandler(InvalidShipmentRequestException.class)
    public ProblemDetail onInvalidRequest(InvalidShipmentRequestException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Datos inválidos", ex.getMessage());
    }

    @ExceptionHandler(OrderServiceUnavailableException.class)
    public ProblemDetail onOrderServiceDown(OrderServiceUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Servicio no disponible", ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail onOptimisticLock(OptimisticLockingFailureException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto de concurrencia",
                "El envío fue modificado por otra operación; reintente la petición");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail onDataIntegrity(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto de datos",
                "Violación de restricción única (posible carrera entre peticiones simultáneas)");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail onValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        String detail = errors.isEmpty() ? "Falló la validación del request" : errors.get(0);
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Datos inválidos", detail);
        problem.setProperty("errors", errors);
        return problem;
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        return problem;
    }
}
