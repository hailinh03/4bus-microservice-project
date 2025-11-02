package com.mss.project.trip_service.service;

import com.mss.project.trip_service.config.FeignClientConfig;
import com.mss.project.trip_service.dto.response.BusDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bus-service",url = "${4BUS.service.bus-service.url}",
configuration = FeignClientConfig.class)
public interface IBusService {
    @GetMapping("/api/buses/{id}")
    BusDTO getBusById(@PathVariable("id") int id);
}
