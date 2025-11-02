package com.mss.project.trip_service.utils;

import com.mss.project.trip_service.enums.TripStatus;
import jakarta.annotation.Nullable;
import org.springframework.core.convert.converter.Converter;

public class StringToEnumConverter {

    public static class StringToTripStatusConverter implements Converter<String, TripStatus> {
        @Override
        public TripStatus convert(@Nullable String source) {
            if (source == null || source.trim().isEmpty()) {
                return null;
            }
            try {
                return TripStatus.fromString(source.trim());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid TripStatus value: " + source, e);
            }
        }
    }
}
