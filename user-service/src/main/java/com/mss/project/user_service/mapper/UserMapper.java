package com.mss.project.user_service.mapper;

import com.mss.project.user_service.dto.response.UserDTO;
import com.mss.project.user_service.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setBackgroundUrl(user.getBackgroundUrl());
        userDTO.setRole(user.getRole());
        userDTO.setAddress(user.getAddress());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setActive(user.isActive());
        userDTO.setDeleted(user.isDeleted());
        userDTO.setGoogleAccount(user.isGoogleAccount());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        return userDTO;
    }
}
