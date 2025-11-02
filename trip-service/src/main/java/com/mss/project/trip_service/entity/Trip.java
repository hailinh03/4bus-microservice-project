package com.mss.project.trip_service.entity;

import com.mss.project.trip_service.enums.TripStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "trips")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    TripStatus status;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String origin;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String destination;

    @Column(nullable = false)
    int estimateDuration; // in minutes

    @Column(nullable = false)
    LocalDateTime startTime; // format: YYYY-MM-DD HH:mm:ss

    @Column(nullable = false)
    LocalDateTime estimateEndTime; // format: YYYY-MM-DD HH:mm:ss

    boolean isHoliday = false;

    @CreationTimestamp
    LocalDateTime createdAt; // format: YYYY-MM-DD HH:mm:ss

    @UpdateTimestamp
    LocalDateTime updatedAt; // format: YYYY-MM-DD HH:mm:ss

    @ManyToOne(cascade = CascadeType.ALL)
    Route route;

    int busId;

    int startProvinceId;

    int endProvinceId;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "trip_drivers",
            joinColumns = @JoinColumn(name = "trip_id"),
            inverseJoinColumns = @JoinColumn(name = "driver_id")
    )
    List<Driver> drivers;

}
