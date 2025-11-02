package com.mss.project.trip_service.service;

import com.mss.project.trip_service.dto.response.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service", url = "${4BUS.service.user-service.url}")
public interface IUserService {
    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable("id") int id);

    @GetMapping("/users/drivers")
    List<UserDTO> getAllDrivers();
}
