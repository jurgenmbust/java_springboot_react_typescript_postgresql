package com.inventory.exception;
/** Signals a duplicate value or a record still in use. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
