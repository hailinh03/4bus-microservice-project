package com.mss.project.trip_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "route_points")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoutePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    String description;

    @Column(nullable = false, columnDefinition = "DECIMAL(9,6)")
    double latitude;

    @Column(nullable = false, columnDefinition = "DECIMAL(9,6)")
    double longitude;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String fullAddress;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "province_id", nullable = false)
    Province province;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    boolean isActive = true;

    boolean isDeleted = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "route_point_id")
    List<RouteDetail> routeDetails = new ArrayList<>();
}
