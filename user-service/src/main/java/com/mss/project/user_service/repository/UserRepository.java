package com.mss.project.user_service.repository;

import com.mss.project.user_service.entity.User;
import com.mss.project.user_service.enums.Role;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByRole(Role role);
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.isDeleted = false AND u.role = ?1")
    List<User> findByRoleAndActiveAndDeleted(Role role);
    List<User> findByIdInAndRole(List<Integer> ids, Role role);
    Page<User> findAll(Specification<User> spec, Pageable pageable);
    List<User> findAllByRole(Role role);
    List<User> findByRoleAndCreatedAtBetween(Role role,LocalDate startDate, LocalDate endDate);

}
