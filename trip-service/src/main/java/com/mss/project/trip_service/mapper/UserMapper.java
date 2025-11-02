package com.mss.project.trip_service.mapper;

import com.mss.project.trip_service.dto.response.UserDTO;
import com.mss.project.trip_service.entity.Driver;

import java.util.List;

public class UserMapper {
    public static UserDTO mapDriverToUserDTO(Driver driver){
        if (driver == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(driver.getId());
        userDTO.setUsername(driver.getUsername());
        userDTO.setFirstName(driver.getFirstName());
        userDTO.setLastName(driver.getLastName());
        userDTO.setRole(driver.getRole());
        userDTO.setPhoneNumber(driver.getPhoneNumber());
        userDTO.setAvatarUrl(driver.getAvatarUrl());
        userDTO.setBackgroundUrl(driver.getBackgroundUrl());
        userDTO.setEmail(driver.getEmail());
        userDTO.setAddress(driver.getAddress());
        return userDTO;
    }

    public static List<UserDTO> toListUserDTO(List<Driver> drivers){
        if (drivers == null || drivers.isEmpty()) {
            return List.of();
        }
        return drivers.stream()
                .map(UserMapper::mapDriverToUserDTO)
                .toList();
    }
}
