package com.mss.project.trip_service.repository;

import com.mss.project.trip_service.entity.RoutePoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Integer> {
    Page<RoutePoint> findAll(Specification<RoutePoint> spec, Pageable pageable);
}
