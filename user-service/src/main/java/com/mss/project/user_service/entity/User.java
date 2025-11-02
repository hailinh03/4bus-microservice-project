package com.mss.project.user_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mss.project.user_service.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @NotEmpty(message = "Tên đăng nhập là bắt buộc")
    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    String username;

    @NotEmpty(message = "Mật khẩu là bắt buộc")
    @JsonIgnore
    String password;

    @NotEmpty(message = "Tên là bắt buộc")
    String firstName;

    @NotEmpty(message = "Họ là bắt buộc")
    String lastName;

    @Enumerated(EnumType.STRING)
    Role role;

    String phoneNumber;

    String avatarUrl;

    String backgroundUrl;

    @Email
    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(255) COLLATE utf8mb4_unicode_ci")
    @NotEmpty(message = "Email là bắt buộc")
    String email;

    String address;

    @CreationTimestamp
    LocalDate createdAt;

    @UpdateTimestamp
    LocalDate updatedAt;

    boolean isGoogleAccount = false;
    boolean isActive = true;
    boolean isDeleted = false;

}
