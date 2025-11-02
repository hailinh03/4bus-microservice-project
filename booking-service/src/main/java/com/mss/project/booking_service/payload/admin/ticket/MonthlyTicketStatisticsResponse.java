package com.mss.project.booking_service.payload.admin.ticket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTicketStatisticsResponse {
    private Integer year;
    private Long totalTickets;
    private Long totalActiveTickets;
    private Long totalUsedTickets;
    private Long totalCancelledTickets;
    private Long totalExpiredTickets;
    private List<MonthlyTicketData> monthlyData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTicketData {
        private Month month;
        private String monthName;
        private Integer monthNumber;
        private Long totalTickets;
        private Long activeTickets;
        private Long usedTickets;
        private Long cancelledTickets;
        private Long expiredTickets;
        private Double averageTicketPrice;
        private Long totalRevenue;
    }
}
