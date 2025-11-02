package com.mss.project.trip_service.entity;

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
@Table(name = "routes")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String name;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String origin;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String destination;

    @Column(nullable = false)
    int distance;// in kilometers

    @Column(nullable = false)
    int duration;// in minutes

    @Column(nullable = false)
    int totalRoutePoints;

    @Column(columnDefinition = "TEXT")
    String description;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    boolean isDeleted = false;

    boolean isActive = true;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    List<RouteDetail> routeDetails;
}
