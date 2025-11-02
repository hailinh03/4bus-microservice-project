package com.mss.project.booking_service.payload.booking;

import com.mss.project.booking_service.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;
import vn.payos.type.CheckoutResponseData;

import java.time.Instant;

@Data
@Builder
public class BookingResponse {
    // Booking entity fields
    private Integer id;
    private BookingStatus status;
    private Double totalPrice;
    private Integer numberOfTickets;
    private Long userId;
    private Integer tripId;
    private String seatCodes;
    private Long orderCode;
    private Instant createdAt;
    private Instant updatedAt;

    // Payment response fields
    private CheckoutResponseData paymentResponse;

    // Legacy fields for backward compatibility
    private Integer bookingId; // Will map to id
}
