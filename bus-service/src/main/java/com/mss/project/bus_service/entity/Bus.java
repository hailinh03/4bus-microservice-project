package com.mss.project.bus_service.entity;

import com.mss.project.bus_service.enums.BusStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bus")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@SQLRestriction("is_deleted = false")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci",unique = true)
    @NotEmpty(message = "Tên xe là bắt buộc")
    String name;

    @NotEmpty(message = "Biển số xe là bắt buộc")
    @Column(nullable = false, unique = true)
    String plateNumber;

    @NotEmpty(message = "Màu xe là bắt buộc")
    String color;


    String description;

    @CreationTimestamp
    LocalDateTime createdAt;
    @UpdateTimestamp
    LocalDateTime updatedAt;
    boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    BusStatus status = BusStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    BusCategory category;

    @OneToMany(mappedBy = "bus", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BusImage> images;
}
