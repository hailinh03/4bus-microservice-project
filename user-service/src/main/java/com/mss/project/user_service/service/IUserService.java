package com.mss.project.user_service.service;

import com.mss.project.user_service.dto.request.CreateUserRequest;
import com.mss.project.user_service.dto.response.UserDTO;
import com.mss.project.user_service.entity.User;
import com.mss.project.user_service.enums.Role;
import com.mss.project.user_service.enums.UserSortField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

public interface IUserService {

    User getAuthenticatedUser();
    UserDTO register(CreateUserRequest request);
    UserDTO getUserById(int id);
    UserDTO updateUser(Map<String, Object> updateRequest);
    void changePassword(String oldPassword, String newPassword);
    List<UserDTO> getAllDrivers();
    List<UserDTO> getDriversByIds(List<Integer> ids);
    UserDTO createDriver(CreateUserRequest request);

    Page<UserDTO> getAllUsers(int page,
                              int size,
                              Sort.Direction sortDir,
                              UserSortField sortBy,
                              String searchString,
                              Role role,
                              boolean isDeleted);
    Map<String, Object> getUserStatistics();

    void deleteUser(int id);

    void activeAction(int id);
}
