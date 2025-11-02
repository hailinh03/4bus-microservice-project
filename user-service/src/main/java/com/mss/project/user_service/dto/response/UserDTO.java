package com.mss.project.user_service.dto.response;

import com.mss.project.user_service.enums.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {

    int id;
    String username;
    String firstName;
    String lastName;
    Role role;
    String phoneNumber;
    String avatarUrl;
    String backgroundUrl;
    String email;
    String address;
    boolean isActive;
    boolean isDeleted;
    private boolean isGoogleAccount;
    LocalDate createdAt;
    LocalDate updatedAt;
}
