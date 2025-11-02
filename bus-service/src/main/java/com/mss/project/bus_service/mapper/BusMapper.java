package com.mss.project.bus_service.mapper;

import com.mss.project.bus_service.dto.request.BusImageDTO;
import com.mss.project.bus_service.dto.response.BusCategoryResponse;
import com.mss.project.bus_service.dto.response.BusResponse;
import com.mss.project.bus_service.dto.response.BusImageDTOResponse;
import com.mss.project.bus_service.entity.Bus;
import com.mss.project.bus_service.entity.BusImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusMapper {

    private final BusCategoryMapper busCategoryMapper;

    public BusResponse mapToBusCreateResponse(Bus bus) {
        BusResponse response = new BusResponse();
        response.setId(bus.getId());
        response.setName(bus.getName());
        response.setPlateNumber(bus.getPlateNumber());
        response.setColor(bus.getColor());
        response.setDescription(bus.getDescription());
        response.setCreatedAt(bus.getCreatedAt());
        response.setStatus(bus.getStatus());
        response.setUpdatedAt(bus.getUpdatedAt());
        response.setHasDeleted(bus.isDeleted());
        if (bus.getCategory() != null) {
            BusCategoryResponse busCategoryResponse = busCategoryMapper.toResponse(bus.getCategory());
            response.setCategory(busCategoryResponse);
        }
        response.setImages(bus.getImages().stream()
                .map(image -> new BusImageDTOResponse(image.getId(),image.getImageUrl(), image.getPublicId()))
                .toList());
        return response;
    }

   public BusImage mapToBusImage(BusImageDTO busImageDTO) {
        BusImage busImage = new BusImage();
        busImage.setImageUrl(busImageDTO.getImageUrl());
        busImage.setPublicId(busImageDTO.getPublicId());
        return busImage;
    }
}
