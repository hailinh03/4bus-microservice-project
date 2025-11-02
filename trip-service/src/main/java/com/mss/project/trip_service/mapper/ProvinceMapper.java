package com.mss.project.trip_service.mapper;

import com.mss.project.trip_service.dto.response.ProvinceDTO;
import com.mss.project.trip_service.entity.Province;

public class ProvinceMapper {

    public static ProvinceDTO toProvinceDTO(Province province) {
        if (province == null) {
            return null;
        }

        return ProvinceDTO.builder()
                .id(province.getId())
                .name(province.getName())
                .build();
    }
}
