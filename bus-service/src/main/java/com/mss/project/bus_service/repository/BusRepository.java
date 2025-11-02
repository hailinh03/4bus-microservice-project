package com.mss.project.bus_service.repository;

import com.mss.project.bus_service.entity.Bus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusRepository  extends JpaRepository<Bus, Integer> {
    boolean existsByName (String name);
    boolean existsByPlateNumber(String plateNumber);
    
    @Query("SELECT b FROM Bus b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Bus> searchBuses(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<Bus> findAll(Specification<Bus> specification, Pageable pageable);

}
