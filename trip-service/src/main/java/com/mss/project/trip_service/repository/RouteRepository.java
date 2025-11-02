package com.mss.project.trip_service.repository;

import com.mss.project.trip_service.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Integer> {
    Page<Route> findAll(Specification<Route> spec, Pageable pageable);
}
