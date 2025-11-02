package com.mss.project.bus_service.config;

import com.mss.project.bus_service.entity.Bus;
import com.mss.project.bus_service.entity.BusCategory;
import com.mss.project.bus_service.entity.BusImage;
import com.mss.project.bus_service.enums.BusStatus;
import com.mss.project.bus_service.repository.BusCategoryRepository;
import com.mss.project.bus_service.repository.BusImageRepository;
import com.mss.project.bus_service.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AppInitConfig {

    private final BusRepository busRepository;
    private final BusCategoryRepository busCategoryRepository;
    private final BusImageRepository busImageRepository;

    @Bean
    ApplicationRunner init() {
        return args -> {
            if (busRepository.count() > 0 && busCategoryRepository.count() > 0 && busImageRepository.count() > 0) {
                System.out.println("Bus,img Bus, category Bus data already exists, skipping initialization");
                return;
            }
            // Initialize Bus Categories
//            BusCategory categoryInit = new BusCategory();
//            categoryInit.setName("Limousine 22 Chỗ Giường Phòng Đôi Có WC");
//            categoryInit.setTotalSeats(22);
//            categoryInit.setFloor(true);
//            categoryInit.setDescription("Limousine xịn giường phòng 22 chỗ có WC, ghế ngồi bọc da cao cấp, tiện nghi hiện đại.");
//            categoryInit.setSeatCode("T1-A1,T1-A2,T1-B1,T1-B2,T1-C1,T1-C2,T1-D1,T1-D2,T1-E1,T1-E2,T1-F1,T2-A1,T2-A2,T2-B1,T2-B2,T2-C1,T2-C2,T2-D1,T2-D2,T2-E1,T2-E2,T2-F1");
//            busCategoryRepository.save(categoryInit);
//            Bus busInit = new Bus();
//            busInit.setName("Buýt Buýt");
//            busInit.setPlateNumber("51A-12345");
//            busInit.setColor("Black");
//            busInit.setDescription("A luxury limousine bus with comfortable seating.");
//            busInit.setCategory(categoryInit);
//            busInit.setStatus(BusStatus.AVAILABLE);
//            busRepository.save(busInit);
//            // Initialize Bus Images
//            BusImage busImageInit = new BusImage();
//            busImageInit.setBus(busInit);
//            busImageInit.setImageUrl("https://res.cloudinary.com/dsuxhxkya/image/upload/v1749293367/montero-sl.img_fhrp16.webp");
//            busImageInit.setPublicId("default_bus");
//            busImageRepository.save(busImageInit);
//            System.out.println("Bus, Bus Category, and Bus Image data initialized successfully.");
        };
    }
}
