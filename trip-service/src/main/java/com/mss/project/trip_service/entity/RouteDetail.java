package com.mss.project.trip_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "route_details")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RouteDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_point_id", nullable = false)
    RoutePoint routePoint;

    @Column(nullable = false)
    int orderIndex;

    @Column(nullable = false)
    int durationFromPreviousPoint; // in minutes
}
