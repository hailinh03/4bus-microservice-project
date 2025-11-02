package com.mss.project.trip_service.repository;

import com.mss.project.trip_service.entity.Trip;
import com.mss.project.trip_service.enums.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Integer> {

    /** Find all trips by driver ID.
     * @param driverId the ID of the driver
     */
    @Query("SELECT t FROM Trip t JOIN t.drivers d WHERE d.id = :driverId")
    List<Trip> getAllByDriverId(
            @Param("driverId") Integer driverId);

    /** Find all trips by bus ID.
     * @param busId the ID of the bus
     * @return a list of trips associated with the bus
     */
    List<Trip> getAllByBusId(Integer busId);

    @Query(
            value = "SELECT * FROM trips " +
                    "WHERE " +
                    "(:searchString IS NULL OR :searchString = '' OR " +
                    "   LOWER(name) LIKE LOWER(CONCAT('%', :searchString, '%')) " +
                    "   OR LOWER(origin) LIKE LOWER(CONCAT('%', :searchString, '%')) " +
                    "   OR LOWER(destination) LIKE LOWER(CONCAT('%', :searchString, '%'))) " +
                    "AND (:startProvinceId IS NULL OR start_province_id = :startProvinceId) " +
                    "AND (:endProvinceId IS NULL OR end_province_id = :endProvinceId) " +
                    "AND (:startTime IS NULL OR DATE(start_time) = DATE(:startTime)) " +
                    "AND (:status IS NULL OR status = :status)",
            countQuery = "SELECT COUNT(*) FROM trips " +
                    "WHERE" +
                    "(:searchString IS NULL OR :searchString = '' OR " +
                    "   LOWER(name) LIKE LOWER(CONCAT('%', :searchString, '%')) " +
                    "   OR LOWER(origin) LIKE LOWER(CONCAT('%', :searchString, '%')) " +
                    "   OR LOWER(destination) LIKE LOWER(CONCAT('%', :searchString, '%'))) " +
                    "AND (:startProvinceId IS NULL OR start_province_id = :startProvinceId) " +
                    "AND (:endProvinceId IS NULL OR end_province_id = :endProvinceId) " +
                    "AND (:startTime IS NULL OR DATE(start_time) = DATE(:startTime)) " +
                    "AND (:status IS NULL OR status = :status)",
            nativeQuery = true
    )
    Page<Trip> getAllByFilters(
            @Param("searchString") String searchString,
            @Param("startProvinceId") Integer startProvinceId,
            @Param("endProvinceId") Integer endProvinceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("status") String status,
            Pageable pageable
    );

    List<Trip> findByStatusAndStartTimeLessThanEqual(TripStatus status, LocalDateTime now);
    List<Trip> findByStatusAndEstimateEndTimeLessThanEqual(TripStatus status, LocalDateTime now);
    List<Trip> findAllByRouteId(Integer routeId);

    @Query("SELECT t from Trip t JOIN t.drivers d WHERE d.id = :driverId")
    Page<Trip> findAllByDriversId(Integer driverId, Pageable pageable);

    @Query("SELECT COUNT(t) > 0 FROM Trip t JOIN t.drivers d " +
            "WHERE d.id = :driverId AND t.status IN ('PLANNED', 'STARTED')")
    boolean existsActiveTripByDriverId(@Param("driverId") Integer driverId);

}
