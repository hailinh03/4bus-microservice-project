package com.mss.project.booking_service.payload.admin.booking;

import com.mss.project.booking_service.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBookingListRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    private BookingStatus status;
    private Long userId;
    private Integer tripId;
    private LocalDate startDate;
    private LocalDate endDate;
}
