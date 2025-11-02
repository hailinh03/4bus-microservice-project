package com.mss.project.bus_service.repository;

import com.mss.project.bus_service.entity.BusCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusCategoryRepository extends JpaRepository<BusCategory, Integer> {
    boolean existsByName(String name);
    
    @Query("SELECT bc FROM BusCategory bc WHERE " +
           "LOWER(bc.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bc.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<BusCategory> searchBusCategories(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<BusCategory> findAll(Specification<BusCategory> specification, Pageable pageable);
    BusCategory findBusCategoriesByName(String name);
}
