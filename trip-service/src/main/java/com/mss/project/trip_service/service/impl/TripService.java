package com.mss.project.trip_service.service.impl;

import com.mss.project.trip_service.dto.request.TripCreateRequest;
import com.mss.project.trip_service.dto.response.BusDTO;
import com.mss.project.trip_service.dto.response.TripDTO;
import com.mss.project.trip_service.dto.response.TripListDTO;
import com.mss.project.trip_service.dto.response.UserDTO;
import com.mss.project.trip_service.entity.*;
import com.mss.project.trip_service.enums.BusStatus;
import com.mss.project.trip_service.enums.TripSortField;
import com.mss.project.trip_service.enums.TripStatus;
import com.mss.project.trip_service.mapper.TripMapper;
import com.mss.project.trip_service.mapper.UserMapper;
import com.mss.project.trip_service.repository.DriverRepository;
import com.mss.project.trip_service.repository.RouteRepository;
import com.mss.project.trip_service.repository.TripRepository;
import com.mss.project.trip_service.service.IBookingService;
import com.mss.project.trip_service.service.IBusService;
import com.mss.project.trip_service.service.ITripService;
import com.mss.project.trip_service.utils.StringToEnumConverter;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService implements ITripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final IBusService busClientService;
    private final IBookingService bookingClientService;
    private final DriverRepository driverRepository;

    @Override
    public TripListDTO getAllTrips(String searchString, Integer startProvinceId, Integer endProvinceId, LocalDateTime startTime,
                            TripStatus status, int page, int size, TripSortField sortBy, Sort.Direction sortDir){
        Pageable pageable = PageRequest.of(page, size, sortDir, sortBy.getFieldName());
        StringToEnumConverter.StringToTripStatusConverter converter = new StringToEnumConverter.StringToTripStatusConverter();
        String statusName = null;
        if (status !=null) {
            TripStatus requestStatus = converter.convert(status.name());
            statusName = requestStatus != null ? requestStatus.name() : null;
        }
        Page<Trip> tripPage = tripRepository.getAllByFilters(
                searchString, startProvinceId, endProvinceId, startTime, statusName, pageable
        );
        return toTripListDTO(tripPage);
    }

    @Override
    public TripDTO createTrip(TripCreateRequest tripCreateRequest) {
        Trip trip;
        try{
            List<Driver> drivers = new ArrayList<>();
            // Get drivers by their IDs from requestDriverIds
            for (Integer requestDriverId : tripCreateRequest.getDriverIds()) {// For each driver ID in the request
                Driver driver = driverRepository.findById(requestDriverId)
                        .orElseThrow(() -> new RuntimeException("Tài xế đã chọn không tồn tại."));
                drivers.add(driver);
            }
            precheckCreateTripRequest(tripCreateRequest, drivers);
            trip = new Trip();
            // Set trip details
            setTripData(drivers, tripCreateRequest, trip);
            trip.setStatus(TripStatus.PLANNED);
            // Save trip to the database
            Trip savedTrip = tripRepository.save(trip);
            return TripMapper.toTripDTO(
                    savedTrip, getBusDTOByBusId(tripCreateRequest.getBusId()),
                    UserMapper.toListUserDTO(drivers), new ArrayList<>());
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public TripDTO getTripById(int tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));
        BusDTO bus = getBusDTOByBusId(trip.getBusId());
        List<UserDTO> drivers = UserMapper.toListUserDTO(trip.getDrivers());
        List<String> bookedSeats = bookingClientService.getBookedSeatsByTripId(tripId).getData();
        return TripMapper.toTripDTO(trip, bus, drivers, bookedSeats);
    }

    @Override
    public TripDTO updateTrip(int id, TripCreateRequest tripCreateRequest) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));
        List<Driver> drivers = new ArrayList<>();
        // Get drivers by their IDs from requestDriverIds
        for (Integer requestDriverId : tripCreateRequest.getDriverIds()) {
            Driver driver = driverRepository.findById(requestDriverId)
                    .orElseThrow(() -> new RuntimeException("Tài xế đã chọn không tồn tại."));
            drivers.add(driver);
        }
        precheckCreateTripRequest(tripCreateRequest, drivers);
        // Set trip details
        setTripData(drivers, tripCreateRequest, trip);
        return TripMapper.toTripDTO(tripRepository.save(trip), getBusDTOByBusId(trip.getBusId()), UserMapper.toListUserDTO(drivers), new ArrayList<>());
    }

    private void setTripData(
            List<Driver> drivers, TripCreateRequest tripCreateRequest, Trip trip) {
        Route requestRoute = routeRepository.getReferenceById(tripCreateRequest.getRouteId());
        int estimateDuration = requestRoute.getDuration();
        LocalDateTime estimateEndTime = tripCreateRequest.getStartTime().plusMinutes(estimateDuration);
        RoutePoint startPoint = requestRoute.getRouteDetails().getFirst().getRoutePoint();
        trip.setStartProvinceId(startPoint.getProvince().getId());
        RoutePoint endPoint = requestRoute.getRouteDetails().getLast().getRoutePoint();
        trip.setEndProvinceId(endPoint.getProvince().getId());
        trip.setDrivers(drivers);
        trip.setBusId(tripCreateRequest.getBusId());
        trip.setRoute(requestRoute);
        trip.setName(tripCreateRequest.getName());
        trip.setOrigin(requestRoute.getOrigin());
        trip.setDestination(requestRoute.getDestination());
        trip.setBusId(tripCreateRequest.getBusId());
        trip.setStartTime(tripCreateRequest.getStartTime());
        trip.setHoliday(tripCreateRequest.isHoliday());
        trip.setEstimateDuration(estimateDuration);
        trip.setEstimateEndTime(estimateEndTime);
    }

    @Override
    public void deleteTrip(int id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));
        if (trip.getStatus() == TripStatus.PLANNED) {// Only trips with status PLANNED can be cancelled
            try {
                // Mark tickets as expired for the trip
                // -> all tickets associated with this trip will be marked as expired and admin need to refund them
                bookingClientService.markTicketsExpiredByTripId(trip.getId());
                trip.setStatus(TripStatus.CANCELLED);
                tripRepository.save(trip);
                log.info("Trip deleted: {}", id);
            } catch (Exception e) {
                trip.setStatus(TripStatus.PLANNED);
                log.error("Error deleting trip: {}", e.getMessage());
                throw new RuntimeException("Đã xảy ra lỗi khi xóa chuyến đi");
            }
        }else {// If the trip is not in PLANNED status, it cannot be deleted
            throw new RuntimeException("Chỉ có chuyến đi sau khi khởi hành không thể hủy bỏ");
        }
    }

    private TripListDTO toTripListDTO(Page<Trip> trips) {
        TripListDTO tripListDTO = new TripListDTO();
        List<Trip> tripEntities = trips.getContent();
        List<TripDTO> tripDTOs = new ArrayList<>();
        for (Trip trip : tripEntities) {
            BusDTO bus = getBusDTOByBusId(trip.getBusId());
            List<UserDTO> drivers = UserMapper.toListUserDTO(trip.getDrivers());
            List<String> bookedSeats = new ArrayList<>();
            try{
                bookedSeats = bookingClientService.getBookedSeatsByTripId(trip.getId()).getData();
            }catch (Exception e) {
                log.warn("Không thể lấy danh sách ghế đã đặt cho chuyến đi {}: {}", trip.getId(), e.getMessage());
            }
            tripDTOs.add(TripMapper.toTripDTO(trip, bus, drivers, bookedSeats));
        }
        tripListDTO.setTrips(tripDTOs);
        tripListDTO.setTotalPages(trips.getTotalPages());
        tripListDTO.setTotalElements(trips.getTotalElements());
        tripListDTO.setPageNumber(trips.getNumber());
        tripListDTO.setPageSize(trips.getSize());
        return tripListDTO;
    }

    private BusDTO getBusDTOByBusId(int busId) {
        BusDTO bus = busClientService.getBusById(busId);
        if (bus == null || bus.getStatus().equals(BusStatus.OUT_OF_SERVICE)) {
            throw new RuntimeException("Xe buýt không tồn tại hoặc không còn hoạt động");
        }
        return bus;
    }

    private void precheckCreateTripRequest(TripCreateRequest tripCreateRequest, List<Driver> drivers) {
        if (tripCreateRequest == null) {
            throw new RuntimeException("Yêu cầu tạo chuyến đi không được để trống.");
        }
        Route routeRequest = routeRepository.findById(tripCreateRequest.getRouteId())
                .orElseThrow(() -> new RuntimeException("Tuyến đường được chọn không tồn tại."));
        checkValidationOfDrivers(drivers, routeRequest, tripCreateRequest.getStartTime());
        BusDTO bus = getBusDTOByBusId(tripCreateRequest.getBusId());
        checkValidationOfBus(bus, routeRequest, tripCreateRequest.getStartTime());
    }

    /** Check if the drivers are valid and not overlapping with existing trips.
     * 1. Get the list of drivers from the request.
     * 2. If the list is empty, throw an exception.
     * 3. For each driver in the list:
     * 3.1 Get trips assigned to the driver.
     * 3.2 For each trip assigned to the driver - If the trip status is PLANNED or STARTED:
     * - If the create trip request's route origin = trip's destination and vice versa:
     * -> 2 exception cases:
     *      + start time of create trip request between: trip's start time and trip's estimate end time
     *      + estimate end time of create trip request between: trip's start time and trip's estimate end time
     * - Other cases:
     * --> 2 exception cases:
     *      + start time of create trip request between: trip's start time and trip's estimate end time with duration x 2
     *      + estimate end time of create trip  with duration x2 between: trip's start time and trip's estimate end time
     *
     * @param drivers the list of drivers to check
     * @param requestRoute the route of the trip being created
     * @param startTime the start time of the trip being created
     */
    private void checkValidationOfDrivers(List<Driver> drivers, Route requestRoute, LocalDateTime startTime ) {
        if (drivers == null || drivers.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một tài xế cho chuyến đi.");
        }
        for (Driver driver : drivers) {
            if (!driver.isActive() || driver.isDeleted()) {
                throw new RuntimeException("Tài xế " + driver.getUsername() + " không hoạt động.");
            }
            List<Trip> tripsAssignedToDriver = tripRepository.getAllByDriverId(driver.getId());
            for (Trip trip : tripsAssignedToDriver) {
                if (trip.getStatus() == TripStatus.PLANNED || trip.getStatus() == TripStatus.STARTED) {
                    if (requestRoute.getRouteDetails().getFirst().getRoutePoint().getId()
                    == trip.getRoute().getRouteDetails().getLast().getRoutePoint().getId()
                    || requestRoute.getRouteDetails().getLast().getRoutePoint().getId()
                    == trip.getRoute().getRouteDetails().getFirst().getRoutePoint().getId()) {
                        // Case 1: Trip's origin and destination are swapped
                        if ((trip.getStartTime().isBefore(startTime)
                                && trip.getEstimateEndTime().isAfter(startTime))
                        || (trip.getStartTime().isBefore(startTime.plusMinutes(requestRoute.getDuration()))
                                && trip.getEstimateEndTime().isAfter(startTime.plusMinutes(requestRoute.getDuration())))) {
                            throw new RuntimeException("Tài xế " + driver.getUsername() + " đã có chuyến đi khác trong khoảng thời gian này.");
                        }
                    } else {
                        // Case 2: Trip's origin and destination are not swapped
                        if ((startTime.isAfter(trip.getStartTime())
                                && startTime.isBefore(trip.getEstimateEndTime().plusMinutes(trip.getRoute().getDuration())))
                        || (startTime.plusMinutes(requestRoute.getDuration() * 2L).isAfter(trip.getStartTime())
                                && startTime.plusMinutes(requestRoute.getDuration() * 2L).isBefore(trip.getEstimateEndTime()))) {
                            throw new RuntimeException("Tài xế " + driver.getUsername() + " đã có chuyến đi khác trong khoảng thời gian này.");
                        }
                    }
                }
            }
        }
    }

    /** Check if the bus is valid and not out of service.
     * 1. Get the bus by its ID from the request.
     * 2. If the bus is null or its status is OUT_OF_SERVICE, throw an exception.
     * 3. Get the list of trips assigned to the bus.
     * 4. For each trip assigned to the bus:
     * Same logic as checkValidationOfDrivers:
     *
     * @param bus the bus to check
     * @param requestRoute the route of the trip being created
     * @param startTime the start time of the trip being created
     */
    private void checkValidationOfBus(BusDTO bus, Route requestRoute, LocalDateTime startTime) {
        if (bus == null || bus.getStatus().equals(BusStatus.OUT_OF_SERVICE)) {
            throw new RuntimeException("Xe buýt không tồn tại hoặc không còn hoạt động");
        }
        List<Trip> tripsAssignedToBus = tripRepository.getAllByBusId(bus.getId());
        for (Trip trip : tripsAssignedToBus) {
            if (trip.getStatus() == TripStatus.PLANNED || trip.getStatus() == TripStatus.STARTED) {
                if (requestRoute.getRouteDetails().getFirst().getRoutePoint().getId()
                        == trip.getRoute().getRouteDetails().getLast().getRoutePoint().getId()
                        || requestRoute.getRouteDetails().getLast().getRoutePoint().getId()
                        == trip.getRoute().getRouteDetails().getFirst().getRoutePoint().getId()) {
                    // Case 1: Trip's origin and destination are swapped
                    if ((trip.getStartTime().isBefore(startTime)
                            && trip.getEstimateEndTime().isAfter(startTime))
                            || (trip.getStartTime().isBefore(startTime.plusMinutes(requestRoute.getDuration()))
                            && trip.getEstimateEndTime().isAfter(startTime.plusMinutes(requestRoute.getDuration())))) {
                        throw new RuntimeException("Xe buýt đã có chuyến đi khác trong khoảng thời gian này.");
                    }
                } else {
                    // Case 2: Trip's origin and destination are not swapped
                    if ((startTime.isAfter(trip.getStartTime())
                            && startTime.isBefore(trip.getEstimateEndTime().plusMinutes(trip.getRoute().getDuration())))
                            || (startTime.plusMinutes(requestRoute.getDuration() * 2L).isAfter(trip.getStartTime())
                            && startTime.plusMinutes(requestRoute.getDuration() * 2L).isBefore(trip.getEstimateEndTime()))) {
                        throw new RuntimeException("Xe buýt đã có chuyến đi khác trong khoảng thời gian này.");
                    }
                }
            }
        }
    }

    @Override
    public Page<TripDTO> getAllTripsByDriverId(int driverId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Trip> tripPaging = tripRepository.findAllByDriversId(driverId, pageable);

        return tripPaging.map(TripMapper::toDto);
    }
}
