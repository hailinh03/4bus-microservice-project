package com.mss.project.trip_service.service.impl;

import com.mss.project.trip_service.dto.request.CreateDriverRequest;
import com.mss.project.trip_service.dto.response.UserDTO;
import com.mss.project.trip_service.entity.Driver;
import com.mss.project.trip_service.mapper.UserMapper;
import com.mss.project.trip_service.repository.DriverRepository;
import com.mss.project.trip_service.repository.TripRepository;
import com.mss.project.trip_service.service.IDriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverService implements IDriverService {

    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    @Override
    public UserDTO createDriver(CreateDriverRequest createDriverRequest) {
        Driver driver = driverRepository.findById(createDriverRequest.getId()).orElse(null);
        if (driver != null) {
            throw new IllegalArgumentException("Driver with this ID already exists");
        }
        try {
            driver = new Driver();
            driver.setId(createDriverRequest.getId());
            driver.setUsername(createDriverRequest.getUsername());
            driver.setFirstName(createDriverRequest.getFirstName());
            driver.setLastName(createDriverRequest.getLastName());
            driver.setPhoneNumber(createDriverRequest.getPhoneNumber());
            driver.setAddress(createDriverRequest.getAddress());
            driver.setEmail(createDriverRequest.getEmail());
            Driver newDriver = driverRepository.save(driver);
            return UserMapper.mapDriverToUserDTO(newDriver);
        }catch (Exception e){
            throw new RuntimeException("Something went wrong while creating the driver");
        }
    }

    @Override
    public UserDTO deleteDriver(int driverId) {
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new RuntimeException("Tài xế không tồn tại"));
        if(tripRepository.existsActiveTripByDriverId(driverId)){
            throw new RuntimeException("Tài xế đang có chuyến đi hoạt động, không thể xóa");
        }else{
            driver.setDeleted(true);
            driver.setActive(false);
            driverRepository.save(driver);
        }

        return UserMapper.mapDriverToUserDTO(driver);
    }

    @Override
    public UserDTO updateDriverStatus(int driverId) {
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new RuntimeException("Tài xế không tồn tại"));
        if(tripRepository.existsActiveTripByDriverId(driverId)){
            throw new RuntimeException("Tài xế đang có chuyến đi hoạt động, không thể xóa");
        }else{
            driver.setActive(!driver.isActive());
            driverRepository.save(driver);
        }
        return UserMapper.mapDriverToUserDTO(driver);
    }
}
