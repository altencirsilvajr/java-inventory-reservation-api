package dev.altencir.inventory.web;

import dev.altencir.inventory.application.IdempotencyConflictException;
import dev.altencir.inventory.application.ResourceNotFoundException;
import dev.altencir.inventory.domain.InsufficientStockException;
import dev.altencir.inventory.domain.InvalidReservationTransitionException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail notFound(ResourceNotFoundException failure) { return problem(HttpStatus.NOT_FOUND, "resource-not-found", failure.getMessage()); }

    @ExceptionHandler({InsufficientStockException.class, InvalidReservationTransitionException.class, IdempotencyConflictException.class})
    ProblemDetail conflict(RuntimeException failure) { return problem(HttpStatus.CONFLICT, "business-conflict", failure.getMessage()); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail duplicate(DataIntegrityViolationException failure) { return problem(HttpStatus.CONFLICT, "duplicate-resource", "A resource with the same unique value already exists"); }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, MissingRequestHeaderException.class, IllegalArgumentException.class})
    ProblemDetail invalid(Exception failure) { return problem(HttpStatus.BAD_REQUEST, "invalid-request", failure.getMessage()); }

    private static ProblemDetail problem(HttpStatus status, String type, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("https://inventory.altencir.dev/problems/" + type));
        return problem;
    }
}
