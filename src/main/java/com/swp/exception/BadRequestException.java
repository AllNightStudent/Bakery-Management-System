package com.swp.exception;

/**
 * @author
 * @since 5/24/2025 - 10:59 PM
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(final String message) {
        super(message);
    }
}
