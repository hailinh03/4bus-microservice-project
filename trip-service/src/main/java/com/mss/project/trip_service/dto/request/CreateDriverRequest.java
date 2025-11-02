package com.mss.project.trip_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateDriverRequest {

    @NotBlank(message = "ID là bắt buộc")
    Integer id;
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    String username;
    @NotBlank(message = "Tên là bắt buộc")
    String firstName;
    @NotBlank(message = "Họ là bắt buộc")
    String lastName;
    @NotBlank(message = "Số điện thoại là bắt buộc")
    String phoneNumber;
    @NotBlank(message = "Email là bắt buộc")
    String email;
    @NotBlank(message = "Địa chỉ là bắt buộc")
    String address;
}
