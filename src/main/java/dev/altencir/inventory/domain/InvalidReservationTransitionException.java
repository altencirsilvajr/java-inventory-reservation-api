package dev.altencir.inventory.domain;

public class InvalidReservationTransitionException extends RuntimeException {
    public InvalidReservationTransitionException(String message) { super(message); }
}
