package com.mss.project.user_service.specification;

import com.mss.project.user_service.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasSearchString(String searchString){
        return (root, query, criteriaBuilder) -> {
            if(searchString == null || searchString.isEmpty()){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%"+searchString.toLowerCase()+"%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%"+searchString.toLowerCase()+"%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%"+searchString.toLowerCase()+"%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%"+searchString.toLowerCase()+"%")
            );
        };
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, criteriaBuilder) -> {
            if (role == null || role.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("role"), role);
        };
    }

    public static Specification<User> hasDeleted(boolean deleted){
        return (root, query, criteriaBuilder) -> {
            if (deleted) {
                return criteriaBuilder.isTrue(root.get("isDeleted"));
            } else {
                return criteriaBuilder.isFalse(root.get("isDeleted"));
            }
        };
    }
}
