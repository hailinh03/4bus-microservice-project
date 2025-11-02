package com.mss.project.trip_service.service;

import com.mss.project.trip_service.dto.request.RoutePointCreateRequest;
import com.mss.project.trip_service.dto.request.TripCreateRequest;
import com.mss.project.trip_service.dto.response.RoutePointDTO;
import com.mss.project.trip_service.dto.response.RoutePointListDTO;
import com.mss.project.trip_service.dto.response.TripDTO;
import com.mss.project.trip_service.dto.response.TripListDTO;
import com.mss.project.trip_service.entity.Trip;
import com.mss.project.trip_service.enums.RoutePointSortField;
import com.mss.project.trip_service.enums.TripSortField;
import com.mss.project.trip_service.enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface ITripService {
    /** Get all trips with pagination and sorting options.
     *
     * @param searchString search term for trip name or description
     * @param startProvinceId ID of the starting province
     * @param endProvinceId ID of the ending province
     * @param startTime start time of the trip
     * @param status status of the trip
     * @param page page number for pagination
     * @param size number of items per page
     * @param sortBy field to sort by
     * @param sortDir direction of sorting (ASC or DESC)
     * @return TripListDTO containing a list of trips and pagination information
     */
    TripListDTO getAllTrips(String searchString, Integer startProvinceId, Integer endProvinceId, LocalDateTime startTime,
                            TripStatus status, int page, int size, TripSortField sortBy, Sort.Direction sortDir);

    /** Create a new trip, it will get the driver information from the user service
     * and bus information from the bus service. Checks for overlapping trips
     * and ensures that the trip does not conflict with existing trips.
     *
     * @param tripCreateRequest request object containing trip details
     * @return TripDTO containing the created trip details
     */
    TripDTO createTrip(TripCreateRequest tripCreateRequest);

    /** Get a trip by its ID.
     *
     * @param tripId ID of the trip
     * @return TripDTO containing the trip details
     */
    TripDTO getTripById(int tripId);

    /** Update an existing trip.
     *
     * @param id ID of the trip to be updated
     * @param tripCreateRequest request object containing updated trip details
     * @return TripDTO containing the updated trip details
     */
    TripDTO updateTrip(int id, TripCreateRequest tripCreateRequest);

    /** Delete a trip by its ID.
     *
     * @param id ID of the trip to be deleted
     */
    void deleteTrip(int id);

    Page<TripDTO> getAllTripsByDriverId(int driverId, int page, int size);
}
