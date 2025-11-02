package com.mss.project.bus_service.repository;

import com.mss.project.bus_service.entity.BusImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusImageRepository extends JpaRepository<BusImage, Integer> {
    boolean existsByPublicId(String publicId);
}
