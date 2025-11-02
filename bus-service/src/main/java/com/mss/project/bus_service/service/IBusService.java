package com.mss.project.bus_service.service;

import com.mss.project.bus_service.dto.request.BusDTO;
import com.mss.project.bus_service.dto.response.BusResponse;
import com.mss.project.bus_service.dto.response.BusListResponse;

public interface IBusService {
    BusResponse createBus(BusDTO busDTO);
    BusListResponse getAllBuses(int page, int size, String sortBy, String sortDirection, String searchString);
    BusResponse getBusById(int id);
    BusResponse updateBus(int id, BusDTO busDTO);
    BusResponse deleteBus(int id);
    BusListResponse searchBuses(String query, int page, int size);
}
