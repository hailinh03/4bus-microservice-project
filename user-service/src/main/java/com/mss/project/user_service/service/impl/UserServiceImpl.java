package com.mss.project.user_service.service.impl;

import com.mss.project.user_service.dto.request.CreateDriverRequest;
import com.mss.project.user_service.dto.request.CreateUserRequest;
import com.mss.project.user_service.dto.request.NotificationRequest;
import com.mss.project.user_service.dto.response.UserDTO;
import com.mss.project.user_service.entity.User;
import com.mss.project.user_service.enums.Role;
import com.mss.project.user_service.enums.UserSortField;
import com.mss.project.user_service.mapper.UserMapper;
import com.mss.project.user_service.repository.UserRepository;
import com.mss.project.user_service.service.INotificationService;
import com.mss.project.user_service.service.IUserService;
import com.mss.project.user_service.service.TripService;
import com.mss.project.user_service.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.emailExchange}")
    private String emailExchangeName;
    @Value("${rabbitmq.routing.emailKey}")
    private String emailRoutingKey;
    @NonFinal
    @Value("${app.default.avatar}")
    protected String defaultAvatar;
    private final TripService tripService;

    @Override
    public User getAuthenticatedUser() {
        var context = SecurityContextHolder.getContext();
        //get subject from token
        var userId = context.getAuthentication().getName();
        int id = Integer.parseInt(userId);
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng " + id));
    }

    @Override
    public UserDTO register(CreateUserRequest userDto) {
        String PHONE_REGEX = "^[0-9]{10}$";
        User newUser = new User();
        if(!userDto.getPassword().equals(userDto.getConfirmPassword())){
            throw new RuntimeException("Mật khẩu không khớp");
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()){
            throw new RuntimeException("Email đã được sử dụng");
        }
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()){
            throw new RuntimeException("Tên đăng nhập đã được sử dụng");
        }
        if(!userDto.getPhoneNumber().matches(PHONE_REGEX)){
            throw new RuntimeException("Số điện thoại không hợp lệ.");
        }
        newUser.setUsername(userDto.getUsername());
        newUser.setFirstName(userDto.getFirstName());
        newUser.setLastName(userDto.getLastName());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newUser.setPhoneNumber(userDto.getPhoneNumber());
        newUser.setEmail(userDto.getEmail());
        newUser.setAddress(userDto.getAddress());
        newUser.setRole(Role.PASSENGER);
        newUser.setAvatarUrl(defaultAvatar);
        userRepository.save(newUser);

        rabbitTemplate.convertAndSend(emailExchangeName, emailRoutingKey, newUser.getEmail());

        return userMapper.toUserDTO(newUser);
    }

    @Override
    public UserDTO getUserById(int id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + id));

        return userMapper.toUserDTO(user);
    }

    @Transactional
    @Override
    public UserDTO updateUser(Map<String, Object> updateRequest) {

        User authenticatedUser = getAuthenticatedUser();
        updateRequest.forEach((fieldName, value) -> {
            switch (fieldName) {
                case "firstName":
                    authenticatedUser.setFirstName((String) value);
                    break;
                case "lastName":
                    authenticatedUser.setLastName((String) value);
                    break;
                case "phoneNumber":
                    authenticatedUser.setPhoneNumber((String) value);
                    break;
                case "address":
                    authenticatedUser.setAddress((String) value);
                    break;
                case "avatarUrl":
                    authenticatedUser.setAvatarUrl((String) value);
                    break;
                case "backgroundUrl":
                    authenticatedUser.setBackgroundUrl((String) value);
                    break;
                default:
                    throw new IllegalArgumentException("Field '" + fieldName + "' is not allowed");
            }
        });
        userRepository.save(authenticatedUser);
        return userMapper.toUserDTO(authenticatedUser);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        User authenticatedUser = getAuthenticatedUser();
        if (!passwordEncoder.matches(oldPassword, authenticatedUser.getPassword())) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }
        authenticatedUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(authenticatedUser);
    }

    @Override
    public List<UserDTO> getAllDrivers() {
        List<User> drivers = userRepository.findByRoleAndActiveAndDeleted(Role.DRIVER);
        return drivers.stream().map(userMapper::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getDriversByIds(List<Integer> ids) {
        List<User> drivers = userRepository.findByIdInAndRole(ids, Role.DRIVER);
        if (drivers.isEmpty()) {
            throw new RuntimeException("Không tìm thấy tài xế");
        }
        return drivers.stream().map(userMapper::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO createDriver(CreateUserRequest request) {
        String PHONE_REGEX = "^[0-9]{10}$";
        User newUser = new User();
        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new RuntimeException("Mật khẩu không khớp");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new RuntimeException("Email đã được sử dụng");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new RuntimeException("Tên đăng nhập đã được sử dụng");
        }
        if(!request.getPhoneNumber().matches(PHONE_REGEX)){
            throw new RuntimeException("Số điện thoại không hợp lệ.");
        }
        newUser.setUsername(request.getUsername());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setEmail(request.getEmail());
        newUser.setAddress(request.getAddress());
        newUser.setRole(Role.DRIVER);
        newUser.setAvatarUrl(defaultAvatar);
        User newDriver = userRepository.save(newUser);

        if(newDriver != null){
            CreateDriverRequest driverRequest = new CreateDriverRequest();
            driverRequest.setId(newDriver.getId());
            driverRequest.setUsername(newDriver.getUsername());
            driverRequest.setFirstName(newDriver.getFirstName());
            driverRequest.setLastName(newDriver.getLastName());
            driverRequest.setPhoneNumber(newDriver.getPhoneNumber());
            driverRequest.setEmail(newDriver.getEmail());
            driverRequest.setAddress(newDriver.getAddress());

            var response = tripService.addNewDriver(driverRequest);
        }

        return userMapper.toUserDTO(newUser);
    }

    @Override
    public Page<UserDTO> getAllUsers(int page, int size, Sort.Direction sortDir, UserSortField sortBy, String searchString,Role role, boolean isDeleted) {
        Specification<User> spec = Specification.where(
                UserSpecification.hasSearchString(searchString))
                .and(UserSpecification.hasRole(role.name()))
                .and(UserSpecification.hasDeleted(isDeleted));
        Pageable pageable = PageRequest.of(page, size, sortDir, sortBy.getFieldName());
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(userMapper::toUserDTO);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        LocalDate today = LocalDate.now();
        //current month
        LocalDate currentMonthStart = today.withDayOfMonth(1);
        LocalDate  currentMonthEnd = today.withDayOfMonth(today.lengthOfMonth());
        //previous month
        LocalDate previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDate previousMonthEnd = previousMonthStart.withDayOfMonth(previousMonthStart.lengthOfMonth());

        int totalUser = userRepository.findByRole(Role.PASSENGER).size();
        List<User> currentMonthUsers = userRepository.findByRoleAndCreatedAtBetween(Role.PASSENGER,currentMonthStart, currentMonthEnd);
        List<User> previousMonthUsers = userRepository.findByRoleAndCreatedAtBetween(Role.PASSENGER,previousMonthStart, previousMonthEnd);
        int difference = currentMonthUsers.size() - previousMonthUsers.size();
        double percentageChange = previousMonthUsers.size() == 0 ? 100 : (difference * 100.0) / previousMonthUsers.size();
        Map<String, Object> response = new HashMap<>();
        response.put("totalUser", totalUser);
        response.put("growthPercentage", percentageChange);
        response.put("currentMonthUsers", currentMonthUsers.size());
        return response;
    }

    @Override
    public void deleteUser(int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + id));
        if(user.getRole().equals(Role.PASSENGER)){
            user.setDeleted(true);
            user.setActive(false);
            userRepository.save(user);
        } else if(user.getRole().equals(Role.DRIVER)){
            var response = tripService.deleteDriver(id);
            if(response.getStatusCode().is2xxSuccessful()){
                user.setDeleted(true);
                user.setActive(false);
                userRepository.save(user);
            }else {
                throw new RuntimeException("Tài xế đang có chuyến đi hoạt động, không thể xóa");
            }
        } else {
            throw new RuntimeException("Không thể xóa tài khoản");
        }
    }

    @Override
    public void activeAction(int id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + id));
        if(user.getRole().equals(Role.PASSENGER)){
            if(user.isDeleted()){
                throw new RuntimeException("Không thể kích hoạt tài khoản đã bị xóa");
            }
            user.setActive(!user.isActive());
            userRepository.save(user);
        } else if(user.getRole().equals(Role.DRIVER)){
            var response = tripService.updateDriverStatus(id);
            if(response.getStatusCode().is2xxSuccessful()){
                user.setActive(!user.isActive());
                userRepository.save(user);
            } else {
                throw new RuntimeException("Tài xế đang có chuyến đi hoạt động, không thể vô hiệu hóa");
            }
        } else {
            throw new RuntimeException("Không thể vô hiệu hóa tài khoản");
        }
    }
}
