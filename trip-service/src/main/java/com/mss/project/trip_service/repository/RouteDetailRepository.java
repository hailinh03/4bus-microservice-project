package com.mss.project.trip_service.repository;

import com.mss.project.trip_service.entity.RouteDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteDetailRepository extends JpaRepository<RouteDetail, Integer> {
    List<RouteDetail> findAllByRoutePointId(Integer routePointId);
}
