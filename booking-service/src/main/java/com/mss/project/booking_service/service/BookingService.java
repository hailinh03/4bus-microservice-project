package com.mss.project.booking_service.service;

import com.mss.project.booking_service.payload.booking.BookingRequest;
import com.mss.project.booking_service.payload.booking.BookingResponse;
import com.mss.project.booking_service.payload.booking.BookingHistoryListRequest;
import com.mss.project.booking_service.payload.booking.BookingHistoryResponse;
import org.springframework.data.domain.Page;

public interface BookingService {
    BookingResponse initiateBooking(BookingRequest bookingRequest);

    Page<BookingHistoryResponse> getMyBookingHistory(BookingHistoryListRequest request);
}
