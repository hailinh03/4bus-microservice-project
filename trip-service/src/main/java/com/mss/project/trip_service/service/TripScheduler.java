package com.mss.project.trip_service.service;

import com.mss.project.trip_service.entity.Trip;
import com.mss.project.trip_service.enums.TripStatus;
import com.mss.project.trip_service.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TripScheduler {
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private IBookingService iBookingService;

    @Scheduled(fixedRate = 60000)
    public void updatePlannedTripsToStarted() {
        try {
            System.out.println("Checking for planned trips to update to started...");
            LocalDateTime now = LocalDateTime.now();
            var trips = tripRepository.findByStatusAndStartTimeLessThanEqual(TripStatus.PLANNED, now);
            for (Trip trip : trips) {
                trip.setStatus(TripStatus.STARTED);
            }
            tripRepository.saveAll(trips);
        } catch (Exception e) {
            System.err.println("Error in updatePlannedTripsToStarted: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000)
    public void updateStartedTripsToCompleted() {
        try {
            System.out.println("Checking for started trips to update to completed...");
            LocalDateTime now = LocalDateTime.now();
            var trips = tripRepository.findByStatusAndEstimateEndTimeLessThanEqual(TripStatus.STARTED, now);
            for (Trip trip : trips) {
                trip.setStatus(TripStatus.COMPLETED);
                iBookingService.markTicketsUsedByTripId(trip.getId());
            }
            tripRepository.saveAll(trips);

        } catch (Exception e) {
            System.err.println("Error in updateStartedTripsToCompleted: " + e.getMessage());
        }
    }
}
