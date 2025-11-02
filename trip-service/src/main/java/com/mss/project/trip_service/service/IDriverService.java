package com.mss.project.trip_service.service;

import com.mss.project.trip_service.dto.request.CreateDriverRequest;
import com.mss.project.trip_service.dto.response.UserDTO;

public interface IDriverService {

    UserDTO createDriver(CreateDriverRequest createDriverRequest);

    UserDTO deleteDriver(int driverId);

    UserDTO updateDriverStatus(int driverId);
}
