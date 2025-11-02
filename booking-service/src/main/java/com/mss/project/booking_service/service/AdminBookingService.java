package com.mss.project.booking_service.service;

import com.mss.project.booking_service.payload.admin.booking.AdminBookingListRequest;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingResponse;
import com.mss.project.booking_service.payload.admin.booking.AdminBookingUpdateRequest;
import org.springframework.data.domain.Page;

public interface AdminBookingService {
    Page<AdminBookingResponse> getAllBookings(AdminBookingListRequest request);

    AdminBookingResponse getBookingById(Integer id);

    AdminBookingResponse updateBooking(Integer id, AdminBookingUpdateRequest request);

    void deleteBooking(Integer id);

    long countBookingsByStatus();
}
