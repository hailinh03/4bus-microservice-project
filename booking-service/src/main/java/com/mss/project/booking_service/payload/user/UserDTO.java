package com.mss.project.booking_service.payload.user;

import com.mss.project.booking_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
}