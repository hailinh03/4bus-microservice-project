package com.mss.project.user_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateDriverRequest {

    Integer id;
    String username;
    String firstName;
    String lastName;
    String phoneNumber;
    String email;
    String address;
}
