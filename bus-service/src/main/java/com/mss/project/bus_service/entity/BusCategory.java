package com.mss.project.bus_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bus_categories")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@SQLRestriction("is_deleted = false")
public class BusCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci", unique = true)
    @NotEmpty(message = "Tên loại xe là bắt buộc")
    String name;

    @NotNull
    @Min(1)
    int totalSeats;

    String description;


    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

    boolean isDeleted = false;

    long maxPrice = 0;
    long minPrice = 0;

    @Lob
    @Column(name = "seat_code", columnDefinition = "TEXT")
    @NotEmpty
    private String seatCodes;


    @OneToMany(mappedBy = "category")
    List<Bus> buses;


}
