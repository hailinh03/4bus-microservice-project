package com.mss.project.booking_service.exception;

public class BookingHistoryException extends RuntimeException {
    public BookingHistoryException(String message) {
        super(message);
    }

    public BookingHistoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
