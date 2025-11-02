package com.mss.project.user_service.service;

import com.mss.project.user_service.config.AuthRequestInterceptor;
import com.mss.project.user_service.dto.request.CreateDriverRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "trip-service", url = "${trip.service.url}", configuration = {AuthRequestInterceptor.class})
public interface TripService {

    @PostMapping("/drivers")
    ResponseEntity<?> addNewDriver(@RequestBody CreateDriverRequest driverRequest);

    @DeleteMapping("/drivers/{driverId}")
    ResponseEntity<?> deleteDriver(@PathVariable int driverId);

    @PutMapping("/drivers/action/{driverId}")
    ResponseEntity<?> updateDriverStatus(@PathVariable int driverId);
}
