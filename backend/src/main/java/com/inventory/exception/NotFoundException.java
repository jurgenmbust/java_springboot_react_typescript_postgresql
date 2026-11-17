package com.inventory.exception;
/** Signals a missing record. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
