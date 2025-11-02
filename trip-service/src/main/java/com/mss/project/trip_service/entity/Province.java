package com.mss.project.trip_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "provinces")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    String name;
}
